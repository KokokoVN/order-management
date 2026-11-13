package com.example.authapp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import javax.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @JsonAlias({"username", "name"})
    private String name;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
