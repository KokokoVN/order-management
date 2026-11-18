package com.example.authapp.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class CreateOrderRequest {

    // Mã đơn hàng: Tự động sinh nếu để trống
    private String orderCode;

    // Giá trị mã chung cho cả đơn hàng
    private Long codeValueId;

    // Địa chỉ đơn hàng (dc đơn hàng)
    private String address;

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid
    private List<OrderDetailRequest> items = new ArrayList<>();

    public CreateOrderRequest() {}

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Long getCodeValueId() {
        return codeValueId;
    }

    public void setCodeValueId(Long codeValueId) {
        this.codeValueId = codeValueId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<OrderDetailRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderDetailRequest> items) {
        this.items = items;
    }
}
