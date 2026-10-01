package com.box.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    private String password;
    private String new_password;
    private String new_password_repeat;
}