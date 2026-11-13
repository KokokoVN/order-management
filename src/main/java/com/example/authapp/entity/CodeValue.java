package com.example.authapp.entity;

import javax.persistence.*;

@Entity
@Table(name = "code_values")
public class CodeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_value", nullable = false, unique = true, length = 255)
    private String codeValue;

    public CodeValue() {
    }

    public CodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }
}

