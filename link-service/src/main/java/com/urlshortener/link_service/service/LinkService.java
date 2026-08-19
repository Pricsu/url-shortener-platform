package com.urlshortener.link_service.service;

import com.urlshortener.link_service.dto.LinkRequest;
import com.urlshortener.link_service.dto.LinkResponse;
import com.urlshortener.link_service.dto.UrlResponse;
import com.urlshortener.link_service.entity.Link;
import com.urlshortener.link_service.repository.LinkRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.urlshortener.link_service.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    private Link findById(Long id){
        return linkRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Link not found: " + id ));
    }

    public Long extractUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal p){
            return p.id();
        }else {
            throw new IllegalStateException("Expected authenticated UserPrincipal but found none");
        }
    }

    public LinkResponse create(LinkRequest request) {
        Link link = new Link();

        String randomShortCode = RandomStringUtils.secure().next(7, true,true);
        while (linkRepository.findByShortCode(randomShortCode).isPresent()){
            randomShortCode = RandomStringUtils.secure().next(7, true,true);
        }

        link.setShortCode(randomShortCode);
        link.setOriginalUrl(request.getOriginalUrl());
        link.setOwnerId(extractUserId());
        link.setCreatedAt(LocalDateTime.now());
        link.setClickCount(0L);
        link.setClickLimit(request.getClickLimit());
        linkRepository.save(link);
        return LinkResponse.fromEntity(link);
    }

    public List<LinkResponse> findMyLinks(){
        return linkRepository.findByOwnerId(extractUserId()).stream()
                .map(LinkResponse::fromEntity).toList();

    }

    public void deleteLink(Long linkId){
        Link link = findById(linkId);
        if (link.getOwnerId().equals(extractUserId())) {
            linkRepository.delete(link);
        }else {
            throw new AccessDeniedException("Action not permitted");
        }
    }

    public UrlResponse getOriginalUrl(String shortCode){
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));

        if (link.getClickLimit() != null && link.getClickCount() >= link.getClickLimit()){
            throw new IllegalArgumentException("Link click limit exceeded");
        }
        Long next = link.getClickCount() + 1;
        link.setClickCount(next);
        linkRepository.save(link);
        return UrlResponse.fromEntity(link);

    }

}
