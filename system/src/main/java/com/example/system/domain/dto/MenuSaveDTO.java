package com.example.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuSaveDTO {

    private Long id;
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer menuType;
    private Integer visible;
    private Integer sort;
    private Integer status;
}
