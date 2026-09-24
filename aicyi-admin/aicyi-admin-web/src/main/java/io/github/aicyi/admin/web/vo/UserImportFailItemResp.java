package io.github.aicyi.admin.web.vo;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.VoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户批量导入失败明细响应。
 */
@Schema(description = "用户批量导入失败明细")
@Getter
@Setter
public class UserImportFailItemResp extends BaseBean implements VoBean {

    @Schema(description = "Excel 中的行号（表头占第 1 行，数据从第 2 行开始）", example = "3")
    private int rowNo;

    @Schema(description = "该行的用户名（可能为空）", example = "zhangsan")
    private String username;

    @Schema(description = "失败原因", example = "用户名已存在")
    private String reason;

    @Override
    public String toString() {
        return "UserImportFailItemResp{rowNo=" + rowNo + ", username='" + username + "', reason='" + reason + "'}";
    }
}
