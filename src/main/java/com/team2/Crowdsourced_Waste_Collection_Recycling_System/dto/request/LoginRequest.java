package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;

public class LoginRequest {
    @JsonAlias("username")
    private String email;
    private String password;

    public LoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
