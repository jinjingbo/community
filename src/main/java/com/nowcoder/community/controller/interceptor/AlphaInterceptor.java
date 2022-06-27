package com.nowcoder.community.controller.interceptor;

import org.aopalliance.intercept.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/27 20:09
 */
@Component
//实现拦截器，统一处理相同的处理逻辑：什么逻辑。。。。user->ticket->modul->html..
public class AlphaInterceptor implements HandlerInterceptor {
//拦截器，统一管理，降低代码的耦合
    private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    // 在Controller之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("preHandle: " + handler.toString());
        return true;
    }

    // 在Controller之后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("postHandle: " + handler.toString());
    }

    // 在TemplateEngine之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterCompletion: " + handler.toString());
    }



}
