package ru.clevertec.house.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonResponse;

import java.util.UUID;

public interface HouseHistoryService {

    Page<HouseResponse> findAllHousesWherePersonLivedByPersonUuid(UUID personUuid, Pageable pageable);

    Page<HouseResponse> findAllHousesWhichPersonOwnedByPersonUuid(UUID personUuid, Pageable pageable);

    Page<PersonResponse> findAllPeopleThatLivedInHouseByHouseUuid(UUID houseUuid, Pageable pageable);

    Page<PersonResponse> findAllPeopleThatOwnedHouseByHouseUuid(UUID houseUuid, Pageable pageable);
}
