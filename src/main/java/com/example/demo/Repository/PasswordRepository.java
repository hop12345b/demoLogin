package com.example.demo.Repository;

import com.example.demo.Entity.Password;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordRepository extends JpaRepository<Password , Integer> {
    List<Password> findByUid(int uid);
}
