package com.ssafy.dash.github.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.github.domain.GitHubPushEvent;
import com.ssafy.dash.github.domain.GitHubPushEventRepository;
import com.ssafy.dash.github.infrastructure.mapper.GitHubPushEventMapper;

@Repository
public class GitHubPushEventRepositoryImpl implements GitHubPushEventRepository {

    private final GitHubPushEventMapper mapper;

    public GitHubPushEventRepositoryImpl(GitHubPushEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(GitHubPushEvent event) {
        mapper.insert(event);
    }

    @Override
    public Optional<GitHubPushEvent> findByDeliveryId(String deliveryId) {
        return Optional.ofNullable(mapper.selectByDeliveryId(deliveryId));
    }
    
}
