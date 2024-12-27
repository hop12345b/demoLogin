package com.example.demo.Repository;

import com.example.demo.Entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailRepository extends JpaRepository<UserDetail , Integer> {
    Optional<UserDetail> findByUid(int uid);
}
