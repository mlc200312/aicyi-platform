package io.github.aicyi.admin.domain.exception;

import io.github.aicyi.common.model.CommonResultCode;
import io.github.aicyi.common.model.exception.BusinessException;

/**
 * 角色仍被用户绑定异常（已绑定用户的角色禁止删除，避免用户权限失效）。
 */
public class RoleInUseException extends BusinessException {

    public RoleInUseException(String roleName) {
        super(CommonResultCode.BUSINESS_ERROR, "角色仍被用户绑定，禁止删除: " + roleName);
    }
}
