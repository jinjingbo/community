package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/29 22:03
 */
@Mapper
public interface CommentMapper {
//
    //查看评论，分页处理，entityType区分了对什么的评论（主帖子的评论，评论的评论。。。）
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);
    //查找单条评论
    int selectCountByEntity(int entityType, int entityId);

    //添加评论
    int insertComment(Comment comment);
    //个人页面显示发表的comment
    Comment selectCommentById(int id);

    List<Comment> selectCommentsByUser(int userId, int offset, int limit);

    int selectCountByUser(int userId);


}
