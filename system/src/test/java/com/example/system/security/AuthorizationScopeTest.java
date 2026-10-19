package com.example.system.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.security.LoginUser;
import com.example.system.domain.entity.*;
import com.example.system.mapper.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthorizationScopeTest {
    private final SysRoleMapper roles = mock(SysRoleMapper.class);
    private final SysMenuMapper menus = mock(SysMenuMapper.class);
    private final SysRoleMenuMapper roleMenus = mock(SysRoleMenuMapper.class);
    private final SysUserRoleMapper userRoles = mock(SysUserRoleMapper.class);
    private final AuthorizationScope scope = new AuthorizationScope(roles, menus, roleMenus, userRoles);
    private final Map<Long, SysRole> roleData = new HashMap<>();
    private final Map<Long, SysMenu> menuData = new HashMap<>();
    private final Map<Long, List<Long>> roleMenuData = new HashMap<>();
    private final Map<Long, List<Long>> userRoleData = new HashMap<>();

    @BeforeEach
    void setup() {
        for (Class<?> type : List.of(SysUserRole.class, SysRoleMenu.class)) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), type);
        }
        addRole(1L, "admin", 1, List.of(999L));
        addRole(2L, "operator", 1, List.of(100L, 101L));
        addRole(3L, "sensitive", 1, List.of(999L));
        addRole(4L, "disabled", 0, List.of(100L));
        addMenu(100L, "system:user:list");
        addMenu(101L, "system:user:edit");
        addMenu(999L, "system:secret:edit");
        userRoleData.put(42L, List.of(2L));
        userRoleData.put(77L, List.of(3L));
        userRoleData.put(88L, List.of(2L));
        login(42L);
        when(roles.selectById(anyLong())).thenAnswer(i -> roleData.get(i.<Long>getArgument(0)));
        when(menus.selectByIds(anyCollection())).thenAnswer(i -> i.<Collection<Long>>getArgument(0).stream()
                .map(menuData::get).filter(Objects::nonNull).toList());
        when(roleMenus.selectList(any())).thenAnswer(i -> roleMenuData.getOrDefault(id(i.getArgument(0)), List.of())
                .stream().map(menuId -> { SysRoleMenu link = new SysRoleMenu(); link.setMenuId(menuId); return link; }).toList());
        when(userRoles.selectList(any())).thenAnswer(i -> userRoleData.getOrDefault(id(i.getArgument(0)), List.of())
                .stream().map(roleId -> { SysUserRole link = new SysUserRole(); link.setRoleId(roleId); return link; }).toList());
    }

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void allowsOnlyRolesInsideCurrentGrantScope() { assertDoesNotThrow(() -> scope.checkAssignableRoles(List.of(2L))); }
    @Test void rejectsAdminRoleEvenIfItsMenusAreEmpty() {
        roleMenuData.put(1L, List.of());
        assertThrows(AccessDeniedException.class, () -> scope.checkAssignableRoles(List.of(1L)));
    }
    @Test void rejectsRoleWithAdditionalPermissions() { assertThrows(AccessDeniedException.class, () -> scope.checkAssignableRoles(List.of(3L))); }
    @Test void rejectsDisabledAndUnknownRoles() {
        assertThrows(AccessDeniedException.class, () -> scope.checkAssignableRoles(List.of(4L)));
        assertThrows(AccessDeniedException.class, () -> scope.checkAssignableRoles(List.of(999L)));
    }
    @Test void cannotResetPasswordOrStripRolesOfHigherPrivilegedUser() { assertThrows(AccessDeniedException.class, () -> scope.checkUser(77L, false)); }
    @Test void permitsEditingUserInsideScope() { assertDoesNotThrow(() -> scope.checkUser(88L, false)); }
    @Test void cannotModifySuperAdminOrDeleteSelf() {
        assertThrows(AccessDeniedException.class, () -> scope.checkUser(1L, false));
        assertThrows(AccessDeniedException.class, () -> scope.checkUser(42L, true));
    }
    @Test void roleCannotBeGrantedUnknownOrUnassignedMenus() {
        assertThrows(AccessDeniedException.class, () -> scope.checkMenus(List.of(999L)));
        assertThrows(AccessDeniedException.class, () -> scope.checkMenus(List.of(12345L)));
    }
    @Test void cannotEscalateByChangingMenuPermissionStringOrReservedRoleKey() {
        assertThrows(AccessDeniedException.class, () -> scope.checkPermission("system:secret:edit"));
        assertThrows(AccessDeniedException.class, () -> scope.checkRoleKey("admin"));
    }
    @Test void superAdminCanGrantOutsideOwnMenuScopeButCannotDeleteProtectedObjects() {
        login(1L);
        assertDoesNotThrow(() -> scope.checkAssignableRoles(List.of(3L)));
        assertThrows(AccessDeniedException.class, () -> scope.checkDeleteRole(1L));
        assertThrows(AccessDeniedException.class, () -> scope.checkUser(1L, true));
    }
    @Test void unauthenticatedServiceCallsAreRejected() {
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> scope.checkAssignableRoles(List.of()));
    }

    private long id(LambdaQueryWrapper<?> wrapper) {
        wrapper.getSqlSegment();
        return ((Number) wrapper.getParamNameValuePairs().values().iterator().next()).longValue();
    }
    private void login(Long id) {
        LoginUser user = LoginUser.builder().userId(id).permissions(Set.of("system:user:list", "system:user:edit")).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
    private void addRole(Long id, String key, int status, List<Long> ids) {
        SysRole role = new SysRole(); role.setId(id); role.setRoleKey(key); role.setStatus(status);
        roleData.put(id, role); roleMenuData.put(id, ids);
    }
    private void addMenu(Long id, String permission) {
        SysMenu menu = new SysMenu(); menu.setId(id); menu.setPerms(permission); menuData.put(id, menu);
    }
}
