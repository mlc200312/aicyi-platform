package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.BoBean;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户导出数据（Service 查询结果 → Excel VO 的中间模型）。
 */
@Getter
@Setter
public class UserExportBO extends BaseBean implements BoBean {

    private String username;

    private String nickname;

    private String mobile;

    private String email;

    private StatusType status;

    private String remark;

    private LocalDateTime createTime;
}
