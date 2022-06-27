package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/27 21:28
 */
@Component
public class HostHolder {
    //使用了 ThreadLocal<User> ，保证同一个线程一直持有这一个（有一个id的，只能匹配这个线程）
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }
    //结束后清理
    public void clear() {
        users.remove();
    }


}