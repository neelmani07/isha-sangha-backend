package com.ishaworld.social.repository;

import com.ishaworld.common.enums.EntityType;
import com.ishaworld.social.entity.SocialLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {

    List<SocialLink> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
}

