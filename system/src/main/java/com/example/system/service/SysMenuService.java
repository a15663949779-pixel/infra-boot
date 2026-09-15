package com.example.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.dto.MenuSaveDTO;
import com.example.system.domain.entity.SysMenu;
import com.example.system.domain.vo.MenuTreeVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<MenuTreeVO> getCurrentUserMenuTree();

    List<MenuTreeVO> buildMenuTree(List<SysMenu> menus);

    void saveMenu(MenuSaveDTO dto);

    void updateMenu(MenuSaveDTO dto);

    void deleteMenu(Long id);
}
