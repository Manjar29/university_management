package com.university.management.controller;

import com.university.management.dto.ShortLinkRequest;
import com.university.management.entity.ShortLink;
import com.university.management.service.ShortLinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
public class ShortLinkController {
    private final ShortLinkService service;

    public ShortLinkController(ShortLinkService service) { this.service = service; }

    @PostMapping("/api/v1/short-links")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody ShortLinkRequest request) {
        ShortLink link = service.create(request.getTargetUrl());
        return ResponseEntity.ok(Map.of("code", link.getCode(), "shortUrl", "/s/" + link.getCode(), "targetUrl", link.getTargetUrl()));
    }

    @GetMapping("/s/{code}")
    public RedirectView redirect(@PathVariable String code) {
        return new RedirectView(service.get(code).getTargetUrl());
    }
}
