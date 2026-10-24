package com.ishaworld.teacher.repository;

import com.ishaworld.teacher.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepositoryCustom {

    Page<Teacher> findByCityFilters(
            Long cityId,
            String q,
            Long programId,
            Double lat,
            Double lng,
            boolean sortByDistance,
            Pageable pageable
    );
}
