package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/27 21:18
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
// /*
//    * 拦截器的需求：
//    * 拦截器应用
//- 在请求开始时查询登录用户
//- 在本次请求中持有用户数据
//- 在模板视图上显示用户数据
//- 在请求结束时清理用户数据
//    * */
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 知道request,从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户
                //后面使用，服务器是一对多的，所以此时是一个线程的，所以考虑线程的隔离，ThreadLoad
                hostHolder.setUser(user);
            }
        }

        return true;
    }

    // /*
//    * 拦截器的需求：
//    * 拦截器应用
//- 在请求开始时查询登录用户////业务的逻辑
//- 在本次请求中持有用户数据/
//- 在模板视图上显示用户数据
//- 在请求结束时清理用户数据
//    * */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();//当前线程持有的user
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
            //此时表示登录了，在index.html中，可以加入if判断是否有了loginUser，来判断是否登录成功
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}

