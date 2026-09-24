package io.github.aicyi.admin.web.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.VoBean;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户导出 Excel 行模型（EasyExcel 表头，列顺序即导出列顺序）。
 */
@Getter
@Setter
@ColumnWidth(18)
public class UserExportResp extends BaseBean implements VoBean {

    /**
     * 用户名
     */
    @ExcelProperty("用户名")
    private String username;

    /**
     * 昵称
     */
    @ExcelProperty("昵称")
    private String nickname;

    /**
     * 手机号
     */
    @ExcelProperty("手机号")
    private String mobile;

    /**
     * 邮箱
     */
    @ExcelProperty("邮箱")
    @ColumnWidth(26)
    private String email;

    /**
     * 状态（启用 / 禁用）
     */
    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String status;

    /**
     * 备注
     */
    @ExcelProperty("备注")
    @ColumnWidth(28)
    private String remark;

    /**
     * 创建时间（yyyy-MM-dd HH:mm:ss）
     */
    @ExcelProperty("创建时间")
    @ColumnWidth(22)
    private String createTime;

    /**
     * 手机号 / 邮箱为敏感字段，toString 脱敏输出
     */
    @Override
    public String toString() {
        return "UserExportResp{username='" + username + "', nickname='" + nickname
                + "', mobile='***', email='***', status='" + status
                + "', remark='" + remark + "', createTime='" + createTime + "'}";
    }
}
