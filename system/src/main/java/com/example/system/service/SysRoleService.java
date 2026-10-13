package com.example.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.core.PageResult;
import com.example.system.domain.dto.RoleQueryDTO;
import com.example.system.domain.dto.RoleSaveDTO;
import com.example.system.domain.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    List<Long> getMenuIds(Long roleId);

    PageResult<SysRole> selectRolePage(RoleQueryDTO query);

    void saveRole(RoleSaveDTO dto);

    void updateRole(RoleSaveDTO dto);

    void deleteRole(Long id);
}
