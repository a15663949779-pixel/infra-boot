package com.example.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.exception.BusinessException;
import com.example.common.security.LoginUser;
import com.example.common.utils.SecurityUtils;
import com.example.system.domain.dto.MenuSaveDTO;
import com.example.system.domain.entity.SysMenu;
import com.example.system.domain.vo.MenuTreeVO;
import com.example.system.mapper.SysMenuMapper;
import com.example.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<MenuTreeVO> getCurrentUserMenuTree() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录");
        }
        return buildMenuTree(baseMapper.selectMenusByUserId(loginUser.getUserId()));
    }

    @Override
    public List<MenuTreeVO> buildMenuTree(List<SysMenu> menus) {
        Map<Long, MenuTreeVO> nodeMap = new LinkedHashMap<>();
        menus.stream()
                .sorted(Comparator.comparing(SysMenu::getParentId).thenComparing(SysMenu::getSort).thenComparing(SysMenu::getId))
                .forEach(menu -> {
                    MenuTreeVO vo = new MenuTreeVO();
                    BeanUtils.copyProperties(menu, vo);
                    nodeMap.put(menu.getId(), vo);
                });
        nodeMap.values().forEach(node -> {
            if (!Objects.equals(node.getParentId(), 0L) && nodeMap.containsKey(node.getParentId())) {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        });
        return nodeMap.values().stream()
                .filter(node -> Objects.equals(node.getParentId(), 0L) || !nodeMap.containsKey(node.getParentId()))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMenu(MenuSaveDTO dto) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menu.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        menu.setMenuType(dto.getMenuType() == null ? 0 : dto.getMenuType());
        menu.setVisible(dto.getVisible() == null ? 1 : dto.getVisible());
        menu.setSort(dto.getSort() == null ? 0 : dto.getSort());
        menu.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuSaveDTO dto) {
        SysMenu menu = getById(dto.getId());
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        BeanUtils.copyProperties(dto, menu);
        updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        removeById(id);
    }
}
