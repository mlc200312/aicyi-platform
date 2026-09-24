package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.DtoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户导出查询条件（与用户分页查询同字段，但不含分页参数——导出始终为全量匹配结果）。
 */
@Getter
@Setter
public class UserExportReq extends BaseBean implements DtoBean {

    @Schema(description = "用户名（模糊匹配）", example = "admin")
    @Size(max = 64, message = "用户名长度不能超过 64 位")
    private String username;

    @Schema(description = "状态：1 启用 / 0 禁用，不传查全部", example = "1")
    private Integer status;

    @Schema(description = "创建时间范围-起（yyyy-MM-dd HH:mm:ss）", example = "2026-01-01 00:00:00")
    private String beginTime;

    @Schema(description = "创建时间范围-止（yyyy-MM-dd HH:mm:ss）", example = "2026-12-31 23:59:59")
    private String endTime;
}
