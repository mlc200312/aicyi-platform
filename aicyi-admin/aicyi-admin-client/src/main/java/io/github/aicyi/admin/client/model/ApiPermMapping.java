package io.github.aicyi.admin.client.model;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.DtoBean;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 接口权限映射契约（admin → gateway 跨服务返回）。
 *
 * <p>一条映射对应 sys_menu 中配置了 {@code api_path + perm_code} 的按钮级权限项：
 * 命中 {@code apiPath} 模式的请求需持有 {@code permCodes} 中任一权限标识方可放行。
 * {@code apiPath} 支持 {@code {param}} 路径占位与 {@code *} 通配（如 {@code /api/work-order/{id}}），
 * 匹配语义由网关侧统一实现。
 */
@Getter
@Setter
public class ApiPermMapping extends BaseBean implements DtoBean {

    /** 接口路径模式（Ant 风格，支持 {param} 占位符） */
    private String apiPath;

    /** 命中该路径所需权限标识集合（任一命中即可） */
    private Set<String> permCodes;

    public ApiPermMapping() {
    }

    public ApiPermMapping(String apiPath, Set<String> permCodes) {
        this.apiPath = apiPath;
        this.permCodes = permCodes;
    }
}
