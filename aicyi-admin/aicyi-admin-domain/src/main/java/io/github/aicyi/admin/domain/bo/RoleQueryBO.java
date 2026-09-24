package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.common.model.BoBean;
import io.github.aicyi.common.model.PageParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 角色分页查询参数（名称/标识模糊搜索）。
 */
@Getter
@Setter
@NoArgsConstructor
public class RoleQueryBO extends PageParam implements BoBean {

    /** 角色名称（模糊匹配，可选） */
    private String roleName;

    /** 角色标识（模糊匹配，可选） */
    private String roleKey;
}
