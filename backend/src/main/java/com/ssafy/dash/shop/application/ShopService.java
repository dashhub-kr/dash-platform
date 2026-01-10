package com.ssafy.dash.shop.application;

import com.ssafy.dash.decoration.domain.Decoration;
import com.ssafy.dash.decoration.domain.DecorationRepository;
import com.ssafy.dash.decoration.domain.UserDecoration;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShopService {

    private final UserRepository userRepository;
    private final DecorationRepository decorationRepository;

    public ShopService(UserRepository userRepository, DecorationRepository decorationRepository) {
        this.userRepository = userRepository;
        this.decorationRepository = decorationRepository;
    }

    @Transactional(readOnly = true)
    public List<Decoration> getShopItems() {
        return decorationRepository.selectShopItems();
    }

    @Transactional
    public void buyItem(Long userId, Long decorationId) {
        // 1. Validate User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. Validate Item
        Decoration item = decorationRepository.findById(decorationId).orElse(null);
        if (item == null || Boolean.FALSE.equals(item.getIsActive())) {
            throw new IllegalArgumentException("Item not found or inactive");
        }

        // 3. User Ownership Check
        if (decorationRepository.existsUserDecoration(userId, decorationId)) {
            throw new IllegalStateException("Already owned");
        }

        // 4. Check Balance & Deduct (Price can be 0)
        int price = item.getPrice() != null ? item.getPrice() : 0;
        if (price > 0) {
            user.useLogs(price);
            userRepository.update(user);
        }

        // 5. Grant Item
        UserDecoration ud = UserDecoration.create(userId, decorationId);
        decorationRepository.saveUserDecoration(ud);
    }

    @Transactional
    public void giftItem(Long senderId, Long recipientUserId, Long decorationId) {
        // Only Admin Check should be done in Controller or here via user role check?
        // Service should assume caller is authorized or check it.
        // Checking sender role here for safety.

        User sender = userRepository.findById(senderId).orElseThrow(() -> new UserNotFoundException(senderId));
        if (!"ROLE_ADMIN".equals(sender.getRole())) {
            throw new SecurityException("Only admin can gift items");
        }

        // Allow gifting to self or others
        if (decorationRepository.existsUserDecoration(recipientUserId, decorationId)) {
            // Already owned - maybe just ignore or throw?
            // Throwing is better for feedback
            throw new IllegalStateException("User already owns this item");
        }

        UserDecoration ud = UserDecoration.create(recipientUserId, decorationId);
        decorationRepository.saveUserDecoration(ud);
    }
}
