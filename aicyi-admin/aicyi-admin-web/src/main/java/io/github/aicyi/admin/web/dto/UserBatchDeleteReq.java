package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.DtoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户批量删除请求。
 */
@Schema(description = "用户批量删除请求")
@Getter
@Setter
public class UserBatchDeleteReq extends BaseBean implements DtoBean {

    @Schema(description = "待删除用户 ID 集合（admin 账号自动跳过）", example = "[2,3]")
    @NotEmpty(message = "用户 ID 集合不能为空")
    @Size(max = 100, message = "单次最多删除 100 个用户")
    private List<Long> ids;
}
