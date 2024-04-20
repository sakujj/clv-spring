package ru.clevertec.house.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;

import java.util.Optional;
import java.util.UUID;

public interface PersonService {

    Optional<PersonResponse> findByUUID(UUID uuid);

    Page<PersonResponse> findAll(Pageable pageable);

    Page<PersonResponse> findAllResidentsByHouseOfResidenceUUID(UUID houseOfResidenceUUID, Pageable pageable);

    long deleteByUUID(UUID uuid);

    Optional<PersonResponse> update(PersonRequest personToUpdateRequest, UUID personUUID);

    PersonResponse create(PersonRequest personRequest);
}
