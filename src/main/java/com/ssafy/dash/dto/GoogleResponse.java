package com.ssafy.dash.dto;

import java.util.Map;

public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attributes;

    public GoogleResponse(Map<String, Object> attributes) {

        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        Object v = attributes.get("sub");
        return v == null ? null : v.toString();
    }

    @Override
    public String getEmail() {
        Object v = attributes.get("email");
        return v == null ? null : v.toString();
    }

    @Override
    public String getName() {
        Object v = attributes.get("name");
        return v == null ? null : v.toString();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

}
