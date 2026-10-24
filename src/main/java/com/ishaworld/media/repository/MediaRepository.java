package com.ishaworld.media.repository;

import com.ishaworld.common.enums.EntityType;
import com.ishaworld.media.entity.Media;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
}

