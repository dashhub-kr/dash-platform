package com.ssafy.dash.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractOAuth2Response implements OAuth2Response {
    private final Map<String, Object> attributes;

    protected AbstractOAuth2Response(Map<String, Object> attributes) {
        this.attributes = Collections.unmodifiableMap(
            attributes == null ? Map.of() : new HashMap<>(attributes)
        );
    }

    @Override
    public Map<String, Object> getAttributes() {

        return attributes;
    }

    protected String valueAsString(String key) {
        Object v = attributes.get(key);
        
        return v == null ? null : v.toString();
    }
    
}
