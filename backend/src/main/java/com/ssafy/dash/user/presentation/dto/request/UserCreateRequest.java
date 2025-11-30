package com.ssafy.dash.user.presentation.dto.request;

import com.ssafy.dash.user.application.dto.UserCreateCommand;

public class UserCreateRequest {

    private String username;
    private String email;

    public UserCreateRequest() {}

    public UserCreateRequest(String username, String email) {
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

    public UserCreateCommand toCommand() {
        
        return new UserCreateCommand(username, email);
    }

}
