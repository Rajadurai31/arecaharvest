package com.arecaharvest.mapper;

import com.arecaharvest.dto.response.OwnerResponse;
import com.arecaharvest.entity.Owner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OwnerMapper {

    @Mapping(target = "totalFields", ignore = true)
    @Mapping(target = "activeContracts", ignore = true)
    @Mapping(target = "completedContracts", ignore = true)
    OwnerResponse toResponse(Owner owner);
}
