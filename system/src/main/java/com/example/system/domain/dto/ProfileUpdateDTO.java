package com.example.system.domain.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {

    private String nickname;
    private String email;
    private String phone;
    private Long avatar;
}
