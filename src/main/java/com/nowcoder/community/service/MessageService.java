package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/30 13:35
 */
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;///发送会话时过滤敏感词

    //返回meassagemapper的数据

    //
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }
    //发送私信，，加敏感词过滤
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }
    //修改全部的未读私信状态,未读变成已读
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);//1表示已读状态，0未读
    }

    //修改私信状态,删除某一条具体id的消息 状态2、、
    // Arrays.asList(new Integer[]{id})，，，Arrays的转化为数组必须是对象
    public int deleteMessage(int id) {
        return messageMapper.updateStatus(Arrays.asList(new Integer[]{id}), 2);
    }





}
