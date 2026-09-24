package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.common.model.BoBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 新增用户参数。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateBO implements BoBean {

    /**
     * 用户名（唯一，创建后不可修改）
     */
    private String username;

    /**
     * 初始密码（6-32 位）
     */
    private String password;

    /**
     * 昵称（为空时默认取用户名）
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
     * 备注
     */
    private String remark;

    /**
     * 绑定的角色 ID 列表
     */
    private List<Long> roleIds;
}
