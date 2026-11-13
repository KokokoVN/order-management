package com.example.authapp.config;

import com.example.authapp.entity.*;
import com.example.authapp.repository.CodeValueRepository;
import com.example.authapp.repository.OrderRepository;
import com.example.authapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CodeValueRepository codeValueRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
            CodeValueRepository codeValueRepository,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.codeValueRepository = codeValueRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // Tự động nâng cấp kiểu dữ liệu cột product_name và variant để lưu link dài
        // (TikTok, Shopee,...)
        try {
            jdbcTemplate.execute("ALTER TABLE order_details ALTER COLUMN product_name VARCHAR(65535)");
            log.info(">>> Đã nâng cấp cột order_details.product_name thành VARCHAR(65535)");
        } catch (Exception e) {
            log.debug("Notice on product_name alter: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE order_details ALTER COLUMN variant VARCHAR(1000)");
            log.info(">>> Đã nâng cấp cột order_details.variant thành VARCHAR(1000)");
        } catch (Exception e) {
            log.debug("Notice on variant alter: {}", e.getMessage());
        }
        // 1. Seed Users

        // 1. Khởi tạo tài khoản hệ thống (admin / user) nếu chưa có
        User admin = userRepository.findByName("admin").orElseGet(() -> {
            User u = new User("admin", passwordEncoder.encode("admin123"), Role.admin);
            return userRepository.save(u);
        });

        User user = userRepository.findByName("user").orElseGet(() -> {
            User u = new User("user", passwordEncoder.encode("user123"), Role.user);
            return userRepository.save(u);
        });

    }
}
