package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/30 12:18
 */
@Mapper
public interface MessageMapper {
    /**
     * 私信列表
     * - 查询当前用户的会话列表，
     * 每个会话只显示一条最新的私信。
     * - 支持分页显示。
     * • 私信详情
     * - 查询某个会话所包含的私信。
     * - 支持分页显示。
     */
//在xml中实现
    // 查询当前用户的所以的会话列表,针对每个会话只返回一条最新的私信.
    List<Message> selectConversations(int userId, int offset, int limit);//倒叙排序，显示最新一条的信息

    // 查询当前用户的会话数量.
    int selectConversationCount(int userId);

    // 一个会话包含的信息，查询某个会话所包含的私信列表.
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量.
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息,可以发送私信
    int insertMessage(Message message);

    // 修改消息的状态，可以修改为读变成已读（1），，对未读（0）的list进行处理，状态变成 status
    int updateStatus(List<Integer> ids, int status);
    //删除的状态是2
    //这里的ids就是对应数据库中的每条对话的id

}
