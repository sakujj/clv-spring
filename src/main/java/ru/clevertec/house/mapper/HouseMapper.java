package ru.clevertec.house.mapper;

import org.mapstruct.Mapper;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.model.House;

@Mapper(componentModel = "spring")
public interface HouseMapper {
    House fromRequest(HouseRequest houseRequest);
    HouseResponse toResponse(House house);
}