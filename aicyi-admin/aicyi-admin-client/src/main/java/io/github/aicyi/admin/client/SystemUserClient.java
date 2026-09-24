package io.github.aicyi.admin.client;

import io.github.aicyi.admin.client.model.ChangePasswordReq;
import io.github.aicyi.admin.client.model.UserAuthInfo;
import io.github.aicyi.admin.client.model.UsernamePasswordReq;
import io.github.aicyi.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * aicyi-admin 用户中心 Feign 客户端：跨服务获取用户信息与认证能力。
 *
 * <p>服务名 {@code aicyi-admin} 由 Nacos 注册发现解析。业务侧（工单等）调用失败时降级
 * （回退登录上下文用户名）；认证侧（auth）依赖本接口完成登录校验 / 改密 / 忘记密码定位用户，
 * 密码校验与存储均收敛在 admin 用户域。
 */
@FeignClient(name = "aicyi-admin", path = "/api/system/user", contextId = "systemUserClient")
public interface SystemUserClient {

    /**
     * 查询用户昵称。
     *
     * @param userId 用户 ID
     * @return 统一响应，{@code data} 为昵称（用户不存在时 data 为空）
     */
    @GetMapping("/nickname/{userId}")
    Result<String> getNickname(@PathVariable("userId") Long userId);

    /**
     * 凭证校验（登录）：用户域内完成状态校验与 BCrypt 密码比对。
     *
     * <p>用户不存在或密码错误统一返回业务失败（防账号枚举）；账号禁用返回未授权失败。
     * 成功时返回不含密码哈希的认证信息。
     *
     * @param req 用户名 + 明文密码
     * @return 认证信息（失败时 data 为 null）
     */
    @PostMapping("/auth/verify")
    Result<UserAuthInfo> verifyCredentials(@RequestBody UsernamePasswordReq req);

    /**
     * 修改密码：校验原密码后更新（BCrypt），置 {@code passwordModified=TRUE}。
     *
     * @param req 用户 ID + 原密码 + 新密码
     * @return 成功返回 data=null 的 Result
     */
    @PutMapping("/auth/password")
    Result<Void> changePassword(@RequestBody ChangePasswordReq req);

    /**
     * 按用户名查询未删除用户（忘记密码定位用户用）。
     *
     * @param username 用户名
     * @return 认证信息（用户不存在时 data 为空）
     */
    @GetMapping("/auth-info/{username}")
    Result<UserAuthInfo> getByUsername(@PathVariable("username") String username);
}
