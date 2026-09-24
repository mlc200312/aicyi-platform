package io.github.aicyi.admin.service.system;

import io.github.aicyi.admin.domain.constant.SysConstants;
import io.github.aicyi.common.util.json.JsonUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限集缓存（Redis）：{@code rbac:perms:{userId}} → 权限标识集合 JSON。
 *
 * <p>满足需求 6.2「权限数据缓存处理，减少数据库频繁查询」；
 * 角色授权 / 用户单独授权 / 角色状态 / 用户禁用等变更后由业务显式失效对应缓存。
 */
@Component
public class PermissionCache {

    /**
     * 缓存 TTL：30 分钟（兜底过期，正常由变更操作主动失效）
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;

    public PermissionCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 读取用户权限缓存；未命中返回 null
     */
    @SuppressWarnings("unchecked")
    public Set<String> get(Long userId) {
        String value = redisTemplate.opsForValue().get(key(userId));
        if (value == null || value.isEmpty()) {
            return null;
        }
        List<String> perms = JsonUtils.getInstance().fromJson(value, List.class);
        return perms == null ? null : new LinkedHashSet<>(perms);
    }

    /**
     * 写入用户权限缓存
     */
    public void put(Long userId, Set<String> perms) {
        redisTemplate.opsForValue().set(key(userId), JsonUtils.getInstance().toJson(List.copyOf(perms)), CACHE_TTL);
    }

    /**
     * 失效用户权限缓存（权限变更后调用，下次请求实时重算）
     */
    public void evict(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private static String key(Long userId) {
        return SysConstants.PERM_CACHE_KEY_PREFIX + userId;
    }
}
