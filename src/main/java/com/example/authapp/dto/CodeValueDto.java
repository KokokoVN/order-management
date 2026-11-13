package com.example.authapp.dto;

import com.example.authapp.entity.CodeValue;

public class CodeValueDto {
    private Long id;
    private String codeValue;

    public CodeValueDto() {}

    public CodeValueDto(Long id, String codeValue) {
        this.id = id;
        this.codeValue = codeValue;
    }

    public static CodeValueDto fromEntity(CodeValue entity) {
        return new CodeValueDto(entity.getId(), entity.getCodeValue());
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

