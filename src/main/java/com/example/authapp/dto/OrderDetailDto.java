package com.example.authapp.dto;

import com.example.authapp.entity.OrderDetail;

public class OrderDetailDto {
    private Long id;
    private String productName;
    private String variant;
    private Integer quantity;
    private Long codeValueId;
    private String codeValue;

    public OrderDetailDto() {}

    public static OrderDetailDto fromEntity(OrderDetail entity) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(entity.getId());
        dto.setProductName(entity.getProductName());
        dto.setVariant(entity.getVariant());
        dto.setQuantity(entity.getQuantity());
        if (entity.getCodeValue() != null) {
            dto.setCodeValueId(entity.getCodeValue().getId());
            dto.setCodeValue(entity.getCodeValue().getCodeValue());
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getCodeValueId() {
        return codeValueId;
    }

    public void setCodeValueId(Long codeValueId) {
        this.codeValueId = codeValueId;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }
}

