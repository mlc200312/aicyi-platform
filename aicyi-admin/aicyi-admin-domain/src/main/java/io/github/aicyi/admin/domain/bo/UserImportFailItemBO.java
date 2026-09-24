package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.BoBean;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户批量导入失败明细。
 */
@Getter
@Setter
public class UserImportFailItemBO extends BaseBean implements BoBean {

    /** Excel 行号（表头占第 1 行，数据从第 2 行开始） */
    private int rowNo;

    /** 该行用户名（可能为空） */
    private String username;

    /** 失败原因 */
    private String reason;
}
