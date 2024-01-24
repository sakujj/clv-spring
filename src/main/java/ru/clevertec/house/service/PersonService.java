package ru.clevertec.house.service;

import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonService {

    Optional<PersonResponse> findByUUID(UUID uuid);

    List<PersonResponse> findAll(int page, int size);

    List<PersonResponse> findAllResidentsByHouseUUID(UUID houseUUID, int page, int size);

    void deleteByUUID(UUID uuid);

    void update(PersonRequest personToUpdateRequest, UUID personUUID);

    void create(PersonRequest personRequest);
}
