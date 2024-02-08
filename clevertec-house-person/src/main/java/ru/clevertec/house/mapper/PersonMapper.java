package ru.clevertec.house.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;

@Mapper(componentModel = "spring", imports = {House.class})
public abstract class PersonMapper {

    @Mapping(target = Person.Fields.id, ignore = true)
    @Mapping(target = Person.Fields.uuid, ignore = true)
    @Mapping(target = Person.Fields.createDate, ignore = true)
    @Mapping(target = Person.Fields.updateDate, ignore = true)
    @Mapping(target = Person.Fields.ownedHouses, ignore = true)
    @Mapping(target = "houseOfResidence",
            expression = "java(House.builder().uuid(personRequest.getHouseOfResidenceUUID()).build())")
    public abstract Person fromRequest(PersonRequest personRequest);

    @Mapping(target = "houseOfResidenceUUID",
            expression = "java(person.getHouseOfResidence().getUuid())")
    public abstract PersonResponse toResponse(Person person);
}
