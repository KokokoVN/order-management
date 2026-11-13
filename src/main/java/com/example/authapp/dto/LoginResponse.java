package com.example.authapp.dto;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String role;

    public LoginResponse() {}

    public LoginResponse(String token, Long id, String name, String role) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Alias for frontend compatibility
    public String getUsername() {
        return this.name;
    }

    public String getFullName() {
        return this.name;
    }
}
