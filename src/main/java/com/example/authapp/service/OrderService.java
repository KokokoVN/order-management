package com.example.authapp.service;

import com.example.authapp.dto.*;
import com.example.authapp.entity.*;
import com.example.authapp.repository.CodeValueRepository;
import com.example.authapp.repository.OrderDetailRepository;
import com.example.authapp.repository.OrderRepository;
import com.example.authapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CodeValueRepository codeValueRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository,
                        CodeValueRepository codeValueRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.codeValueRepository = codeValueRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // CODE VALUES (Giá trị mã)
    // ==========================================

    public List<CodeValueDto> getAllCodeValues() {
        return codeValueRepository.findAll().stream()
                .map(CodeValueDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CodeValueDto createCodeValue(CreateCodeValueRequest request) {
        String val = request.getCodeValue() != null ? request.getCodeValue().trim() : "";
        if (val.isEmpty()) {
            throw new IllegalArgumentException("Nội dung giá trị mã không được để trống!");
        }

        if (codeValueRepository.existsByCodeValue(val)) {
            throw new IllegalArgumentException("Giá trị mã '" + val + "' đã tồn tại!");
        }

        CodeValue saved = codeValueRepository.save(new CodeValue(val));
        return CodeValueDto.fromEntity(saved);
    }

    @Transactional
    public void deleteCodeValue(Long id) {
        CodeValue cv = codeValueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giá trị mã ID: " + id));

        // Rule: "Không xóa giá trị mã đang được sử dụng trong chi tiết đơn hàng"
        if (orderDetailRepository.existsByCodeValueId(id)) {
            throw new IllegalArgumentException("Không thể xóa giá trị mã '" + cv.getCodeValue() +
                    "' vì đang được sử dụng trong chi tiết đơn hàng!");
        }

        codeValueRepository.delete(cv);
    }

    // ==========================================
    // ORDERS (Đơn hàng & Chi tiết)
    // ==========================================

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getOrdersByUser(String username) {
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long orderId, String username, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!isAdmin && !order.getUser().getName().equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập đơn hàng này!");
        }

        return OrderDto.fromEntity(order);
    }

    @Transactional
    public OrderDto createOrder(String username, CreateOrderRequest request) {
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + username));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải chứa ít nhất 1 sản phẩm!");
        }

        // Lọc các item hợp lệ (bỏ qua các dòng hoàn toàn rỗng nếu người dùng thêm nhiều dòng mà chưa nhập)
        List<OrderDetailRequest> validItems = request.getItems().stream()
                .filter(it -> it != null && it.getProductName() != null && !it.getProductName().trim().isEmpty())
                .collect(Collectors.toList());

        if (validItems.isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải chứa ít nhất 1 link sản phẩm!");
        }

        // Tự động sinh mã đơn hàng nếu để trống
        String orderCode = request.getOrderCode();
        if (orderCode == null || orderCode.trim().isEmpty()) {
            orderCode = "DH" + String.format("%06d", (System.currentTimeMillis() % 1000000));
        } else {
            orderCode = orderCode.trim();
        }

        while (orderRepository.existsByOrderCode(orderCode)) {
            orderCode = "DH" + String.format("%06d", ((System.currentTimeMillis() + (int)(Math.random() * 10000)) % 1000000));
        }

        Order order = new Order(user, orderCode);
        order.setStatus(OrderStatus.da_tao);
        order.setCreatedAt(LocalDateTime.now());

        Long orderLevelCodeValueId = request.getCodeValueId();

        for (OrderDetailRequest item : validItems) {
            int qty = (item.getQuantity() != null && item.getQuantity() > 0) ? item.getQuantity() : 1;

            Long effectiveCodeId = item.getCodeValueId() != null ? item.getCodeValueId() : orderLevelCodeValueId;
            if (effectiveCodeId == null) {
                // Fallback nếu không truyền mã: lấy mã đầu tiên trong database
                CodeValue firstCv = codeValueRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Hệ thống chưa có giá trị mã nào. Vui lòng liên hệ Admin để tạo giá trị mã trước khi đặt đơn!"));
                effectiveCodeId = firstCv.getId();
            }

            final Long finalCodeId = effectiveCodeId;
            CodeValue cv = codeValueRepository.findById(finalCodeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giá trị mã ID: " + finalCodeId));

            OrderDetail detail = new OrderDetail(
                    item.getProductName().trim(),
                    item.getVariant() != null ? item.getVariant().trim() : null,
                    qty,
                    cv
            );
            order.addDetail(detail);
        }

        Order saved = orderRepository.save(order);
        return OrderDto.fromEntity(saved);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus, LocalDateTime sentAt) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        order.setStatus(newStatus);
        if (sentAt != null) {
            order.setSentAt(sentAt);
        } else if (newStatus == OrderStatus.da_dat && order.getSentAt() == null) {
            order.setSentAt(LocalDateTime.now());
        }

        Order updated = orderRepository.save(order);
        return OrderDto.fromEntity(updated);
    }

    @Transactional
    public void deleteOrder(Long orderId, String username, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!isAdmin) {
            if (!order.getUser().getName().equalsIgnoreCase(username)) {
                throw new IllegalArgumentException("Bạn không có quyền xóa đơn hàng này!");
            }
            if (order.getStatus() != OrderStatus.da_tao) {
                throw new IllegalArgumentException("Chỉ có thể xóa đơn hàng khi ở trạng thái 'Đã tạo'!");
            }
        }

        // Rule: "khi xóa đơn hàng, các dòng chi tiết của đơn đó được xóa theo"
        orderRepository.delete(order);
    }
}

