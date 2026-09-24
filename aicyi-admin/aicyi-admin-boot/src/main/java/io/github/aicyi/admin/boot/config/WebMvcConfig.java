package io.github.aicyi.admin.boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册接口权限拦截器。
 *
 * <p>身份鉴权拦截器由 {@code @EnableMidwareWeb} 提供（AuthInterceptor，拦截器 order 默认 0，
 * 校验 Bearer Token 并写入用户上下文）；本配置注册的 PermissionInterceptor 显式
 * {@code order(1)}，保证在同一拦截器链中 AuthInterceptor 先行执行。
 * 拦截器内部仍 fail-closed，顺序异常也不会放行未鉴权请求。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    public WebMvcConfig(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/webjars/**",
                        "/swagger-ui/**",
                        "/apidoc/**",
                        "/api-doc.html",
                        "/v3/api-docs/**")
                .order(1);
    }
}
