package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.common.model.BoBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 编辑角色参数。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEditBO implements BoBean {

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 状态
     */
    private StatusType status;
}
