package io.github.aicyi.admin.web.vo;

import io.github.aicyi.common.model.VoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户响应（脱敏：不返回密码等敏感字段）。
 */
@Schema(description = "用户响应（脱敏：不返回密码等敏感字段）")
@Getter
@Setter
public class UserResp extends BaseBean implements VoBean {

    @Schema(description = "用户 ID", example = "1")
    private String id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 状态：1 启用 / 0 禁用
     */
    @Schema(description = "状态：1 启用 / 0 禁用", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "测试账号")
    private String remark;

    /**
     * 是否已修改初始密码
     */
    @Schema(description = "是否已修改初始密码：1 是 / 0 否", example = "1")
    private Integer passwordModified;

    @Schema(description = "创建时间", example = "2026-09-14 10:00:00")
    private String createTime;

    /**
     * 绑定的角色（VO，脱敏）
     */
    @Schema(description = "绑定的角色列表")
    private List<RoleResp> roles;
    @Override
    public String toString() {
        return "UserResp{id=" + id + ", username='" + username + "', nickname='" + nickname
                + "', mobile='***', email='***', status=" + status + ", remark='" + remark
                + "', passwordModified=" + passwordModified + ", createTime='" + createTime
                + "', roles=" + roles + "}";
    }
}
