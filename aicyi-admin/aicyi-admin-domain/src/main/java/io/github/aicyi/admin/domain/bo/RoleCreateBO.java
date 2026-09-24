package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.common.model.BoBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 新增角色参数。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateBO implements BoBean {

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识（唯一）
     */
    private String roleKey;

    /**
     * 角色描述
     */
    private String description;
}
