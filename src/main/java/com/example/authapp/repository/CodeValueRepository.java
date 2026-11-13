package com.example.authapp.repository;

import com.example.authapp.entity.CodeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeValueRepository extends JpaRepository<CodeValue, Long> {
    Optional<CodeValue> findByCodeValue(String codeValue);
    boolean existsByCodeValue(String codeValue);
}

