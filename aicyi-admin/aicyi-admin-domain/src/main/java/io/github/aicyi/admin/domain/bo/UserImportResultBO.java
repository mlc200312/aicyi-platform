package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.BoBean;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户批量导入结果业务对象。
 */
@Getter
@Setter
public class UserImportResultBO extends BaseBean implements BoBean {

    /**
     * 文件内有效数据行数（剔除空行）
     */
    private int totalRows;

    /**
     * 导入成功数量
     */
    private int successCount;

    /**
     * 导入失败数量
     */
    private int failCount;

    /**
     * 失败明细（行号从 2 开始：表头占第 1 行）
     */
    private List<UserImportFailItemBO> failures = new ArrayList<>();
}
