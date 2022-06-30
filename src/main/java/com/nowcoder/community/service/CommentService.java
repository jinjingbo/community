package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/29 22:09
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    //评论添加.....因为分别有两个表的add操作要保值是同一个事务处理（这个注解保证事务），同时完成添加评论以及帖子数量的处理
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));//标签处理
        comment.setContent(sensitiveFilter.filter(comment.getContent()));//敏感词过滤
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {//如果是帖子类型（回帖），找到comment-mapper中帖子类型的数量（SQL自动更新），在discusspost-mapper更新
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
            //显示子帖回帖数量，不显示评论数量，，但是注意回帖也是帖子，所以会显示他的评论数量（不存在回帖的评论的评论！！！，，都认为是评论）
        }
        //那对评论的评论，数量会不会更新显示呢。。不会显示///不存在回帖的评论的评论！！！，，都认为是评论）

        return rows;
    }



}
