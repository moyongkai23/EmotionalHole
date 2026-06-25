package com.myk.emotionalHole.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

import java.nio.file.Paths;

/**
 * Web MVC配置类
 *
 * 配置：CORS跨域、拦截器路径、静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Autowired
    private UserAuthInterceptor userAuthInterceptor;

    @Value("${file.upload.path:./uploads/images}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/images/}")
    private String urlPrefix;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 管理员接口拦截器
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns(
                        "/admin/**", "/content/admin/**",
                        "/dashboard/overview", "/dashboard/user-trend", "/dashboard/post-trend",
                        "/dashboard/emotion-distribution", "/dashboard/activity-trend", "/dashboard/interaction-stats",
                        "/report/list", "/report/detail/**", "/report/handle", "/report/stats", "/report/*/reset",
                        "/comment/admin/**",
                        "/content/status/**", "/comment/status/**",
                        "/emotion-keyword/**"
                )
                .excludePathPatterns("/admin/login");

        // 用户接口拦截器 — 保护需要登录的接口
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns(
                        "/content/publish", "/content/update", "/content/delete/**", "/content/my-posts",
                        "/comment/publish", "/comment/delete/**", "/comment/my-comments",
                        "/like/add", "/like/remove/**", "/like/my-likes",
                        "/hug/add", "/hug/remove/**", "/hug/my-hugs",
                        "/message/**",
                        "/ai/chat", "/ai/history", "/ai/reply",
                        "/user/statistics", "/user/info", "/user/anonymousId", "/user/avatar",
                        "/report/submit",
                        "/recommendation/behavior/**"
                )
                .excludePathPatterns(
                        "/user/login",
                        "/content/list/**", "/content/detail/**", "/content/search",
                        "/content/batch-interaction",
                        "/comment/list/**", "/comment/batch-count",
                        "/like/status/**", "/like/count/**", "/like/batch-status", "/like/batch-count",
                        "/hug/status/**", "/hug/count/**", "/hug/batch-status", "/hug/batch-count",
                        "/hot-ranking/**",
                        "/recommendation/personalized/**", "/recommendation/related/**", "/recommendation/reason",
                        "/category/**",
                        "/announcement/**",
                        "/upload/image"
                );
    }

    /**
     * 配置静态资源访问
     * 将 /images/** 映射到文件上传目录
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        // 构建完整的上传目录绝对路径
        String absolutePath = Paths.get(projectRoot, uploadPath.replace("./", "")).toAbsolutePath().toString();
        // 将URL路径 /images/** 映射到本地文件系统路径
        String filePath = "file:" + absolutePath.replace("\\", "/");
        if (!filePath.endsWith("/")) {
            filePath += "/";
        }
        registry.addResourceHandler(urlPrefix + "**")
                .addResourceLocations(filePath)
                .setCachePeriod(0);
    }
}
