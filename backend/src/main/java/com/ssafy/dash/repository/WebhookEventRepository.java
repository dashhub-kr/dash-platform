package com.ssafy.dash.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssafy.dash.entity.WebhookEvent;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    List<WebhookEvent> findByStatus(String status);
    
}
