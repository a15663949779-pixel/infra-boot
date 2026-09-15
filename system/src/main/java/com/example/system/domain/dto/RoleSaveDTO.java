package com.example.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RoleSaveDTO {

    private Long id;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    @NotBlank(message = "角色标识不能为空")
    private String roleKey;
    private Integer sort;
    private Integer status;
    private List<Long> menuIds;
}
