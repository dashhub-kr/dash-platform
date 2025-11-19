package com.ssafy.dash.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ssafy.dash.dto.CustomOAuth2User;
import com.ssafy.dash.dto.GoogleResponse;
import com.ssafy.dash.dto.OAuth2Response;
import com.ssafy.dash.entity.UserEntity;
import com.ssafy.dash.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("OAuth2 User Attributes: " + oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;
        if ("google".equals(registrationId)) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {

            return null;
        }

        String username = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();

        UserEntity userEntity = userRepository.findByUsername(username)
            .map(entity -> {
                entity.setEmail(oAuth2Response.getEmail());
                return userRepository.save(entity);
            }).orElseGet(() -> {
                UserEntity newUser = new UserEntity();
                newUser.setUsername(username);
                newUser.setEmail(oAuth2Response.getEmail());
                newUser.setRole("ROLE_USER");
                return userRepository.save(newUser);
            });

        return new CustomOAuth2User(oAuth2Response, userEntity.getRole());
    }

}
