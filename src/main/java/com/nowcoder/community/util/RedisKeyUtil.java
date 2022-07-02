package com.nowcoder.community.util;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/7/1 18:40
 */
//封装  key 的格式
/**
 * 使用Redis存储验证码
 * - 验证码需要频繁的访问与刷新，对性能要求较高。
 * - 验证码不需永久保存，通常在很短的时间后就会失效。
 * - 分布式部署时，存在Session共享的问题。
 * • 使用Redis存储登录凭证
 * - 处理每次请求时，都要查询用户的登录凭证，访问的频率非常高。• 使用Redis缓存用户信息
 * - 处理每次请求时，都要根据凭证查询用户信息，访问的频率非常高。
 */

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";//帖子等点赞
    private static final String PREFIX_USER_LIKE = "like:user";//user的被点赞数
    //处理粉丝和关注的功能，也放到redis中存储，这里封装这两个功能的key
    //注意  若A关注了B，则A是B的Follower（粉丝），B是A的Followee（被关注的对象）。
    private static final String PREFIX_FOLLOWEE = "followee";//
    private static final String PREFIX_FOLLOWER = "follower";

    private static final String PREFIX_KAPTCHA = "kaptcha";//验证码缓存优化
    private static final String PREFIX_TICKET = "ticket";//使用Redis存储登录凭证
    private static final String PREFIX_USER = "user";//处理每次请求时，都要根据凭证查询用户信息，访问的频率非常高

    // 某个实体的赞，就是对某个帖子或者回复的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体，关注的实体可能有对象或者帖子。。。所以type区分
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
  /*  使用Redis存储登录凭证
 * - 处理每次请求时，都要查询用户的登录凭证，访问的频率非常高。• 使用Redis缓存用户信息*/
    //kaptcha：owner->set   owner是随机指明这次行为的标识（）
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户的缓存处理
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }



}
