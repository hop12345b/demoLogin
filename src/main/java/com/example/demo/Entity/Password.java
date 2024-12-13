package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
public class Password {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String currentPassword;

    @Column(name = "old_password1")
    private String oldPassword1;

    @Column(name = "old_password2")
    private String oldPassword2;

    @Column(name = "old_password3")
    private String oldPassword3;

    @OneToOne
    @JoinColumn(name = "username" , referencedColumnName = "username" , insertable = false , updatable = false)
    private User user;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getOldPassword1() {
        return oldPassword1;
    }

    public String getOldPassword2() {
        return oldPassword2;
    }

    public String getOldPassword3() {
        return oldPassword3;
    }

    public void setPassword(String password){
        oldPassword3 = oldPassword2;
        oldPassword2 = oldPassword1;
        oldPassword1 = currentPassword;
        currentPassword = password;
    }
}
