package com.classicstore.identity.dto;

public class RefreshResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn = 900;

    public RefreshResponse() {
    }

    public RefreshResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public RefreshResponse(String accessToken, String tokenType, long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
