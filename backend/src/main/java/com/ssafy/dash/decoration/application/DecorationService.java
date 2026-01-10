package com.ssafy.dash.decoration.application;

import com.ssafy.dash.decoration.domain.Decoration;
import com.ssafy.dash.decoration.domain.DecorationRepository;
import com.ssafy.dash.decoration.domain.UserDecoration;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DecorationService {

    private final DecorationRepository decorationRepository;
    private final UserRepository userRepository;

    public DecorationService(DecorationRepository decorationRepository, UserRepository userRepository) {
        this.decorationRepository = decorationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Decoration create(String name, String description, String cssClass, String type) {
        // Default: Price 0, Active True (or update Service to accept these)
        // For backwards compatibility/legacy admin:
        Decoration decoration = Decoration.create(name, description, cssClass, type, 0, true);
        decorationRepository.save(decoration);
        return decoration;
    }

    @Transactional(readOnly = true)
    public List<Decoration> findAll() {
        return decorationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UserDecoration> getInventory(Long userId) {
        return decorationRepository.findByUserId(userId);
    }

    @Transactional
    public void grant(Long userId, Long decorationId) {
        if (decorationRepository.existsUserDecoration(userId, decorationId)) {
            // 이미 보유 중
            return;
        }
        UserDecoration ud = UserDecoration.create(userId, decorationId);
        decorationRepository.saveUserDecoration(ud);
    }

    @Transactional
    public void equip(Long userId, Long decorationId) {
        // 소유 여부 확인
        if (!decorationRepository.existsUserDecoration(userId, decorationId)) {
            throw new IllegalArgumentException("User does not own this decoration");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEquippedDecorationId(decorationId);
        userRepository.update(user);
    }

    @Transactional
    public void unequip(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEquippedDecorationId(null);
        userRepository.update(user);
    }

    @Transactional
    public void delete(Long id) {
        decorationRepository.delete(id);
    }
}
