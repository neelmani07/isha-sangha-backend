package com.ishaworld.program.repository;

import com.ishaworld.program.entity.Program;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    List<Program> findByIdIn(Collection<Long> ids);

    List<Program> findTop10ByNameContainingIgnoreCase(String name);
}

