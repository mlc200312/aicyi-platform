package io.github.aicyi.admin.domain.exception;

import io.github.aicyi.common.model.CommonResultCode;
import io.github.aicyi.common.model.exception.BusinessException;

/**
 * 账号被禁用异常（禁用用户无法登录）。
 */
public class UserDisabledException extends BusinessException {

    public UserDisabledException(String username) {
        super(CommonResultCode.UNAUTHORIZED, "账号已禁用: " + username);
    }
}
