package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "status")
    private String status;

    @Column(name = "FAILED_ATTEMPTS")
    private int FAILED_ATTEMPTS;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL)
    private OTP otp;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL )
    private Password passwords;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL)
    private UserDetail userDetail;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL)
    private Role role;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getFAILED_ATTEMPTS() {
        return FAILED_ATTEMPTS;
    }

    public void setFAILED_ATTEMPTS(int FAILED_ATTEMPTS) {
        this.FAILED_ATTEMPTS = FAILED_ATTEMPTS;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OTP getOtp() {
        return otp;
    }

    public void setOtp(OTP otp) {
        this.otp = otp;
    }

    public Password getPasswords() {
        return passwords;
    }

    public void setPasswords(Password passwords) {
        this.passwords = passwords;
    }

    public UserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
