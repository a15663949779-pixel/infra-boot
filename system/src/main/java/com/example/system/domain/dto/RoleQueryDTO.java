package com.example.system.domain.dto;

import com.example.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends PageQuery {
    private String roleName;
    private String roleKey;
    private Integer status;
}
