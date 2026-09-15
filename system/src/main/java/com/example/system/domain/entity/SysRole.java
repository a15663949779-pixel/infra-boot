package com.example.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer sort;
    private Integer status;
}
