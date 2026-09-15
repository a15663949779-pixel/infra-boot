package com.example.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.dto.RoleSaveDTO;
import com.example.system.domain.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    List<Long> getMenuIds(Long roleId);

    void saveRole(RoleSaveDTO dto);

    void updateRole(RoleSaveDTO dto);

    void deleteRole(Long id);
}
