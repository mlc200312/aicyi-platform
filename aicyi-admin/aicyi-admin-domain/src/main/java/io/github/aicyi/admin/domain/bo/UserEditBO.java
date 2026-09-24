package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.common.model.BoBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 编辑用户参数（用户名不可修改）。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEditBO implements BoBean {

    private Long userId;

    private String nickname;

    private String mobile;

    private String email;

    private StatusType status;

    private String remark;

    /** 绑定的角色 ID 列表 */
    private List<Long> roleIds;
}
