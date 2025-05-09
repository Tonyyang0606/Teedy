package com.sismics.docs.core.dao.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.util.Date;

public class RegisterUserDto {
    private String id;

    private String username;

    private String email;

    private Long storage;

    private Long submitTime;

    private Integer status;

    private Long operatedTime;

    public String getId() {
        return id;
    }

    public RegisterUserDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RegisterUserDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public RegisterUserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public Long getStorage() {
        return storage;
    }

    public RegisterUserDto setStorage(Long storage) {
        this.storage = storage;
        return this;
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public RegisterUserDto setSubmitTime(Long submitTime) {
        this.submitTime = submitTime;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public RegisterUserDto setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Long getOperatedTime() {
        return operatedTime;
    }

    public RegisterUserDto setOperatedTime(Long operatedTime) {
        this.operatedTime = operatedTime;
        return this;
    }

    @Override
    public String toString() {
        return "RegisterUserDto [id=" + id + ", username=" + username + "]";
    }
}