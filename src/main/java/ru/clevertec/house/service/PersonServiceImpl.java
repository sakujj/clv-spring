package ru.clevertec.house.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.cache.aop.CacheableCreate;
import ru.clevertec.house.cache.aop.CacheableDeleteByUUID;
import ru.clevertec.house.cache.aop.CacheableFindByUUID;
import ru.clevertec.house.cache.aop.CacheableUpdateByUUID;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.mapper.PersonMapper;
import ru.clevertec.house.repository.HouseRepository;
import ru.clevertec.house.repository.PersonRepository;
import ru.clevertec.house.service.PersonService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final HouseRepository houseRepository;

    private final PersonMapper personMapper;

    @Override
    @CacheableFindByUUID
    public Optional<PersonResponse> findByUUID(UUID uuid) {

        return personRepository.findByUuid(uuid)
                .map(personMapper::toResponse);
    }

    @Override
    public Page<PersonResponse> findAll(Pageable pageable) {

            return personRepository.findAll(pageable)
                    .map(personMapper::toResponse);
    }

    @Override
    public Page<PersonResponse> findAllResidentsByHouseOfResidenceUUID(UUID houseOfResidenceUUID, Pageable pageable) {

            return personRepository.findAllResidentsByHouseOfResidenceUuid(houseOfResidenceUUID, pageable)
                    .map(personMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheableDeleteByUUID
    public long deleteByUUID(UUID uuid) {

            return personRepository.deleteByUuid(uuid);
    }

    @Override
    @Transactional
    @CacheableCreate
    public PersonResponse create(PersonRequest personRequest) {

            Person personToCreate = personMapper.fromRequest(personRequest);

            Optional<House> optionalHouseOfResidence = houseRepository.findByUuid(personRequest.getHouseOfResidenceUUID());
            House houseOfResidence = optionalHouseOfResidence.orElseThrow(() ->
                    new RuntimeException("the specified house of residence does not exist"));

            personToCreate.setHouseOfResidence(houseOfResidence);

            Person saved = personRepository.save(personToCreate);

            return personMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheableUpdateByUUID
    public Optional<PersonResponse> update(PersonRequest personToUpdateRequest, UUID personUUID) {

            Optional<Person> optionalPerson = personRepository.findByUuid(personUUID);
            if (optionalPerson.isEmpty()) {
                return Optional.empty();
            }

            Person existingPerson = optionalPerson.get();
            Person personToUpdate = personMapper.fromRequest(personToUpdateRequest);

            if (!willUpdateChangePerson(personToUpdate, existingPerson)) {
                return Optional.of(personMapper.toResponse(existingPerson));
            }

            setFieldsToUpdateOnExistingPerson(personToUpdate, existingPerson, houseRepository);

            return Optional.of(personMapper.toResponse(personRepository.save(existingPerson)));
    }

    private static void setFieldsToUpdateOnExistingPerson(Person personToUpdate,
                                                          Person existingPerson,
                                                          HouseRepository houseRepository) {

        House newHouseOfResidence = houseRepository.findByUuid(personToUpdate.getHouseOfResidence().getUuid())
                .orElseThrow(() -> new RuntimeException("the specified house of residence does not exist"));

        existingPerson.setName(personToUpdate.getName());
        existingPerson.setSurname(personToUpdate.getSurname());
        existingPerson.setSex(personToUpdate.getSex());
        existingPerson.setPassportNumber(personToUpdate.getPassportNumber());
        existingPerson.setPassportSeries(personToUpdate.getPassportSeries());
        existingPerson.setHouseOfResidence(newHouseOfResidence);

        existingPerson.setUpdateDate(LocalDateTime.now());
    }

    private static boolean willUpdateChangePerson(Person personToUpdate, Person currentlyExisting) {
        return !currentlyExisting.getName().equals(personToUpdate.getName())
                || !currentlyExisting.getSurname().equals(personToUpdate.getSurname())
                || !currentlyExisting.getSex().equals(personToUpdate.getSex())
                || !currentlyExisting.getPassportSeries().equals(personToUpdate.getPassportSeries())
                || !currentlyExisting.getPassportNumber().equals(personToUpdate.getPassportNumber())
                || !currentlyExisting.getHouseOfResidence().getUuid().equals(personToUpdate.getHouseOfResidence().getUuid());
    }

}
