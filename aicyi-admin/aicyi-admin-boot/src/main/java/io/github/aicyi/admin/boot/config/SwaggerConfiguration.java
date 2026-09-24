package io.github.aicyi.admin.boot.config;

import io.github.aicyi.common.logging.Logger;
import io.github.aicyi.common.logging.LoggerFactory;
import io.github.aicyi.common.util.system.SystemUtils;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

    /**
     * 安全方案名称：即 {@code components.securitySchemes} 的 key，
     * UI 的 Authorize / Token 输入框按此名索引，{@link SecurityRequirement} 也引用此名
     */
    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * 鉴权头名称，与 {@code AuthInterceptor} 读取的 {@code HttpHeaders.AUTHORIZATION} 一致
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Bean
    public OpenAPI aicyiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("接口文档")
                        .description("接口文档示例")
                        .contact(new Contact()
                                .name("Leon Min")
                                .email("mlc200312@163.com"))
                        .version("1.0"))
                // 声明 Bearer JWT 安全方案：官方 Swagger UI 据此渲染 Authorize 按钮，
                // 自研 UI 据此渲染 Token 输入框。type=HTTP + scheme=bearer 时 in/name 不适用，不可设置
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录后获得的 accessToken，UI 发送请求时自动补全 Bearer 前缀")));
    }

    @EventListener
    public void printApiDocUrls(ApplicationReadyEvent event) {
        if (!(event.getApplicationContext() instanceof WebServerApplicationContext webContext)) {
            return;
        }
        WebServer webServer = webContext.getWebServer();
        // 非 Web 容器（如测试切片）或容器尚未启动时 getPort() 返回 -1，此时不打印
        if (webServer == null || webServer.getPort() < 0) {
            return;
        }
        int port = webServer.getPort();
        String ipAddress = SystemUtils.getIpAddress();
        log.info("Swagger ui 'https://{}:{}/swagger-ui/index.html'!", ipAddress, port);
        log.info("Aicyi   ui 'https://{}:{}/apidoc/index.html'!", ipAddress, port);
    }

    @Bean
    public OperationCustomizer bearerSecurityOperationCustomizer() {
        return bearerSecurityCustomizer();
    }

    /**
     * 无状态实现，由上面的 {@code @Bean}（服务默认文档）与下面各分组共用，避免同一逻辑写两份
     *
     * <p>说明：接口方法上以 {@code @Parameter(name="Authorization", in=HEADER)} 显式声明令牌参数时，
     * 不再剔除该参数（保留参数表展示），并同时附加 {@code bearerAuth} security 引用，
     * 使 Swagger UI 既显示 Authorization 头字段也可用 Authorize 按钮。
     */
    private static OperationCustomizer bearerSecurityCustomizer() {
        return (operation, handlerMethod) -> {
            List<Parameter> parameters = operation.getParameters();
            if (parameters == null || parameters.isEmpty()) {
                return operation;
            }
            boolean hasAuthParam = parameters.stream()
                    .anyMatch(p -> AUTHORIZATION_HEADER.equalsIgnoreCase(p.getName())
                            && ParameterIn.HEADER.toString().equalsIgnoreCase(p.getIn()));
            if (hasAuthParam) {
                operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
            }
            return operation;
        };
    }

    /**
     * 全量分组：默认展示，覆盖整个 web 层
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("全部接口")
                .packagesToScan("io.github.aicyi.admin.web.controller")
                .addOperationCustomizer(bearerSecurityCustomizer())
                .build();
    }

    /**
     * 授权与验证码分组：{@code @IgnoreAuth} 的公开接口，调试时无需 Token
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("授权与验证码")
                .pathsToMatch("/api/auth/**")
                .addOperationCustomizer(bearerSecurityCustomizer())
                .build();
    }
}
