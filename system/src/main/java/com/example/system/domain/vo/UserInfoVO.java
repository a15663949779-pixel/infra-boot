package com.example.system.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserInfoVO {

    private Long userId;
    private String username;
    private String nickname;
    private List<String> roles;
    private Set<String> permissions;
}
