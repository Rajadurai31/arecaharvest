package com.arecaharvest.mapper;

import com.arecaharvest.dto.response.ExpenseResponse;
import com.arecaharvest.entity.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "fieldId", source = "field.id")
    @Mapping(target = "fieldName", source = "field.fieldName")
    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    ExpenseResponse toResponse(Expense expense);
}
