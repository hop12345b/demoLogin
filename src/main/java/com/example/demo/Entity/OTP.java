package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "otp")
    private String otp;

    @Column(name = "otp_expiry")
    private String otpExpiry;

    @OneToOne
    @JoinColumn(name = "username" , referencedColumnName = "username", insertable = false , updatable = false)
    private User user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(String otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otpForgetPassword) {
        this.otp = otpForgetPassword;
    }

}
