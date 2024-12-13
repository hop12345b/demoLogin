package com.example.demo.Repository;

import com.example.demo.Entity.Password;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordRepository extends JpaRepository<Password , Integer> {
    Optional<Password> findByUsername(String username);
}
