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

    private Long roleId;

    private String roleName;

    private String description;

    private StatusType status;
}
