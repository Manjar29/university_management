package com.university.management.repository;

import com.university.management.entity.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {
    Optional<ShortLink> findByCode(String code);
}
