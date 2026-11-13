package com.example.authapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    // Lưu link sản phẩm: dùng TEXT để chứa được cả link TikTok, Shopee dài hàng trăm/nghìn ký tự
    @Column(name = "product_name", nullable = false, columnDefinition = "TEXT", length = 65535)
    private String productName;

    @Column(name = "variant", length = 1000)
    private String variant;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "code_value_id", nullable = false)
    private CodeValue codeValue;

    public OrderDetail() {
    }

    public OrderDetail(String productName, String variant, Integer quantity, CodeValue codeValue) {
        this.productName = productName;
        this.variant = variant;
        this.quantity = quantity;
        this.codeValue = codeValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    public CodeValue getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(CodeValue codeValue) {
        this.codeValue = codeValue;
    }
}
