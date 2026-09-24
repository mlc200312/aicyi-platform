package io.github.aicyi.admin.client;

import io.github.aicyi.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * aicyi-system 用户中心 Feign 客户端：跨服务同步获取用户昵称快照。
 *
 * <p>用于工单提交 / 处理时冗余 {@code submitter_name} / {@code handler_name}，避免列表跨库 JOIN。
 * 服务名 {@code aicyi-system} 由 Nacos 注册发现解析；调用失败由业务侧降级（回退登录上下文用户名）。
 *
 * <p>注意：绞杀者并行期 aicyi-system 尚未拆分上线，此接口先定义契约，联调时对接真实端点。
 */
@FeignClient(name = "aicyi-system", path = "/api/system/user", contextId = "systemUserClient")
public interface SystemUserClient {

    /**
     * 查询用户昵称。
     *
     * @param userId 用户 ID
     * @return 统一响应，{@code data} 为昵称（用户不存在时 data 为空）
     */
    @GetMapping("/{userId}/nickname")
    Result<String> getNickname(@PathVariable("userId") Long userId);
}
