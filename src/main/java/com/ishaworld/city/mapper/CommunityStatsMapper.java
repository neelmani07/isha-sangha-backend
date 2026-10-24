package com.ishaworld.city.mapper;

import com.ishaworld.city.dto.CommunityStatsResponse;
import com.ishaworld.city.entity.CommunityStats;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommunityStatsMapper {

    CommunityStatsResponse toResponse(CommunityStats stats);
}

