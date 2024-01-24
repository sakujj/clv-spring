package ru.clevertec.house.service;

import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HouseService {

    Optional<HouseResponse> findByUUID(UUID uuid);

    List<HouseResponse> findAll(int page, int size);

    List<HouseResponse> findAllHousesByOwnerUUID(UUID ownerUUID, int page, int size);

    void deleteByUUID(UUID uuid);

    void update(HouseRequest houseToUpdateRequest, UUID houseUUID);

    void create(HouseRequest houseRequest);
}
