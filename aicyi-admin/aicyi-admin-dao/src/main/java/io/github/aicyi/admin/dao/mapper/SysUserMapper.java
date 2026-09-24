package io.github.aicyi.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aicyi.admin.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
