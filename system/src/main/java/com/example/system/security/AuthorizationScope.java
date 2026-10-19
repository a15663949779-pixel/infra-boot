package com.example.system.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.security.LoginUser;
import com.example.common.utils.SecurityUtils;
import com.example.system.domain.entity.*;
import com.example.system.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.*;

/** Grant boundaries are checked against persisted role/menu associations, not submitted IDs. */
@Component
@RequiredArgsConstructor
public class AuthorizationScope {
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public void checkUser(Long userId, boolean deleting) {
        LoginUser actor = actor();
        if (deleting && (Objects.equals(userId, 1L) || Objects.equals(userId, actor.getUserId()))) {
            deny();
        }
        if (isAdmin(actor)) return;
        if (Objects.equals(userId, 1L)) deny();
        List<Long> roles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)).stream().map(SysUserRole::getRoleId).toList();
        for (Long id : roles) checkRole(id);
    }

    public void checkAssignableRoles(List<Long> roleIds) {
        actor();
        if (roleIds == null) return;
        for (Long id : new HashSet<>(roleIds)) {
            SysRole role = roleMapper.selectById(id);
            if (role == null || !Objects.equals(role.getStatus(), 1)) deny();
            checkRole(id);
        }
    }

    public void checkRole(Long roleId) {
        LoginUser actor = actor();
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) deny();
        if (isAdmin(actor)) return;
        if (Objects.equals(roleId, 1L) || "admin".equalsIgnoreCase(role.getRoleKey())) deny();
        checkMenus(menuIds(roleId));
    }

    public void checkRoleKey(String key) {
        if (!isAdmin(actor()) && "admin".equalsIgnoreCase(key)) deny();
    }

    public void checkDeleteRole(Long id) {
        if (Objects.equals(id, 1L)) deny();
        checkRole(id);
    }

    public void checkMenus(List<Long> ids) {
        LoginUser actor = actor();
        if (ids == null || ids.isEmpty()) return;
        Set<Long> requested = new HashSet<>(ids);
        if (requested.contains(null)) deny();
        List<SysMenu> menus = menuMapper.selectByIds(requested);
        if (menus.size() != requested.size()) deny();
        if (isAdmin(actor)) return;
        Set<Long> available = new HashSet<>();
        List<SysUserRole> links = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, actor.getUserId()));
        for (SysUserRole link : links) {
            SysRole role = roleMapper.selectById(link.getRoleId());
            if (role != null && Objects.equals(role.getStatus(), 1)) available.addAll(menuIds(role.getId()));
        }
        if (!available.containsAll(requested)) deny();
        for (SysMenu menu : menus) checkPermission(menu.getPerms());
    }

    public void checkPermission(String permission) {
        LoginUser actor = actor();
        if (isAdmin(actor) || permission == null || permission.isBlank()) return;
        if (actor.getPermissions() == null || !actor.getPermissions().contains(permission.trim())) deny();
    }

    private List<Long> menuIds(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)).stream().map(SysRoleMenu::getMenuId).toList();
    }

    private LoginUser actor() {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) throw new AccessDeniedException("未登录");
        return user;
    }

    private boolean isAdmin(LoginUser user) {
        return Objects.equals(user.getUserId(), 1L);
    }

    private void deny() {
        throw new AccessDeniedException("不能操作或授予超出自身范围的用户、角色和权限");
    }
}
