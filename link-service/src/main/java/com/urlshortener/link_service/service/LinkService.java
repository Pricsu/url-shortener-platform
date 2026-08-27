package com.urlshortener.link_service.service;

import com.urlshortener.link_service.dto.LinkRequest;
import com.urlshortener.link_service.dto.LinkResponse;
import com.urlshortener.link_service.dto.UrlResponse;
import com.urlshortener.link_service.entity.Link;
import com.urlshortener.link_service.repository.LinkRepository;
import org.apache.commons.lang3.RandomStringUtils;


import org.springframework.data.redis.core.Cursor;

import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.urlshortener.link_service.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class LinkService {

    private final LinkRepository linkRepository;

    private final StringRedisTemplate redisTemplate;

    public LinkService(LinkRepository linkRepository, StringRedisTemplate redisTemplate) {
        this.linkRepository = linkRepository;
        this.redisTemplate = redisTemplate;
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

        saveAll(link, randomShortCode);

        return LinkResponse.fromEntity(link);
    }

    public List<LinkResponse> findMyLinks(){
        return linkRepository.findByOwnerId(extractUserId()).stream()
                .map(LinkResponse::fromEntity).toList();

    }

    public void deleteLink(Long linkId){
        Link link = findById(linkId);
        String shortCode = link.getShortCode();
        if (link.getOwnerId().equals(extractUserId())) {
            linkRepository.delete(link);

            delete("url:"+shortCode);
            delete("limit:"+shortCode);
            delete("count:"+shortCode);

        }else {
            throw new AccessDeniedException("Action not permitted");
        }
    }

    public UrlResponse getOriginalUrl(String shortCode){

        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        Long clickCount = link.getClickCount();
        if (readValue("url:"+ shortCode) == null){
            saveAll(link, shortCode);
        }


        if (!Objects.equals(readValue("limit:" + shortCode), "-")){
            Long clickLimit = Long.parseLong(readValue("limit:"+shortCode));
            Long total = clickCount + Long.parseLong(readValue("count:"+shortCode));
            if (total >= clickLimit){
                throw new IllegalArgumentException("Link click limit exceeded");
            }
        }
        incrementCounter("count:" + shortCode);
        return new UrlResponse(readValue("url:" + shortCode));
    }

    public void saveAll(Link link, String shortCode){
        saveWithExpiration("url:" + shortCode, link.getOriginalUrl());
        saveWithExpiration("limit:" + shortCode, link.getClickLimit() == null ? "-" : link.getClickLimit().toString());
        saveWithExpiration("count:" + shortCode, "0");
    }

    public void saveWithExpiration(String key, String value){
        redisTemplate.opsForValue().set(key,value, 10, TimeUnit.MINUTES);
    }

    public String readValue(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public Long incrementCounter(String key){
        return redisTemplate.opsForValue().increment(key);
    }

    public String getAndDelete(String key){
        return redisTemplate.opsForValue().getAndDelete(key);
    }

    public void delete(String key){
        redisTemplate.delete(key);
    }

    @Scheduled(fixedDelay = 100000)
    public void flushClickCounts(){
        String prefix = "count:";
        Set<String> keys = scanWithPrefix(prefix);


        for (String key : keys){
            String shortCode = key.substring(prefix.length());

            String rawCount = getAndDelete(key);

            if (rawCount == null){
                continue;
            }
            Long clickCount = Long.valueOf(rawCount);

            Link link = linkRepository.findByShortCode(shortCode)
                    .orElseThrow(() -> new IllegalArgumentException("Link nopt found"));

            link.setClickCount(link.getClickCount() + clickCount);

            linkRepository.save(link);
            System.out.println("Flush job running at " + LocalDateTime.now());
        }
    }

    public Set<String> scanWithPrefix(String prefix){

        Set<String> keys = new HashSet<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(prefix + "*")
                .count(1000)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)){
            while (cursor.hasNext()){
                keys.add(cursor.next());
            }
        }catch (Exception e){
            throw new RuntimeException("Error executing Redis SCAN operation");
        }

        return keys;
    }
}
