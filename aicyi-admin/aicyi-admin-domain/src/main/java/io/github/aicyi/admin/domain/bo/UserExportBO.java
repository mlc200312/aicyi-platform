package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.BoBean;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户导出业务对象（Service 层查询结果 → Controller 层 Excel VO 的中间模型）。
 */
@Getter
@Setter
public class UserExportBO extends BaseBean implements BoBean {

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态：启用 / 禁用
     */
    private StatusType status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
