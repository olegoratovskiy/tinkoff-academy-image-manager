package com.example.imageapi.domain;

import com.example.imageapi.dto.ImageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Image mapper.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImageMapper {

    @Mapping(target = "filename", source = "name")
    @Mapping(target = "imageId", source = "fileId")
    ImageResponse toDto(Image image);
}