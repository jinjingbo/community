package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/27 20:21
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

  //配合自定义注解实现
  @Autowired
  private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {//先判断是不是方法

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);

            //获取此时上面的注解
            if (loginRequired != null && hostHolder.getUser() == null) {//此时加了这个注解，但是没有登录
                response.sendRedirect(request.getContextPath() + "/login");//返回登录界面
                return false;
            }
            //别忘了还有配置拦截器的配置，静态等资源不判断
        }
        return true;
    }




}
