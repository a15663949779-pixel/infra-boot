package com.example.system.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer menuType;
    private Integer visible;
    private Integer sort;
    private List<MenuTreeVO> children = new ArrayList<>();
}
