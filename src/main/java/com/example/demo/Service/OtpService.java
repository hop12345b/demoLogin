package com.example.demo.Service;

import com.example.demo.Entity.OTP;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface OtpService {
    OTP getOtp(int uid);

    void saveNewOtp(int uid);

    void saveOTP(OTP otp);

    void sendOTPEmail(String email, OTP otp) throws UnsupportedEncodingException, MessagingException ;
}
