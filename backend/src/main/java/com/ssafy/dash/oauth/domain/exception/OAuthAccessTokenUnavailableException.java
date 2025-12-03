package com.ssafy.dash.oauth.domain.exception;

public class OAuthAccessTokenUnavailableException extends RuntimeException {

    public OAuthAccessTokenUnavailableException(String message) {
        super(message);
    }

}
