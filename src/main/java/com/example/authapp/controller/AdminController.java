package com.example.authapp.controller;

import com.example.authapp.dto.*;
import com.example.authapp.entity.OrderStatus;
import com.example.authapp.service.OrderService;
import com.example.authapp.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;

    public AdminController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    // ==========================================
    // 1. QUẢN LÝ TÀI KHOẢN (Chỉ tạo ở trang Admin)
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách người dùng thành công", users));
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }

        try {
            UserDto createdUser = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Tạo tài khoản mới thành công!", createdUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo tài khoản: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails adminDetails) {
        try {
            userService.deleteUser(id, adminDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Xóa tài khoản thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // 2. QUẢN LÝ GIÁ TRỊ MÃ (code_values)
    // ==========================================

    @GetMapping("/code-values")
    public ResponseEntity<?> getAllCodeValues() {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách giá trị mã", orderService.getAllCodeValues()));
    }

    @PostMapping("/code-values")
    public ResponseEntity<?> createCodeValue(@Valid @RequestBody CreateCodeValueRequest request,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        try {
            CodeValueDto dto = orderService.createCodeValue(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Thêm giá trị mã thành công", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/code-values/{id}")
    public ResponseEntity<?> deleteCodeValue(@PathVariable Long id) {
        try {
            orderService.deleteCodeValue(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa giá trị mã thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // 3. QUẢN LÝ TẤT CẢ ĐƠN HÀNG (orders & details)
    // ==========================================

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách đơn hàng", orders));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails adminDetails) {
        try {
            OrderDto dto = orderService.getOrderById(id, adminDetails.getUsername(), true);
            return ResponseEntity.ok(ApiResponse.ok("Chi tiết đơn hàng", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        try {
            String statusStr = body.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng chọn trạng thái đơn hàng!"));
            }
            OrderStatus status = OrderStatus.valueOf(statusStr);

            LocalDateTime sentAt = null;
            if (body.containsKey("sentAt") && body.get("sentAt") != null && !body.get("sentAt").trim().isEmpty()) {
                sentAt = LocalDateTime.parse(body.get("sentAt"));
            }

            OrderDto updated = orderService.updateOrderStatus(id, status, sentAt);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái đơn hàng thành công!", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Định dạng dữ liệu không hợp lệ!"));
        }
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails adminDetails) {
        try {
            orderService.deleteOrder(id, adminDetails.getUsername(), true);
            return ResponseEntity.ok(ApiResponse.ok("Xóa đơn hàng thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(required = false) OrderStatus status,
            @AuthenticationPrincipal UserDetails adminDetails) {
        try {
            byte[] excelData = orderService.exportOrdersToExcel(status, adminDetails != null ? adminDetails.getUsername() : null, true);
            String statusSuffix = status != null ? status.name() : "tat_ca";
            String filename = "quan_ly_don_hang_" + statusSuffix + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
