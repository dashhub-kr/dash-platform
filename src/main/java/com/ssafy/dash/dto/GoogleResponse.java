package com.ssafy.dash.dto;

import java.util.Map;

public class GoogleResponse extends AbstractOAuth2Response {

    public GoogleResponse(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return valueAsString("sub");
    }

    @Override
    public String getEmail() {
        return valueAsString("email");
    }

    @Override
    public String getName() {
        return valueAsString("name");
    }

}
