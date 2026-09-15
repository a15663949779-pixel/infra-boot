package com.example.system.service.impl;

import com.example.common.security.LoginUser;
import com.example.system.domain.entity.SysUser;
import com.example.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .status(user.getStatus())
                .roles(userService.getRoleKeys(user.getId()))
                .permissions(userService.getPermissions(user.getId()))
                .build();
    }
}
