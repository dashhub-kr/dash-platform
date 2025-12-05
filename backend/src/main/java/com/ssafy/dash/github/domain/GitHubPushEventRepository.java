package com.ssafy.dash.github.domain;

import java.util.Optional;

public interface GitHubPushEventRepository {

    void save(GitHubPushEvent event);

    Optional<GitHubPushEvent> findByDeliveryId(String deliveryId);

    Optional<GitHubPushEvent> findNextQueued();

    void update(GitHubPushEvent event);
    
}
