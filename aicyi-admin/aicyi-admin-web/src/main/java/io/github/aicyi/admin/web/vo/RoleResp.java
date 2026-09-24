package io.github.aicyi.admin.web.vo;

import io.github.aicyi.common.model.VoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色响应（脱敏：不返回 deleted/version 等内部字段）。
 */
@Schema(description = "角色响应")
@Getter
@Setter
public class RoleResp extends BaseBean implements VoBean {

    @Schema(description = "角色 ID", example = "1")
    private Long id;

    @Schema(description = "角色名称", example = "运营")
    private String roleName;

    @Schema(description = "角色标识", example = "operator")
    private String roleKey;

    @Schema(description = "角色描述", example = "负责日常运营管理")
    private String description;

    @Schema(description = "状态：1 启用 / 0 禁用", example = "1")
    private Integer status;

    @Schema(description = "内置角色：1 是 / 0 否", example = "0")
    private Integer builtin;

    @Schema(description = "创建时间", example = "2026-09-14 10:00:00")
    private String createTime;

    @Schema(description = "更新时间", example = "2026-09-14 10:00:00")
    private String updateTime;
}
