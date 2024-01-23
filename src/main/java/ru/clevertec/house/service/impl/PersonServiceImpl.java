package ru.clevertec.house.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.exception.ServiceException;
import ru.clevertec.house.mapper.HouseMapper;
import ru.clevertec.house.mapper.PersonMapper;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.repository.PersonRepository;
import ru.clevertec.house.service.PersonService;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final HouseMapper houseMapper;

    @Override
    public Optional<PersonResponse> findByUUID(UUID uuid) {
        try {
            return personRepository.findByUUID(uuid)
                    .map(personMapper::toResponse);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<PersonResponse> findAll(int page, int size) {
        try {
            return personRepository.findAll(page, size)
                    .stream()
                    .map(personMapper::toResponse)
                    .toList();
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<HouseResponse> findAllHousesByOwnerUUID(UUID ownerUUID, int page, int size) {
        try {
            return personRepository.findAllHousesByOwnerUUID(ownerUUID, page, size)
                    .stream()
                    .map(houseMapper::toResponse)
                    .toList();
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteByUUID(UUID uuid) {
        try {
            personRepository.deleteByUUID(uuid);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void update(PersonRequest personToUpdateRequest, UUID personUUID) {
        try {
            Person personToUpdate = personMapper.fromRequest(personToUpdateRequest);
            personRepository.update(personToUpdate, personUUID);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void create(PersonRequest personRequest) {
        try {
            Person personToCreate = personMapper.fromRequest(personRequest);
            personRepository.create(personToCreate);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

}
