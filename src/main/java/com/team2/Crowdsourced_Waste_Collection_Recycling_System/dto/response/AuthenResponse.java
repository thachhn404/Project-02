package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.UserDto;

public class AuthenResponse {
    private String token;
    private String tokenType;
    private long expiresInMs;
    private UserDto user;

    public AuthenResponse() {
    }

    public AuthenResponse(String token, String tokenType, long expiresInMs, UserDto user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresInMs = expiresInMs;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}