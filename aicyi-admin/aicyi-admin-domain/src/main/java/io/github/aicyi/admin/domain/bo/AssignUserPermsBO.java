package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.common.model.BoBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * 用户单独授权参数（追加/扣除权限标识，整体覆盖）。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserPermsBO implements BoBean {

    private Long userId;

    /** 追加权限标识集合 */
    private Set<String> addCodes;

    /** 扣除权限标识集合 */
    private Set<String> removeCodes;
}
