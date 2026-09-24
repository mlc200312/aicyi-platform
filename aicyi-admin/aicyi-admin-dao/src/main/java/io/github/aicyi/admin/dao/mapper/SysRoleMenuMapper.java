package io.github.aicyi.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aicyi.admin.domain.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单权限关联 Mapper。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
