package ru.clevertec.house.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.entity.House;

@Mapper(componentModel = "spring")
public interface HouseMapper {

    @Mapping(target = House.Fields.id, ignore = true)
    @Mapping(target = House.Fields.uuid, ignore = true)
    @Mapping(target = House.Fields.owners, ignore = true)
    @Mapping(target = House.Fields.residents, ignore = true)
    @Mapping(target = House.Fields.createDate, ignore = true)
    House fromRequest(HouseRequest houseRequest);

    HouseResponse toResponse(House house);
}