package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {


//在配置文件 discusspost-mapper.xml中实现方法,sql语句实现
    //返回offer-limit的贴子
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);
    //分页处理，ordermoode==0按时间排序，==11按分数排序

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    //知道id返回处在的总行数，查询帖子
    int selectDiscussPostRows(@Param("userId") int userId);

    ////增加帖子
    int insertDiscussPost(DiscussPost discussPost);

    //实现查看帖子的功能，dao层，，根据id查找帖子
    DiscussPost selectDiscussPostById(int id);
    //sql实现

    //更新帖子的数量
    int updateCommentCount(int id, int commentCount);


    /*
    *
    • 功能实现//dao,sever,controller处理
    - 点击 置顶，修改帖子的类型。
    - 点击“加精”、“删除”，修改帖子的状态。
    • 权限管理//securityConfig 权限管理
    - 版主可以执行“置顶”、“加精”操作。
    - 管理员可以执行“删除”操作。
    • 按钮显示//html处理
    - 版主可以看到“置顶”、“加精”按钮。
    - 管理员可以看到“删除”按钮。
    * */

    //@@！！对帖子的权限管理，使得能针对不能权限呈现 置顶，加精和删除等相应的功能
    //改变状态 `type` int DEFAULT NULL COMMENT '0-普通; 1-置顶;',
    int updateType(int id, int type);
    //status` int DEFAULT NULL COMMENT '0-正常; 1-精华; 2-拉黑;',
    int updateStatus(int id, int status);

    //更新分数
    int updateScore(int id, double score);
}
