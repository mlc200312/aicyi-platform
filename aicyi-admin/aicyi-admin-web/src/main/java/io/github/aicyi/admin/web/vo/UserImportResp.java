package io.github.aicyi.admin.web.vo;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.VoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户批量导入结果响应。
 */
@Schema(description = "用户批量导入结果")
@Getter
@Setter
public class UserImportResp extends BaseBean implements VoBean {

    @Schema(description = "文件内有效数据行数（剔除空行）", example = "10")
    private int totalRows;

    @Schema(description = "导入成功数量", example = "8")
    private int successCount;

    @Schema(description = "导入失败数量", example = "2")
    private int failCount;

    @Schema(description = "失败明细")
    private List<UserImportFailItemResp> failures = new ArrayList<>();

    @Override
    public String toString() {
        return "UserImportResp{totalRows=" + totalRows + ", successCount=" + successCount + ", failCount=" + failCount + "}";
    }
}
