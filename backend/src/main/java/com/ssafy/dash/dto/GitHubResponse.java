package com.ssafy.dash.dto;

import java.util.Map;

public class GitHubResponse extends AbstractOAuth2Response {

    public GitHubResponse(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    public String getProviderId() {
        return valueAsString("id");
    }

    @Override
    public String getEmail() {
        String email = valueAsString("email");
        if (email != null && !email.isBlank()) return email;

        Object emailsObj = getAttributes().get("emails");
        if (emailsObj instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> emails = (java.util.List<Object>) emailsObj;
            for (Object e : emails) {
                if (e instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> m = (java.util.Map<String, Object>) e;
                    Object primary = m.get("primary");
                    Object emailVal = m.get("email");
                    if (emailVal != null) {
                        if (Boolean.TRUE.equals(primary)) return emailVal.toString();
                        if (email == null) email = emailVal.toString();
                    }
                }
            }
        }

        return email;
    }

    @Override
    public String getName() {
        String name = valueAsString("name");
        if (name != null && !name.isBlank()) return name;
        return valueAsString("login");
    }

}
