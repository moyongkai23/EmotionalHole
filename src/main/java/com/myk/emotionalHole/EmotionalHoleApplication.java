package com.myk.emotionalHole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot 应用启动类
 *
 * @SpringBootApplication 包含自动配置、组件扫描、配置加载
 * @EnableTransactionManagement 开启声明式事务管理
 * @EnableScheduling 开启定时任务支持（热榜刷新、推荐更新等）
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class EmotionalHoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmotionalHoleApplication.class, args);
    }

}
