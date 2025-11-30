package com.ssafy.dash.user.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    void update(User user);

    boolean delete(Long id);
    
}
