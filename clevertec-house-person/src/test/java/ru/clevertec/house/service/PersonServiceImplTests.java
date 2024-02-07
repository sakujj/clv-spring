package ru.clevertec.house.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.enumeration.Sex;
import ru.clevertec.house.mapper.PersonMapper;
import ru.clevertec.house.repository.HouseRepository;
import ru.clevertec.house.repository.PersonRepository;
import ru.clevertec.house.test.util.HouseTestBuilder;
import ru.clevertec.house.test.util.PersonTestBuilder;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTests {

    @Mock
    private HouseRepository houseRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonMapper personMapper;

    @InjectMocks
    private PersonServiceImpl personServiceImpl;

    @Test
    void shouldFindPersonById_whenRepositoryFoundById() {
        // given
        UUID uuidToFindBy = UUID.fromString("ede5bf7c-6029-48c2-b225-95afc0fe1b36");
        Person expectedFromRepo = PersonTestBuilder.aPerson().build();
        PersonResponse expected = PersonTestBuilder.aPerson().buildResponse();

        when(personRepository.findByUuid(uuidToFindBy))
                .thenReturn(Optional.of(expectedFromRepo));
        when(personMapper.toResponse(expectedFromRepo))
                .thenReturn(expected);

        // when
        Optional<PersonResponse> actual = personServiceImpl.findByUUID(uuidToFindBy);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    void shouldNotFindPersonById_whenRepositoryDidNotFindById() {
        // given
        UUID uuidToFindBy = UUID.fromString("ede5bf7c-6029-48c2-b225-95afc0fe1b36");

        when(personRepository.findByUuid(uuidToFindBy))
                .thenReturn(Optional.empty());

        // when
        Optional<PersonResponse> actual = personServiceImpl.findByUUID(uuidToFindBy);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAll() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Person> expectedContentFromRepo = List.of(
                PersonTestBuilder.aPerson()
                        .build(),
                PersonTestBuilder.aPerson()
                        .withUuid(UUID.fromString("3e020b18-18dd-4cd5-94d5-37ac24482d70"))
                        .build(),
                PersonTestBuilder.aPerson()
                        .withUuid(UUID.fromString("77bcde0b-069d-41db-beab-a7e6580c1a24"))
                        .build()
        );

        List<PersonResponse> expectedContent = expectedContentFromRepo.stream()
                .map(p -> PersonTestBuilder.aPerson()
                        .withUuid(p.getUuid())
                        .buildResponse())
                .toList();

        IntStream.range(0, 3).forEach(i ->
                when(personMapper.toResponse(expectedContentFromRepo.get(i))).
                        thenReturn(expectedContent.get(i)));

        Page<Person> expectedFromRepo = new PageImpl<>(expectedContentFromRepo);
        when(personRepository.findAll(pageable))
                .thenReturn(expectedFromRepo);

        // when
        Page<PersonResponse> actual = personServiceImpl.findAll(pageable);
        System.out.println(actual.getContent());

        // then
        assertThat(actual.getNumberOfElements()).isPositive();
        assertThat(actual.getContent()).isEqualTo(expectedContent);
    }

    @Test
    void shouldCreate() {
        // given
        PersonRequest personRequestToCreate = PersonTestBuilder.aPerson().buildRequest();
        Person personFromRequest = PersonTestBuilder.aPerson().build();
        PersonResponse expected = PersonTestBuilder.aPerson().buildResponse();

        House houseOfResidence = HouseTestBuilder.aHouse()
                .withUuid(personRequestToCreate.getHouseOfResidenceUUID())
                .build();

        when(houseRepository.findByUuid(personRequestToCreate.getHouseOfResidenceUUID()))
                .thenReturn(Optional.of(houseOfResidence));
        when(personMapper.fromRequest(personRequestToCreate))
                .thenReturn(personFromRequest);
        when(personRepository.save(personFromRequest))
                .thenReturn(personFromRequest);
        when(personMapper.toResponse(personFromRequest))
                .thenReturn(expected);

        // when
        PersonResponse actual = personServiceImpl.create(personRequestToCreate);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldUpdate() {
        // given
        House newHouseOfResidence = HouseTestBuilder.aHouse().build();

        PersonTestBuilder personFromRequestBuilder = PersonTestBuilder.aPerson()
                .withName("UPDATED NAME")
                .withSurname("UPDATED SURNAME")
                .withSex(Sex.FEMALE)
                .withHouseOfResidence(newHouseOfResidence)
                .withPassportNumber("NEW PASSPORT NUMBER")
                .withPassportSeries("NEW PASSPORT SERIES");

        PersonRequest personToUpdateRequest = personFromRequestBuilder.buildRequest();
        Person personFromRequest = personFromRequestBuilder.build();

        PersonTestBuilder existingPersonBuilder = PersonTestBuilder.aPerson();
        Person existingPerson = existingPersonBuilder.build();

        LocalDateTime updatedLocalDateTime = LocalDateTime.of(2024, Month.JUNE, 12, 12, 12, 21,123456789);
        try (MockedStatic<LocalDateTime> localDateTimeMockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            localDateTimeMockedStatic.when(LocalDateTime::now)
                    .thenReturn(updatedLocalDateTime);

            Person existingPersonWithUpdatedFields = existingPersonBuilder
                    .withName(personFromRequest.getName())
                    .withSurname(personFromRequest.getSurname())
                    .withSex(personFromRequest.getSex())
                    .withPassportSeries(personFromRequest.getPassportSeries())
                    .withPassportNumber(personFromRequest.getPassportNumber())
                    .withUpdateDate(updatedLocalDateTime)
                    .build();

            PersonTestBuilder expectedPersonBuilder = PersonTestBuilder.aPerson()
                    .withName(personFromRequest.getName())
                    .withSurname(personFromRequest.getSurname())
                    .withSex(personFromRequest.getSex())
                    .withPassportSeries(personFromRequest.getPassportSeries())
                    .withPassportNumber(personFromRequest.getPassportNumber());
            Person expectedFromSave = expectedPersonBuilder.build();
            PersonResponse expected = expectedPersonBuilder.buildResponse();

            when(personRepository.findByUuid(personFromRequest.getUuid()))
                    .thenReturn(Optional.of(existingPerson));
            when(personMapper.fromRequest(personToUpdateRequest))
                    .thenReturn(personFromRequest);
            when(houseRepository.findByUuid(personToUpdateRequest.getHouseOfResidenceUUID()))
                    .thenReturn(Optional.of(newHouseOfResidence));
            when(personRepository.save(existingPersonWithUpdatedFields))
                    .thenReturn(expectedFromSave);
            when(personMapper.toResponse(expectedFromSave))
                    .thenReturn(expected);

            // when
            Optional<PersonResponse> actual = personServiceImpl.update(personToUpdateRequest, personFromRequest.getUuid());

            // then
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(expected);
        }
    }

    @Test
    void shouldNotUpdate_whenNotFoundInRepository() {
        // given
        PersonRequest personRequest = PersonTestBuilder.aPerson().buildRequest();
        UUID uuid = PersonTestBuilder.aPerson().getUuid();


        when(personRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.empty());

        // when
        Optional<PersonResponse> actual = personServiceImpl.update(personRequest, uuid);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldDelete() {
        // given
        long expected = 666L;
        UUID uuidToDeleteBy = PersonTestBuilder.aPerson().getUuid();
        when(personRepository.deleteByUuid(uuidToDeleteBy))
                .thenReturn(expected);

        // when
        long actual = personServiceImpl.deleteByUUID(uuidToDeleteBy);

        // then
        assertThat(actual).isEqualTo(expected);
    }

}
