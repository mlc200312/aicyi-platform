package io.github.aicyi.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aicyi.admin.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联 Mapper。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
