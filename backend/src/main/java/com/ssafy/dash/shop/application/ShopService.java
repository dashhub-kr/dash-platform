package com.ssafy.dash.shop.application;

import com.ssafy.dash.decoration.domain.Decoration;
import com.ssafy.dash.decoration.domain.DecorationRepository;
import com.ssafy.dash.decoration.domain.UserDecoration;
import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
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
    private final NotificationService notificationService;

    public ShopService(UserRepository userRepository, DecorationRepository decorationRepository,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.decorationRepository = decorationRepository;
        this.notificationService = notificationService;
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
        // 1. Validate Sender
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException(senderId));

        // 2. Validate Recipient
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new UserNotFoundException(recipientUserId));

        // 3. Validate Item
        Decoration item = decorationRepository.findById(decorationId).orElse(null);
        if (item == null || Boolean.FALSE.equals(item.getIsActive())) {
            throw new IllegalArgumentException("Item not found or inactive");
        }

        // 4. Check Ownership (Recipient)
        if (decorationRepository.existsUserDecoration(recipientUserId, decorationId)) {
            throw new IllegalStateException("Recipient already owns this item");
        }

        // 5. Check Balance & Deduct from Sender
        int price = item.getPrice() != null ? item.getPrice() : 0;
        if (price > 0) {
            if (sender.getLogCount() < price) {
                throw new IllegalStateException("Not enough logs");
            }
            sender.useLogs(price);
            userRepository.update(sender);
        }

        // 6. Grant Item to Recipient
        UserDecoration ud = UserDecoration.create(recipientUserId, decorationId);
        decorationRepository.saveUserDecoration(ud);

        // 7. Notify Recipient
        String message = sender.getUsername() + "님이 [" + item.getName() + "] 아이템을 선물했습니다!";
        // Using SYSTEM type for now as generic notification
        notificationService.send(recipientUserId, message, "/shop", NotificationType.SYSTEM);
    }
}
