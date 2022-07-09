package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/29 18:39
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件，，，，，，发帖了就把数据存到Kafka消息队列中，进行异步存储到ES中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        //发帖的时候，设置一个固定分数
        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    //显示帖子的详情页，帖子的mapper中获取user的id转化user-mapper得到user的信息，，，，
    //在这里加上评论显示的功能
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 查看帖子，放进model里
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //找到作者，,根据作者id查找作者的信息，，两种表帖子表和用户表的user-id是相同的，相关联的
        User user=userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //对帖子的点赞处理
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);//1
        model.addAttribute("likeCount", likeCount);
        // 点赞状态//看 “我”有没有点赞
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());//这个存对帖子的评论/看这里的entityType
////这个要分页处理

        // 评论VO列表：：：这个存帖子打开，所以的回复（包括对帖子的评论，包括对评论的评论）
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        if (commentList != null) {//有 对帖子 的评论
            for (Comment comment : commentList) {//对帖子评论的遍历
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();//存这个（对帖子）评论下面所以的评论（包括自己）
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                // 回复的点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 回复列表，，存这个（对帖子）评论下面所以的评论（不包括这个评论），对评论的回复
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);///看这里的entityType


                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {//对这个comment.getId()帖子有回复
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();//存回复
                        // 回复
                        replyVo.put("reply", reply);
                        // 回复的作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //对评论的点赞处理
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());//comment类型
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);


                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
            //后续的优化！！！！！！！
            ////////有个缺点，，只是处理了评论的评论，嵌套了一层，但是评论的评论的评论就无法处理，没有考虑，
            // 改成while判断后面有没有评论，没有就不往下考虑，把多层嵌套放在循环中，定值类型也可以变成count++格式

            /**上面的想法不对，
             * 帖子   评论    回复（对）、、、、、、、回复也认为是评论
             *
             * 这个不对的饿1.@@@对帖子主题的评论，叫回帖，也认为是帖子！！！！！！！！！！！！！！111后面发现这里不对，帖子的回帖，回复的也是评论
             * ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                            2.对回帖的回复才是评论
                            3.对评论的回复    还是认为是评论
                            4.所以回复只有两种：
             1.没有回帖 2.有回帖，但是回帖中没有回复
             3.回帖中有回复（但是可能存在对回复的评论），这些都认为是评论，（有userid和评论的id（comment-mapper中的targetID）进行区分）,
             comment_count,评论数量

             综上，只需要两个map即可，，一个统计回帖，一个统计回帖中的回复评论！！！！（对此评论都是一类的）

             （不存在回帖的评论的评论！！！，，都认为是评论）！！！！！！！！！！！！！！
             只要两种类型：帖子，评论
             */


            //综上
            /*
            *只要两种状态：帖子和评论
            * 对帖子的回复是评论，对评论的回复也是评论！！！！！！！！！！！！！！！！！！！！！！！！！！！
            *
            *
            * */

        }

        model.addAttribute("comments", commentVoList);





        //返回路径
        return "/site/discuss-detail";//th动态修改
        //还要对首页进行修改，使得首页显示的主题和发布的帖子相关联

    }

    //// 置顶
    //@RequestMapping(path = "/top", method = RequestMethod.POST)
    //@ResponseBody//异步请求，Kafka处理
    //public String setTop(int id) {
    //    discussPostService.updateType(id, 1);//改变type,在重新放进kafka处理
    //
    //    // 触发发帖事件,同步
    //    Event event = new Event()
    //            .setTopic(TOPIC_PUBLISH)
    //            .setUserId(hostHolder.getUser().getId())
    //            .setEntityType(ENTITY_TYPE_POST)
    //            .setEntityId(id);
    //    eventProducer.fireEvent(event);
    //
    //    return CommunityUtil.getJSONString(0);
    //}
    // 置顶、取消置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody//异步请求，Kafka处理
    public String setTop(int id) {
        DiscussPost discussPostById = discussPostService.findDiscussPostById(id);
        // 获取置顶状态，1为置顶，0为正常状态,1^1=0 0^1=1
        int type = discussPostById.getType()^1;///////////////使用异或，设置置顶开关
        discussPostService.updateType(id, type);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);

        // 触发发帖事件(更改帖子状态)
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, null, map);
    }
    //// 加精
    //@RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    //@ResponseBody
    //public String setWonderful(int id) {
    //    discussPostService.updateStatus(id, 1);///改变status
    //
    //    // 触发发帖事件
    //    Event event = new Event()
    //            .setTopic(TOPIC_PUBLISH)
    //            .setUserId(hostHolder.getUser().getId())
    //            .setEntityType(ENTITY_TYPE_POST)
    //            .setEntityId(id);
    //    eventProducer.fireEvent(event);
    //
    //    return CommunityUtil.getJSONString(0);
    //}
    // 加精、取消加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        DiscussPost discussPostById = discussPostService.findDiscussPostById(id);
        int status = discussPostById.getStatus()^1;
        // 1为加精，0为正常， 1^1=0, 0^1=1
        discussPostService.updateStatus(id, status);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);

        // 触发发帖事件(更改帖子类型)
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);/////////////////////
        // ///使用set的数据结构，方便多次操作去重处理，定时器线程不会反复计算

        return CommunityUtil.getJSONString(0, null, map);
    }


    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }



}
