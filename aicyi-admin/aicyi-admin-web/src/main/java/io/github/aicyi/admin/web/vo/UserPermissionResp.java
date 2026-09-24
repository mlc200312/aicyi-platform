package io.github.aicyi.admin.web.vo;

import io.github.aicyi.common.model.VoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户单独授权明细响应（脱敏：不返回 deleted/version 等内部字段）。
 */
@Schema(description = "用户单独授权明细")
@Getter
@Setter
public class UserPermissionResp extends BaseBean implements VoBean {

    @Schema(description = "授权记录 ID", example = "1")
    private String id;

    @Schema(description = "用户 ID", example = "1")
    private String userId;

    @Schema(description = "权限标识", example = "system:user:add")
    private String permCode;

    @Schema(description = "类型：1 追加 / 2 扣除", example = "1")
    private Integer permType;

    @Schema(description = "创建时间", example = "2026-09-14 10:00:00")
    private String createTime;
}
