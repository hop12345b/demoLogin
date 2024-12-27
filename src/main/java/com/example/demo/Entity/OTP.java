package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "otp")
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private int id;

    @Column(name = "uid")
    private int uid;

    @Column(name = "otp")
    private String otp;

    @Column(name = "otp_expiry")
    private String otpExpiry;

    @OneToOne
    @JoinColumn(name = "id" , referencedColumnName = "uid", insertable = false , updatable = false)
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

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
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
