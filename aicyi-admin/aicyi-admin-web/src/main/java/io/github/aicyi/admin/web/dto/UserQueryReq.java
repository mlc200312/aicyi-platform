package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.middleware.web.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户分页查询请求。
 *
 * <p>分页字段（page/size）继承 {@link PageRequest}（JSR-303 强校验，size 上限 500）。
 */
@Schema(description = "用户分页查询请求")
@Getter
@Setter
public class UserQueryReq extends PageRequest implements DtoBean {

    @Schema(description = "用户名（模糊匹配）", example = "zhang")
    private String username;

    @Schema(description = "状态：1 启用 / 0 禁用", example = "1")
    private Integer status;

    @Schema(description = "创建时间起始（格式：yyyy-MM-dd HH:mm:ss）", example = "2026-09-01 00:00:00")
    private String beginTime;

    @Schema(description = "创建时间结束（格式：yyyy-MM-dd HH:mm:ss）", example = "2026-09-30 23:59:59")
    private String endTime;
}
