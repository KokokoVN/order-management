package com.example.authapp.entity;

public enum Role {
    admin,
    user;

    public String toAuthority() {
        return "ROLE_" + this.name().toUpperCase();
    }
}
