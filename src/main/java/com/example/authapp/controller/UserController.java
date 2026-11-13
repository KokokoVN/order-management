package com.example.authapp.controller;

import com.example.authapp.dto.*;
import com.example.authapp.service.OrderService;
import com.example.authapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final OrderService orderService;

    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        try {
            UserDto dto = userService.getUserByName(userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin người dùng thành công", dto));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin cá nhân: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // DANH SÁCH GIÁ TRỊ MÃ ĐỂ CHỌN KHI TẠO ĐƠN
    // ==========================================

    @GetMapping("/code-values")
    public ResponseEntity<?> getAvailableCodeValues() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Danh sách giá trị mã", orderService.getAllCodeValues()));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách mã: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi khi lấy danh sách mã: " + e.getMessage()));
        }
    }

    // ==========================================
    // QUẢN LÝ ĐƠN HÀNG CỦA USER
    // ==========================================

    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        try {
            List<OrderDto> orders = orderService.getOrdersByUser(userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Danh sách đơn hàng của bạn", orders));
        } catch (IllegalArgumentException e) {
            log.warn("Không tìm thấy người dùng hoặc dữ liệu đơn hàng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách đơn hàng của user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi máy chủ khi lấy danh sách đơn hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        try {
            OrderDto dto = orderService.getOrderById(id, userDetails.getUsername(), false);
            return ResponseEntity.ok(ApiResponse.ok("Chi tiết đơn hàng", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết đơn hàng: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        try {
            OrderDto created = orderService.createOrder(userDetails.getUsername(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Tạo đơn hàng thành công!", created));
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi dữ liệu tạo đơn hàng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo đơn hàng: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo đơn hàng: " + e.getMessage()));
        }
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        try {
            orderService.deleteOrder(id, userDetails.getUsername(), false);
            return ResponseEntity.ok(ApiResponse.ok("Xóa đơn hàng thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi hủy đơn hàng: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}
