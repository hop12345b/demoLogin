package com.example.demo.Entity;

import jakarta.persistence.*;

import java.util.Calendar;
import java.util.Date;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "locked")
    private boolean locked;

    @Column(name = "FAILED_ATTEMPTS")
    private int FAILED_ATTEMPTS;

    @Column(name = "first_login")
    private String firstLogin;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "role")
    private String role;

    @Column(name = "creation_date")
    private Date creationDate;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL)
    private OTP otp;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL )
    private Password passwords;

//    public void setOtp(OTP otp) {
//        this.otp = otp;
//    }
//
//    public void setPasswords(Password passwords) {
//        this.passwords = passwords;
//    }

    public String getRole() {
        return role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getFAILED_ATTEMPTS() {
        return FAILED_ATTEMPTS;
    }

    public void setFAILED_ATTEMPTS(int FAILED_ATTEMPTS) {
        this.FAILED_ATTEMPTS = FAILED_ATTEMPTS;
    }

    public String getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(String firstLogin) {
        this.firstLogin = firstLogin;
    }

    public void setCreationDate() {
        this.creationDate = Calendar.getInstance().getTime();
    }
}
