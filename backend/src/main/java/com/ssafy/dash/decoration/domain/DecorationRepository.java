package com.ssafy.dash.decoration.domain;

import java.util.List;
import java.util.Optional;

public interface DecorationRepository {
    void save(Decoration decoration);

    List<Decoration> findAll();

    List<Decoration> selectShopItems();

    Optional<Decoration> findById(Long id);

    void delete(Long id);

    // User Decoration
    void saveUserDecoration(UserDecoration userDecoration);

    List<UserDecoration> findByUserId(Long userId);

    boolean existsUserDecoration(Long userId, Long decorationId);

    void deleteUserDecoration(Long userId, Long decorationId);
}
