package com.example.demo.Service;

import com.example.demo.Entity.Password;
import com.example.demo.Entity.UserDetail;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface PasswordService {
    List<Password> getPasswordList(int uid);

    void savePassword(Password password);

    boolean checkOldPassword(int uid , String password);

    void sendPasswordEmail(UserDetail userDetail , String password) throws MessagingException, UnsupportedEncodingException;

    void savePassword(int uid , String password);
}
