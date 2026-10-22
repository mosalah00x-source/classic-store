package com.classicstore.identity.dto;

import com.classicstore.identity.domain.AddressEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AddressResponse {

    private UUID id;
    private UUID userId;
    private String recipientName;
    private String phone;
    private String countryCode;
    private Long governorateId;
    private Long cityId;
    private String district;
    private String line1;
    private String street;
    private String building;
    private String apartment;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isDefault;
    private OffsetDateTime createdAt;

    public AddressResponse() {
    }

    public AddressResponse(AddressEntity entity) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.recipientName = entity.getRecipientName();
        this.phone = entity.getPhone();
        this.countryCode = entity.getCountryCode();
        this.governorateId = entity.getGovernorateId();
        this.cityId = entity.getCityId();
        this.district = entity.getDistrict();
        this.line1 = entity.getStreet();
        this.street = entity.getStreet();
        this.building = entity.getBuilding();
        this.apartment = entity.getApartment();
        this.postalCode = entity.getPostalCode();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
        this.isDefault = entity.isDefault();
        this.createdAt = entity.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Long getGovernorateId() {
        return governorateId;
    }

    public void setGovernorateId(Long governorateId) {
        this.governorateId = governorateId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
