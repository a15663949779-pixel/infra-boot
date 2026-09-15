package com.example.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_menu")
@EqualsAndHashCode(callSuper = true)
public class SysMenu extends BaseEntity {

    @TableId(type = IdType.AUTO)
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
    private Integer status;
}
