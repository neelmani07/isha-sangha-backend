package com.ishaworld.program.mapper;

import com.ishaworld.program.dto.ProgramResponse;
import com.ishaworld.program.entity.Program;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProgramMapper {

    ProgramResponse toResponse(Program program);
}

