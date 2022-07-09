package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/7/1 18:45
 */
@Service
public class LikeService {
    //统计点赞服务，使用redis内存记录

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞处理，因为在redis中处理，
    // 1.选取的格式是key-sets的格式
    //2.点赞更新的时候，，帖子点赞数和个人被点赞数应该是在一个事务中的。!!!
    /**
     *
     *
     * @author jinjingbo
     * @version 2022/7/1 18:54
     * @param userId  正在使用账号的用户，点赞者
     * @param entityType 消息实体
     * @param entityId
     * @param entityUserId 被点赞的人，用于更新个人主页的被点赞数
     * @return void
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {////////在事务中加入redis的操作，固定格式如下
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //得到key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //查询是否点过赞，因为要处理点2次取消赞的功能
                //还要，，，，查询必须放在事务之前！！！！！！！！！！！！事务中不能查询，无效
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();//开启事务处理

                if (isMember) {//取消点赞
                    operations.opsForSet().remove(entityLikeKey, userId);///////这个是key-sets
                    operations.opsForValue().decrement(userLikeKey);//////这个是key-String
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();//执行事务
            }
        });
    }

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //用户是key-value结构
    // 查询某人对某实体的点赞状态，，查看user有没有点赞
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);//key
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);//新的结构，，key-String 存，usr-点赞数
        return count == null ? 0 : count.intValue();
    }




}
