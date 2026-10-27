package com.arecaharvest.mapper;

import com.arecaharvest.dto.response.WorkerResponse;
import com.arecaharvest.entity.Worker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkerMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totalEarned", ignore = true)
    @Mapping(target = "totalPaid", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    WorkerResponse toResponse(Worker worker);
}
