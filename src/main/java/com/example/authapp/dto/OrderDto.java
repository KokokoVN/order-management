package com.example.authapp.dto;

import com.example.authapp.entity.Order;
import com.example.authapp.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDto {
    private Long id;
    private Long userId;
    private String userName;
    private String orderCode;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private OrderStatus status;
    private String statusLabel;
    private String address;
    private int totalQuantity;
    private int itemCount;
    private List<OrderDetailDto> items = new ArrayList<>();

    public OrderDto() {}

    public static OrderDto fromEntity(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUserName(order.getUser().getName());
        }
        dto.setOrderCode(order.getOrderCode());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setSentAt(order.getSentAt());
        dto.setStatus(order.getStatus());
        dto.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : "");
        dto.setAddress(order.getAddress());

        if (order.getOrderDetails() != null) {
            dto.setItems(order.getOrderDetails().stream()
                    .map(OrderDetailDto::fromEntity)
                    .collect(Collectors.toList()));
            dto.setItemCount(order.getOrderDetails().size());
            dto.setTotalQuantity(order.getOrderDetails().stream()
                    .mapToInt(d -> d.getQuantity() != null ? d.getQuantity() : 0)
                    .sum());
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public List<OrderDetailDto> getItems() {
        return items;
    }

    public void setItems(List<OrderDetailDto> items) {
        this.items = items;
    }
}

