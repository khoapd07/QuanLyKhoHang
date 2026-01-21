package com.poly.quanlykhohang.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String tenTaiKhoan;
    private String password;
}