package com.ssafy.dash.user.dto;

public class UserUpdateRequest {

    private String username;
    private String email;

    public UserUpdateRequest() {}

    public UserUpdateRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
}
