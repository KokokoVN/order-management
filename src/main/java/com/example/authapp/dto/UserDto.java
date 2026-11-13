package com.example.authapp.dto;

import com.example.authapp.entity.Role;
import com.example.authapp.entity.User;

public class UserDto {
    private Long id;
    private String name;
    private Role role;

    public UserDto() {}

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Aliases
    public String getUsername() {
        return this.name;
    }

    public String getFullName() {
        return this.name;
    }
}
