package com.myk.emotionalHole.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import org.springframework.lang.NonNull;

/**
 * RestTemplate配置类
 */
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 配置错误处理器
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(@NonNull ClientHttpResponse response) throws IOException {
                // 这里可以根据需要自定义错误处理逻辑
                // 例如：记录日志、转换异常类型等
                super.handleError(response);
            }
        });
        
        return restTemplate;
    }
}
