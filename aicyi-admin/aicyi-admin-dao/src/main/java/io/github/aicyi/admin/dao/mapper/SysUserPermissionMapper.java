package io.github.aicyi.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aicyi.admin.domain.entity.SysUserPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户单独授权 Mapper。
 */
@Mapper
public interface SysUserPermissionMapper extends BaseMapper<SysUserPermission> {
}
