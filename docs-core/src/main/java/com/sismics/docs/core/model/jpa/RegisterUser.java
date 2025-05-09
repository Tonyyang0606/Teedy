package com.sismics.docs.core.model.jpa;


import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "T_REGISTER_USER")
public class RegisterUser implements Loggable{

    @Id
    @Column(name="REG_ID_C", nullable = false, length = 36)
    private String id;

    @Column(name="REG_USERNAME_C", nullable = false, length = 50)
    private String username;

    @Column(name="REG_PASSWORD_C", nullable = false, length = 60)
    private String password;

    @Column(name="REG_EMAIL_C", nullable = false, length = 100)
    private String email;

    @Column(name="REG_STORAGE_N", nullable = false)
    private Long storage;

    @Column(name="REG_SUBMIT_TIME_D", nullable = false)
    private Date submitTime;

    @Column(name="REG_STATUS_N", nullable = false)
    private Integer status;

    @Column(name="REG_OPERATED_TIME_D")
    private Date operatedTime;

    public String getId() {
        return id;
    }

    public RegisterUser setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RegisterUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RegisterUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public RegisterUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public Long getStorage() {
        return storage;
    }

    public RegisterUser setStorage(Long storage) {
        this.storage = storage;
        return this;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public RegisterUser setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public RegisterUser setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Date getOperatedTime() {
        return operatedTime;
    }

    public RegisterUser setOperatedTime(Date operatedTime) {
        this.operatedTime = operatedTime;
        return this;
    }

    @Override
    public String toMessage() {
        return "Register user [id=" + id + ", username=" + username + "]";
    }

    @Override
    public Date getDeleteDate() {
        return operatedTime;
    }
}