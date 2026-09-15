package com.example.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.dto.UserSaveDTO;
import com.example.system.domain.entity.SysUser;
import com.example.system.domain.vo.UserInfoVO;

import java.util.List;
import java.util.Set;

public interface SysUserService extends IService<SysUser> {

    SysUser getByUsername(String username);

    List<String> getRoleKeys(Long userId);

    Set<String> getPermissions(Long userId);

    List<Long> getRoleIds(Long userId);

    UserInfoVO getCurrentUserInfo();

    void saveUser(UserSaveDTO dto);

    void updateUser(UserSaveDTO dto);

    void deleteUser(Long id);
}
