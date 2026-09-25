package io.github.aicyi.admin.client;

import io.github.aicyi.admin.client.model.ApiPermMapping;
import io.github.aicyi.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;

/**
 * aicyi-admin 权限中心 Feign 客户端：跨服务获取用户有效权限集与接口权限映射。
 *
 * <p>主要消费方为 aicyi-gateway 权限过滤器（WebFlux 响应式，实际经负载均衡 WebClient 调用，
 * 本契约仅作端点与数据结构约定）；其他服务做服务端兜底校验时亦可依赖本模块以 Feign 调用。
 *
 * <p>服务端实现标记 {@code @IgnoreAuth} 并由网关 {@code internal-only-paths} 拒绝外部经网关访问，
 * 仅限内网服务间调用（与 {@code SystemUserClient} 认证端点同一信任模型）。
 */
@FeignClient(name = "aicyi-admin", path = "/api/system/perm", contextId = "permissionClient")
public interface PermissionClient {

    /**
     * 获取用户有效权限集（角色权限 ∪ 单独追加 − 单独扣除，admin 侧带 Redis 缓存）。
     *
     * @param userId 用户 ID
     * @return 统一响应，{@code data} 为权限标识集合（如 {@code system:user:list}）
     */
    @GetMapping("/effective/{userId}")
    Result<Set<String>> getEffectivePermissions(@PathVariable("userId") Long userId);

    /**
     * 获取接口权限映射全量清单（api_path → perm_code，来自 sys_menu 按钮级配置）。
     *
     * @return 统一响应，{@code data} 为映射列表；未配置权限的接口不在清单内（语义为放行）
     */
    @GetMapping("/api-mappings")
    Result<List<ApiPermMapping>> getApiPermMappings();
}
