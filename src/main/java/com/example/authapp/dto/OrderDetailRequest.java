package com.example.authapp.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class OrderDetailRequest {

    @NotBlank(message = "Link / Tên sản phẩm không được để trống")
    private String productName;

    private String variant;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng sản phẩm phải lớn hơn 0")
    private Integer quantity = 1;

    // Tùy chọn: Nếu để trống sẽ kế thừa giá trị mã chung của toàn bộ đơn hàng
    private Long codeValueId;

    public OrderDetailRequest() {}

    public OrderDetailRequest(String productName, String variant, Integer quantity, Long codeValueId) {
        this.productName = productName;
        this.variant = variant;
        this.quantity = quantity;
        this.codeValueId = codeValueId;
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
}
