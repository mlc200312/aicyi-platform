package io.github.aicyi.admin.domain.bo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.BoBean;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户导入 Excel 行模型（与导入模板表头一一对应）。
 */
@Getter
@Setter
@ColumnWidth(18)
public class UserImportRow extends BaseBean implements BoBean {

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("昵称")
    private String nickname;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("初始密码")
    private String password;

    @Override
    public String toString() {
        return "UserImportRow{username='" + username + "', nickname='" + nickname + "', mobile='***', email='***', password='***'}";
    }
}
