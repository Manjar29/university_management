package com.university.management.service;

import com.university.management.entity.ShortLink;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.ShortLinkRepository;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class ShortLinkService {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();
    private final ShortLinkRepository repository;

    public ShortLinkService(ShortLinkRepository repository) { this.repository = repository; }

    public ShortLink create(String targetUrl) {
        ShortLink link = new ShortLink();
        String code;
        do {
            StringBuilder value = new StringBuilder(7);
            for (int i = 0; i < 7; i++) value.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            code = value.toString();
        } while (repository.findByCode(code).isPresent());
        link.setCode(code);
        link.setTargetUrl(targetUrl);
        return repository.save(link);
    }

    public ShortLink get(String code) {
        return repository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Short link not found."));
    }
}
