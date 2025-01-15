package com.example.demo.Service;

import com.example.demo.Entity.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AppService {
    void saveUserDetail(UserDetail userDetail);

    void delete(int id);

    void saveUser(User user);

    void saveUser(String username);

    void saveUserDetail(int uid , String username , String email , String creationDate , Role role);

    void saveRole(int uid , String username);

    List<UserDetail> userDetailListByPage(Pageable pageable);

    List<UserDetail> userDetailList();

    User getUser(String username);

    UserDetail getUserDetail(int id);

    Role getRole(int id);
}
