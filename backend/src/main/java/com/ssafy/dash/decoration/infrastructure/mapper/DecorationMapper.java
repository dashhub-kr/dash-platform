package com.ssafy.dash.decoration.infrastructure.mapper;

import com.ssafy.dash.decoration.domain.Decoration;
import com.ssafy.dash.decoration.domain.UserDecoration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DecorationMapper {
    void insert(Decoration decoration);

    List<Decoration> selectAll();

    List<Decoration> selectShopItems();

    Optional<Decoration> selectById(Long id);

    void delete(Long id);

    void insertUserDecoration(UserDecoration userDecoration);

    List<UserDecoration> selectByUserId(Long userId);

    int countUserDecoration(@Param("userId") Long userId, @Param("decorationId") Long decorationId);

    void deleteUserDecoration(@Param("userId") Long userId, @Param("decorationId") Long decorationId);
}
