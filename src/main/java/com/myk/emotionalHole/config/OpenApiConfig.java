package com.myk.emotionalHole.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置类
 * 配置API文档的基本信息
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "情感树洞API",
                description = "情感树洞后端API文档",
                version = "1.0.0",
                contact = @Contact(
                        name = "Myk",
                        email = "myk@example.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = @Server(
                url = "/",
                description = "本地开发环境"
        )
)
public class OpenApiConfig {
}
