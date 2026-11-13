package com.example.authapp.controller;

import com.example.authapp.config.JwtUtils;
import com.example.authapp.dto.ApiResponse;
import com.example.authapp.dto.LoginRequest;
import com.example.authapp.dto.LoginResponse;
import com.example.authapp.dto.UserDto;
import com.example.authapp.entity.User;
import com.example.authapp.repository.UserRepository;
import com.example.authapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String loginName = request.getName();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginName, request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateToken(authentication);

            User user = userRepository.findByName(loginName)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            LoginResponse response = new LoginResponse(
                    jwt,
                    user.getId(),
                    user.getName(),
                    user.getRole().toAuthority()
            );

            return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Tên đăng nhập hoặc mật khẩu không chính xác"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi đăng nhập: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        UserDto userDto = userService.getUserByName(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Thông tin người dùng hiện tại", userDto));
    }
}
