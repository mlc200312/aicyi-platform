package io.github.aicyi.admin.domain.exception;

import io.github.aicyi.common.model.CommonResultCode;
import io.github.aicyi.common.model.exception.BusinessException;

/**
 * 登录失败异常（账号或密码错误），不区分具体原因，避免账号枚举。
 */
public class LoginFailException extends BusinessException {

    public LoginFailException() {
        super(CommonResultCode.BUSINESS_ERROR, "用户名或密码错误");
    }
}
