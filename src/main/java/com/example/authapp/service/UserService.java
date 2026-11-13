package com.example.authapp.service;

import com.example.authapp.dto.CreateUserRequest;
import com.example.authapp.dto.UserDto;
import com.example.authapp.entity.User;
import com.example.authapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDto getUserByName(String name) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + name));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        String username = request.getName();
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }

        if (userRepository.existsByName(username.trim())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + username + "' đã tồn tại!");
        }

        User user = new User(
                username.trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        User saved = userRepository.save(user);
        return UserDto.fromEntity(saved);
    }

    @Transactional
    public void deleteUser(Long id, String currentAdminName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));

        if (user.getName().equalsIgnoreCase(currentAdminName)) {
            throw new IllegalArgumentException("Bạn không thể xóa tài khoản của chính mình!");
        }

        userRepository.delete(user);
    }
}
