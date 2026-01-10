package com.ssafy.dash.decoration.infrastructure.persistence;

import com.ssafy.dash.decoration.domain.Decoration;
import com.ssafy.dash.decoration.domain.DecorationRepository;
import com.ssafy.dash.decoration.domain.UserDecoration;
import com.ssafy.dash.decoration.infrastructure.mapper.DecorationMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DecorationRepositoryImpl implements DecorationRepository {

    private final DecorationMapper decorationMapper;

    public DecorationRepositoryImpl(DecorationMapper decorationMapper) {
        this.decorationMapper = decorationMapper;
    }

    @Override
    public void save(Decoration decoration) {
        decorationMapper.insert(decoration);
    }

    @Override
    public List<Decoration> findAll() {
        return decorationMapper.selectAll();
    }

    @Override
    public List<Decoration> selectShopItems() {
        return decorationMapper.selectShopItems();
    }

    @Override
    public Optional<Decoration> findById(Long id) {
        return decorationMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        decorationMapper.delete(id);
    }

    @Override
    public void saveUserDecoration(UserDecoration userDecoration) {
        decorationMapper.insertUserDecoration(userDecoration);
    }

    @Override
    public List<UserDecoration> findByUserId(Long userId) {
        return decorationMapper.selectByUserId(userId);
    }

    @Override
    public boolean existsUserDecoration(Long userId, Long decorationId) {
        return decorationMapper.countUserDecoration(userId, decorationId) > 0;
    }

    @Override
    public void deleteUserDecoration(Long userId, Long decorationId) {
        decorationMapper.deleteUserDecoration(userId, decorationId);
    }
}
