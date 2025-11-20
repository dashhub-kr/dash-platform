package com.ssafy.dash.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssafy.dash.entity.OAuthToken;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {

    Optional<OAuthToken> findByProviderAndProviderId(String provider, String providerId);
    Optional<OAuthToken> findByProviderAndProviderLogin(String provider, String providerLogin);
    
}
