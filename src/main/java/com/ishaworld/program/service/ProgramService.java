package com.ishaworld.program.service;

import com.ishaworld.common.api.PageResponse;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.program.dto.ProgramResponse;
import com.ishaworld.program.dto.ProgramUpsertRequest;
import com.ishaworld.program.entity.Program;
import com.ishaworld.program.mapper.ProgramMapper;
import com.ishaworld.program.repository.ProgramRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ProgramMapper programMapper;

    public ProgramService(ProgramRepository programRepository, ProgramMapper programMapper) {
        this.programRepository = programRepository;
        this.programMapper = programMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProgramResponse> findAll(Pageable pageable) {
        return PageResponse.from(programRepository.findAll(pageable).map(programMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ProgramResponse findById(Long id) {
        return programMapper.toResponse(getProgram(id));
    }

    @Transactional
    public ProgramResponse create(ProgramUpsertRequest request) {
        Program program = new Program();
        apply(program, request);
        return programMapper.toResponse(programRepository.save(program));
    }

    @Transactional
    public ProgramResponse update(Long id, ProgramUpsertRequest request) {
        Program program = getProgram(id);
        apply(program, request);
        return programMapper.toResponse(programRepository.save(program));
    }

    @Transactional
    public void delete(Long id) {
        programRepository.delete(getProgram(id));
    }

    public Program getProgram(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Programme not found"));
    }

    public List<Program> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return programRepository.findByIdIn(ids);
    }

    private void apply(Program program, ProgramUpsertRequest request) {
        program.setName(request.name());
        program.setDescription(request.description());
    }
}

