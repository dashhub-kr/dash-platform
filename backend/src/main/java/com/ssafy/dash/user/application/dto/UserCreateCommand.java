package com.ssafy.dash.user.application.dto;

public class UserCreateCommand {

    private final String username;
    private final String email;

    public UserCreateCommand(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
    
}
