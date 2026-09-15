package com.example.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.system.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    Set<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
