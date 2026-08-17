package com.urlshortener.link_service.controller;

import com.urlshortener.link_service.dto.LinkRequest;
import com.urlshortener.link_service.dto.LinkResponse;
import com.urlshortener.link_service.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class LinkController {

    private final LinkService linkService;


    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("/api/links")
    public List<LinkResponse> findLinks(){
        return linkService.findMyLinks();
    }

    @PostMapping("/api/links")
    public LinkResponse create(@Valid @RequestBody LinkRequest linkRequest){
        return linkService.create(linkRequest);
    }

    @DeleteMapping("/api/links/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        linkService.deleteLink(id);
        return ResponseEntity.noContent().build();
    }

}
