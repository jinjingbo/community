package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/30 18:05
 */
@ControllerAdvice(annotations = Controller.class)
//这个注解，表示该类是Controller的全局配置类,括号中限制bean范围
//定向处理：服务器只是在错误发生时跳转到错误页面，（error）
// 但是我们还需要记录日志，并且在异步请求中不是返回页面，而是返回json，这些是服务器不会帮你做的事情。所以加注解
public class ExceptionAdvice {
    /**
     * @ControllerAdvice
     * - 用于修饰类，表示该类是Controller的全局配置类。
     * - 在此类中，可以对Controller进行如下三种全局配置：
     * 异常处理方案、绑定数据方案、绑定参数方案。
     * • @ExceptionHandler - 用于修饰方法，该方法会在Controller出现异常后被调用，用于处理捕获到的异常。
     */
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常: " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }
        //判断是异步的（需要xml）,,还是返回页面的处理（html）
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
