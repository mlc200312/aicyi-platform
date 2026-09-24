package io.github.aicyi.admin.domain.convert;

import io.github.aicyi.admin.domain.bo.UserCreateBO;
import io.github.aicyi.admin.domain.bo.UserImportFailItemBO;
import io.github.aicyi.admin.domain.bo.UserImportRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户导入转换器：Excel 行模型 ↔ 导入相关业务对象。
 */
@Mapper
public interface UserImportConverter {

    UserImportConverter INSTANCE = Mappers.getMapper(UserImportConverter.class);

    /** Excel 行 → 新增用户 BO（角色留空，导入用户不绑角色） */
    @Mapping(target = "remark", ignore = true)
    @Mapping(target = "roleIds", ignore = true)
    UserCreateBO toUserCreateBO(UserImportRow row);

    /** 组装导入失败项（行号 + 行模型 + 原因） */
    @Mapping(target = "username", source = "row.username")
    UserImportFailItemBO toFailItemBO(int rowNo, UserImportRow row, String reason);
}
