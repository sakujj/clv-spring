package ru.clevertec.house.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;

import java.util.Optional;
import java.util.UUID;

public interface HouseService {

    Optional<HouseResponse> findByUUID(UUID uuid);

    Page<HouseResponse> findAll(Pageable pageable);

    Page<HouseResponse> findAllHousesByOwnerUUID(UUID ownerUUID, Pageable pageable);

    void addNewOwnerToHouse(UUID houseUUID, UUID newOwnerUUID);

    long deleteByUUID(UUID uuid);

    Optional<HouseResponse> update(HouseRequest houseToUpdateRequest, UUID houseUUID);

    HouseResponse create(HouseRequest houseRequest);
}
