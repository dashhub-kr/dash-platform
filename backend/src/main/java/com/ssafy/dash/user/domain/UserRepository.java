package com.ssafy.dash.user.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByIdIncludingDeleted(Long id);

    List<User> findAll();

    List<User> searchByKeyword(String keyword);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    void update(User user);

    boolean delete(Long id);

    List<User> findUsersForAnonymization(java.time.LocalDateTime threshold);

    void anonymize(Long id);

    List<User> findByStudyId(Long studyId);

}
