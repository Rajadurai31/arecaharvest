package com.arecaharvest.mapper;

import com.arecaharvest.dto.response.FieldResponse;
import com.arecaharvest.entity.Field;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FieldMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.name")
    @Mapping(target = "harvestedQuantityKg", ignore = true)
    @Mapping(target = "remainingQuantityKg", ignore = true)
    @Mapping(target = "progressPercent", ignore = true)
    @Mapping(target = "activeContractId", ignore = true)
    FieldResponse toResponse(Field field);
}
