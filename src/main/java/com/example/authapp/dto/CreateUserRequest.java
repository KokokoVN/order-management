package com.example.authapp.dto;

import com.example.authapp.entity.Role;
import com.fasterxml.jackson.annotation.JsonAlias;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @JsonAlias({"username", "name"})
    private String name;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 4, message = "Mật khẩu tối thiểu 4 ký tự")
    private String password;

    @NotNull(message = "Vai trò không được để trống")
    private Role role;

    public CreateUserRequest() {}

    public CreateUserRequest(String name, String password, Role role) {
        this.name = name;
        this.password = password;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
