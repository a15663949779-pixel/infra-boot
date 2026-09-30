package com.example.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_dict_type")
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long dictId;

    private String dictName;

    private String dictType;

    private Integer status;

    private String remark;
}
