package com.urlshortener.link_service.repository;

import com.urlshortener.link_service.entity.Link;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;



public interface LinkRepository extends JpaRepository<Link, Long> {
     List<Link> findByOwnerId(Long ownerId);
     Optional<Link> findByShortCode(String shortCode);
}
