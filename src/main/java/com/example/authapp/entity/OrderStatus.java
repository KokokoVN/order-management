package com.example.authapp.entity;

public enum OrderStatus {
    da_tao("Đã tạo"),
    da_dat("Đã đặt"),
    da_nhan_hang("Đã nhận hàng");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

