package com.ssafy.dash.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ssafy.dash.dto.CustomOAuth2User;
import com.ssafy.dash.dto.GoogleResponse;
import com.ssafy.dash.dto.OAuth2Response;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("OAuth2 User Attributes: " + oAuth2User.getAttributes());

        // 여기에 사용자 정보를 처리하는 로직을 추가합니다.
        // 예: 데이터베이스에 사용자 정보 저장 또는 업데이트

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if ("google".equals(registrationId)) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {

            return null;
        }

        return new CustomOAuth2User(oAuth2Response, "ROLE_USER");
    }

}
