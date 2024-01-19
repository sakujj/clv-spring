package ru.clevertec.house.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.exception.ServiceException;
import ru.clevertec.house.mapper.PersonMapper;
import ru.clevertec.house.model.Person;
import ru.clevertec.house.repository.PersonRepository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public Optional<PersonResponse> findByUUID(UUID uuid) {
        try {
            return personRepository.findByUUID(uuid)
                    .map(personMapper::toResponse);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

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

    public List<PersonResponse> findAllByHouseOfResidenceUUID(UUID houseOfResidenceUUID, int page, int size) {
        try {
            return personRepository.findAllByHouseOfResidenceUUID(houseOfResidenceUUID, page, size)
                    .stream()
                    .map(personMapper::toResponse)
                    .toList();
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    public void deleteByUUID(UUID uuid) throws DataIntegrityViolationException {
        try {
            personRepository.deleteByUUID(uuid);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    public void update(PersonRequest personToUpdateRequest, UUID personUUID) {
        try {
            Person personToUpdate = personMapper.fromRequest(personToUpdateRequest);
            personRepository.update(personToUpdate, personUUID);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    public void create(PersonRequest personRequest) {
        try {
            Person personToCreate = personMapper.fromRequest(personRequest);
            personRepository.create(personToCreate);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

}
