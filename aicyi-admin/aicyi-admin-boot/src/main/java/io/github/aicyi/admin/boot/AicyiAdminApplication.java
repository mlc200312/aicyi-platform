package io.github.aicyi.admin.boot;

import io.github.aicyi.middleware.web.annotation.EnableMidwareWeb;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Spring Boot 启动类（aicyi-admin RBAC 权限管理微服务）。
 *
 * <p>并行期 {@code enableAuth=false}：登录签发由 aicyi-auth 接管，身份由 aicyi-gateway
 * 统一校验 JWT 后以 {@code X-User-Id / X-Username} 可信头透传，服务内不再二次验签。
 *
 * <p>配套 {@code aicyi.token.enabled=true} 仍装配 JwtRefreshAuthenticationTokenService：
 * 供 {@code SessionRevoker} 吊销指定用户全部会话（与 aicyi-auth 共用同一 Redis token 注册表）。
 *
 * <p>{@code @MapperScan} 额外扫描 infra 内置 Mapper（操作日志 db 存储等）。
 */
@SpringBootApplication(scanBasePackages = {"io.github.aicyi.admin"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@MapperScan(basePackages = {"io.github.aicyi.admin.dao.mapper", "io.github.aicyi.infra.mybatisplus.mapper"},
        annotationClass = Mapper.class)
@EnableMidwareWeb(
        enableAuth = false,
        excludePathPatterns = {
                "/webjars/**",
                "/swagger-ui/**",
                "/apidoc/**",
                "/api-doc.html",
                "/v3/api-docs/**"
        })
public class AicyiAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AicyiAdminApplication.class, args);
    }
}
