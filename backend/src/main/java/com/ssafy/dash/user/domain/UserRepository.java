package com.ssafy.dash.user.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
}
