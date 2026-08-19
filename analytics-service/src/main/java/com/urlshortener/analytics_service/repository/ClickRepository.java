package com.urlshortener.analytics_service.repository;

import com.urlshortener.analytics_service.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickRepository extends JpaRepository<ClickEvent, Long> {
}
