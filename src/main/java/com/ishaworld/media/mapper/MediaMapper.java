package com.ishaworld.media.mapper;

import com.ishaworld.media.dto.MediaResponse;
import com.ishaworld.media.entity.Media;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaMapper {

    MediaResponse toResponse(Media media);
}

