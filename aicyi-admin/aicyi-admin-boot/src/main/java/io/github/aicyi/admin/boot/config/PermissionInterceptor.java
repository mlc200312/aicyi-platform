package io.github.aicyi.admin.boot.config;

import io.github.aicyi.admin.domain.constant.SysConstants;
import io.github.aicyi.admin.domain.exception.ForbiddenException;
import io.github.aicyi.admin.service.system.PermissionService;
import io.github.aicyi.common.model.exception.UnauthorizedException;
import io.github.aicyi.common.util.context.CurrentContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 接口权限细粒度拦截。
 *
 * <p>规则：
 * <ul>
 *     <li>认证接口（/api/auth/**）放行（登录 / 刷新免权限）；</li>
 *     <li>超级管理员 admin 放行，不参与权限拦截校验（拥有系统全部权限）；</li>
 *     <li>按菜单 {@code api_path} 匹配当前请求路径所需权限标识，未配置权限的接口放行；</li>
 *     <li>已配置权限的接口要求当前用户拥有任一对应权限标识，否则 40300 拒绝。</li>
 * </ul>
 *
 * <p>fail-closed：当前上下文无 userId（未经过 {@code AuthInterceptor} 鉴权）时拒绝，
 * 避免拦截器顺序异常导致鉴权被绕过。
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;

    public PermissionInterceptor(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String uri = request.getRequestURI();

        // 认证接口放行（登录 / 刷新等免权限接口）
        if (uri.startsWith(SysConstants.AUTH_API_PREFIX)) {
            return true;
        }

        // fail-closed：未经鉴权进入说明拦截器顺序异常或配置错误，拒绝而非放行
        String userIdStr = CurrentContextHolder.getUserId();
        if (userIdStr == null) {
            throw new UnauthorizedException();
        }

        // 超级管理员不参与权限拦截
        if (SysConstants.ADMIN_USERNAME.equals(CurrentContextHolder.getUsername())) {
            return true;
        }

        // 未配置权限的接口放行
        Set<String> permCodes = permissionService.findPermCodesByApiPath(uri);
        if (permCodes.isEmpty()) {
            return true;
        }

        Long userId = Long.valueOf(userIdStr);
        for (String permCode : permCodes) {
            if (permissionService.hasPermission(userId, permCode)) {
                return true;
            }
        }
        throw new ForbiddenException("无权限访问: " + uri);
    }
}
