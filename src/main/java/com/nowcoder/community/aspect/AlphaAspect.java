package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/30 20:05
 */
//@Component
//@Aspect//AOP注解
public class AlphaAspect {
    //简单实现aop

    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")//对方法的注释：哪些bean，哪些方法实现这个方法
    public void pointcut() {//这个方法没什么需求，只是充当切点的工具，和注解一起理解为一个切点

    }

    //五类通知
    @Before("pointcut()")//哪些链接点，针对这个切点
    public void before() {
        System.out.println("before");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }

    @AfterReturning("pointcut()")//有了返回值之后
    public void afterRetuning() {
        System.out.println("afterRetuning");
    }

    @AfterThrowing("pointcut()")//抛异常之后
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }
}
