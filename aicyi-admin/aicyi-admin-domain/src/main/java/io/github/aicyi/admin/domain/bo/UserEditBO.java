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

    /**
     * 用户 ID
     */
    private Long userId;

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
     * 状态
     */
    private StatusType status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 绑定的角色 ID 列表
     */
    private List<Long> roleIds;
}
