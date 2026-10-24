package com.ishaworld.social.mapper;

import com.ishaworld.social.dto.SocialLinkResponse;
import com.ishaworld.social.entity.SocialLink;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SocialLinkMapper {

    SocialLinkResponse toResponse(SocialLink socialLink);
}

