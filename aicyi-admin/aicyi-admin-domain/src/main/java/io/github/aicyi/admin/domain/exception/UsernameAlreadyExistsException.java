package io.github.aicyi.admin.domain.exception;

import io.github.aicyi.common.model.CommonResultCode;
import io.github.aicyi.common.model.exception.BusinessException;

/**
 * 用户名已存在异常（用户名唯一约束）。
 */
public class UsernameAlreadyExistsException extends BusinessException {

    public UsernameAlreadyExistsException(String username) {
        super(CommonResultCode.PARAM_ERROR, "用户名已存在: " + username);
    }
}
