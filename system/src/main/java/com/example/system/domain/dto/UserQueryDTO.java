package com.example.system.domain.dto;

import com.example.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {
    private String username;
    private String phone;
    private Integer status;
}
