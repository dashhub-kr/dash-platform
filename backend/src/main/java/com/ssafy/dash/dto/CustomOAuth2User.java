package com.ssafy.dash.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2Response oAuth2Response;
    private final String role;

    public CustomOAuth2User(OAuth2Response oAuth2Response, String role) {
        this.oAuth2Response = oAuth2Response;
        this.role = role;
    }

    @Override
    public Map<String, Object> getAttributes() {
        
        return oAuth2Response.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> role);
        
        return authorities;
    }

    @Override
    public String getName() {
        
        return oAuth2Response.getName();
    }
    
    public String getUsername() {

        return oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
    }

}
