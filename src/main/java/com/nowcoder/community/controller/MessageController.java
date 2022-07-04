package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/30 13:41
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /*
    * 私信列表
- 查询当前用户的会话列表，
每个会话只显示一条最新的私信。
- 支持分页显示。
• 私信详情
- 查询某个会话所包含的私信。
- 支持分页显示。*/

    //1.分页显示用户的所以会话列表
    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));//总的行数

        // 会话列表，用户的全部会话交流页面，findConversations几条会话信息
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());///倒叙排序，显示最新一条的信息

        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                //会话列表打开的，一个会话中， 所有两方交流的对话
                //因为打开会话表的时候，还要显示 1.一共几个会话  2.未读的消息  3.发送方的user信息 4.最后一条消息（/倒叙排序，显示最新一条的信息）
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));//！！删除增加message,都是异步的
                //因为未读0 已读1 删除2 是通用一个状态栏的
                //所以有一个问题：A->发送->B,但是B未读的时候，A就把message删除了，此时message 在数据库状态是 删除，B就不会再接收到了
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读私信数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 查询未读系统通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

//打开会话的详情，，，，类似帖子详情处理
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
   // @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)//////
   // @ResponseBody////异步更新已读，所以加这个备注
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表，findLetters，几条私信
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();//一个封装：Message->私信->内容+发送方
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读!!!!!!!!!!!!!!!!!!!!!!1加一个事务！
        List<Integer> ids = getLetterIds(letterList);//未读列表
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);///变成已读
        }

        return "/site/letter-detail";
    }

    //因为messagemapper中的conversationId，是由发送方和接收方加-组成的，小的id在前面（A:11  B:22   A->B:会话id11-22）
    private User getLetterTarget(String conversationId) {//得到发送方的id..也就是此时我要发私信的目标
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    //得到此时，未读的状态的list，在打开会话列表时，变成已读
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    //1.是发送给我的信息， 2.此时消息是未读状态
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }


    //发送私信，user->target
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody////异步更新，所以加这个备注
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);//根据Name，找到对应的user信息
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在!");
        }
        //更新一条新的消息 ：user->target。。。对message完成set，id自动生成
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());

        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);//内容
        message.setCreateTime(new Date());
        message.setStatus(0);///可以没有，默认是0，表示未读
        messageService.addMessage(message);//更新进mapper中

        return CommunityUtil.getJSONString(0);
    }

    //
    // 删除与某人的私信
    @RequestMapping(path = "/letter/delete", method = RequestMethod.POST)
    @ResponseBody//异步更新，所以加这个备注
    public String deleteLetter(int id) {
        messageService.deleteMessage(id);
        return CommunityUtil.getJSONString(0);
    }

    //• 通知列表
    //- 显示评论、点赞、关注三种类型的通知
    //• 通知详情
    //- 分页显示某一类主题所包含的通知
    //• 未读消息
    //- 在页面头部显示所有的未读消息数量
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // 查询评论类通知，系统通知详情：1.三类通知类型都放进去  2.只显示三类中最新的三个数据   3.要显示最新一条的一些具体的情况：
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);//最新通知
            /////只要出错！！！要加上这一个判断。
            //三notice都需要，判断不存在这一类消息的时候，就不去message中查找
            //如果没有判断(不管是哪一类notice没有，都会)，会返回404..
            if (message != null) {
                Map<String, Object> messageVO = new HashMap<>();//记录comment的情况，最新comment的详情
                messageVO.put("message", message);
                //因为加入了系统通知，系统通知的信息要json转化
                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
                messageVO.put("entityType", data.get("entityType"));
                messageVO.put("entityId", data.get("entityId"));
                messageVO.put("postId", data.get("postId"));//可以返回哪个帖子被评论

                int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
                messageVO.put("count", count);

                int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
                messageVO.put("unread", unread);
                model.addAttribute("commentNotice", messageVO);//model不能添加null的页面，所以放在if中
            }



        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);

           // Map<String, Object> messageVO = new HashMap<>();//点赞的消息，存具体内容
            if (message != null) {
                Map<String, Object> messageVO = new HashMap<>();//点赞的消息，存具体内容
                messageVO.put("message", message);

                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
                messageVO.put("entityType", data.get("entityType"));
                messageVO.put("entityId", data.get("entityId"));
                messageVO.put("postId", data.get("postId"));

                int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
                messageVO.put("count", count);

                int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
                messageVO.put("unread", unread);
                model.addAttribute("likeNotice", messageVO);
            }
           // model.addAttribute("likeNotice", messageVO);



        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        //if(message!=null){
        //    Map<String, Object> messageVO = new HashMap<>();
            if (message != null) {
                Map<String, Object> messageVO = new HashMap<>();

                messageVO.put("message", message);

                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
                messageVO.put("entityType", data.get("entityType"));
                messageVO.put("entityId", data.get("entityId"));

                int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
                messageVO.put("count", count);

                int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
                messageVO.put("unread", unread);

                model.addAttribute("followNotice", messageVO);//messageVo就存了这三类消息的最新message，还有具体内容
            }
           // model.addAttribute("followNotice", messageVO);//messageVo就存了这三类消息的最新message，还有具体内容
        //}

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //未读系统通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }
    //显示系统通知详情列表
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {//每一条message的详情查到放在model中
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());//转意，系统通知有特殊符号
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }



}
