package com.nowcoder.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/30 20:12
 */
@Component
@Aspect//AOP注解，方面组件申明
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")//在所以方法前统一日志打印输出
    public void before(JoinPoint joinPoint) {
        // 需要统一日志格式：
        // 用户[127.0.0.1],在[xxx],访问了[com.nowcoder.community.service.xxx()].
        //获取此时的ip
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {////////加了消费者模式，这里为什么可能接收不到request呢
            //因为一般的controller是调service的，但是加了生产者——消费者模式，
            // 有些数据就直接从消费者中获取，
            // 消费者调的service，不是根据controller调的，此就没有request的请求（request是controller调用的）
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        String ip = request.getRemoteHost();
        //时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //包名，方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        //log打印日志
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }
}
