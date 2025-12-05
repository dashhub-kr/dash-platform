package com.ssafy.dash.github.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.dash.github.domain.GitHubPushEvent;

@Mapper
public interface GitHubPushEventMapper {

    void insert(GitHubPushEvent event);

    GitHubPushEvent selectByDeliveryId(@Param("deliveryId") String deliveryId);

    GitHubPushEvent selectNextQueued();

    void update(GitHubPushEvent event);
    
}
