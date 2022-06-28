package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/27 22:36
 */

//自定义注解，配合拦截器进行，，，，，，判断是不是需要注册才能接着操作的功能
    //后续只有加注解就可以了，不用像之前一样每个都写拦截器
    //具体实现什么功能，要看配合的拦截器怎么设定拦截的条件
@Target(ElementType.METHOD)////申明这个注解只有方法可以用
@Retention(RetentionPolicy.RUNTIME)//这个注解运行时可以用
public @interface LoginRequired {
//
}
/*
* 这个项目加此注解：可以防止没有登录的时候，拦截，不能查看比如user/setting等等页面*/
