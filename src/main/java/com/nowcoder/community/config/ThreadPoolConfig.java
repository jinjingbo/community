package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/7/7 18:34
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}