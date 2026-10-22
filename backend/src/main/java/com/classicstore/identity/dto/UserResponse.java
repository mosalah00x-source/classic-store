package com.classicstore.identity.dto;

import com.classicstore.identity.domain.UserEntity;
import com.classicstore.identity.domain.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private UserRole role;
    private OffsetDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(UserEntity entity) {
        this.id = entity.getId();
        this.email = entity.getEmail();
        this.phone = entity.getPhone();
        this.firstName = entity.getFirstName();
        this.lastName = entity.getLastName();
        this.role = entity.getRole();
        this.createdAt = entity.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
