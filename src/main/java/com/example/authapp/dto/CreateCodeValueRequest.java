package com.example.authapp.dto;

import javax.validation.constraints.NotBlank;

public class CreateCodeValueRequest {

    @NotBlank(message = "Giá trị mã không được để trống")
    private String codeValue;

    public CreateCodeValueRequest() {}

    public CreateCodeValueRequest(String codeValue) {
        this.codeValue = codeValue;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }
}

