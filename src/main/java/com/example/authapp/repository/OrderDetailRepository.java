package com.example.authapp.repository;

import com.example.authapp.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    boolean existsByCodeValueId(Long codeValueId);
    List<OrderDetail> findByOrderId(Long orderId);
}

