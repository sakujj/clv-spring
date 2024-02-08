package ru.clevertec.house.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.enumeration.Sex;
import ru.clevertec.house.testcontainer.CommonPostgresContainerInitializer;
import ru.clevertec.house.test.util.PersonTestBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ActiveProfiles({"test"})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonRepositoryImplTests extends CommonPostgresContainerInitializer {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @MethodSource
    @ParameterizedTest
    void shouldFindAllPaginated(int page, int size, int expectedListLength) {
        // given, when
        Page<Person> actual = personRepository.findAll(PageRequest.of(page - 1, size));

        // then
        assertThat(actual.getNumberOfElements()).isEqualTo(expectedListLength);
    }

    static Stream<Arguments> shouldFindAllPaginated() {
        return Stream.of(
                Arguments.arguments(5, 2, 2),
                Arguments.arguments(1, 7, 7),
                Arguments.arguments(1, 11, 10)
        );
    }

    @Test
    void shouldFindPersonByUUID() {
        // given
        UUID uuidToSearchBy = UUID.fromString("95f3178e-f6a5-4ca6-b4f2-f0780a3f74b0");
        Person expected = PersonTestBuilder.aPerson()
                .withId(1L)
                .withUuid(uuidToSearchBy)
                .withName("Pavel")
                .withSurname("Ivanov")
                .withSex(Sex.MALE)
                .withPassportSeries("MP")
                .withPassportNumber("1234567890123")
                .withCreateDate(LocalDateTime.parse("2020-09-09T10:00:00.000"))
                .withUpdateDate(LocalDateTime.parse("2020-09-09T10:00:00.000"))
                .build();

        // when
        Optional<Person> actual = personRepository.findByUuid(uuidToSearchBy);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get().getHouseOfResidence().getUuid())
                .isEqualTo(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"));
        assertThat(actual.get()).isEqualTo(expected);

    }

    @ParameterizedTest
    @MethodSource
    void shouldFindAllResidentsByHouseUUIDPaginated(UUID uuidToFindBy, int page, int size, int expectedListLength) {
        // given, when
        Page<Person> actual = personRepository.findAllResidentsByHouseOfResidenceUuid(
                uuidToFindBy,
                PageRequest.of(page - 1, size));

        // then
        assertThat(actual.getNumberOfElements()).isEqualTo(expectedListLength);
    }

    static Stream<Arguments> shouldFindAllResidentsByHouseUUIDPaginated() {
        return Stream.of(
                Arguments.arguments(UUID.fromString("e89895ef-ca4c-433b-87e8-3ead2646fed1"), 1, 10, 0),
                Arguments.arguments(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"), 1, 10, 3),
                Arguments.arguments(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"), 2, 2, 1),
                Arguments.arguments(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"), 2, 3, 0),
                Arguments.arguments(UUID.fromString("01e311bf-ec36-47ca-91e6-e67c959c57cc"), 1, 10, 4)
        );
    }

    @Test
    void shouldDeleteByUUID() {
        // given
        UUID uuidToDeleteBy = UUID.fromString("13985e64-a4f1-42eb-b23e-8d8c12a3c14b");

        // when
        long actual = personRepository.deleteByUuid(uuidToDeleteBy);

        testEntityManager.flush();

        Optional<Person> deletedPerson = personRepository.findByUuid(uuidToDeleteBy);

        // then
        assertThat(actual).isPositive();
        assertThat(deletedPerson).isEmpty();
    }

    @Test
    void shouldUpdate() {
        // given
        UUID newHouseOfResidenceUUID = UUID.fromString("e89895ef-ca4c-433b-87e8-3ead2646fed1");
        House newHouseOfResidence = houseRepository.findByUuid(newHouseOfResidenceUUID).get();

        UUID personFromDbToUpdateUUID = UUID.fromString("95f3178e-f6a5-4ca6-b4f2-f0780a3f74b0");
        Person personFromDbToUpdate = personRepository.findByUuid(personFromDbToUpdateUUID).get();

        Person personWithUpdatedFields = PersonTestBuilder.aPerson().build();

        personFromDbToUpdate.setName(personWithUpdatedFields.getName());
        personFromDbToUpdate.setSurname(personWithUpdatedFields.getSurname());
        personFromDbToUpdate.setSex(personWithUpdatedFields.getSex());
        personFromDbToUpdate.setPassportNumber(personWithUpdatedFields.getPassportNumber());
        personFromDbToUpdate.setPassportSeries(personWithUpdatedFields.getPassportSeries());
        personFromDbToUpdate.setHouseOfResidence(newHouseOfResidence);

        // when
        Person savedFromRepo = personRepository.save(personFromDbToUpdate);
        testEntityManager.flush();

        Person savedFromDb = personRepository.findByUuid(savedFromRepo.getUuid()).get();

        // then
        assertThat(savedFromRepo.getOwnedHouses()).isNotNull();

        assertThat(savedFromRepo).isEqualTo(personFromDbToUpdate);
        assertThat(savedFromRepo).isEqualTo(savedFromDb);
    }


    @Test
    void shouldCreate() {
        // given
        UUID savedUUID = UUID.fromString("8d2ea5e7-7fbf-4427-8f79-2f534574b15c");
        UUID houseOfResidenceUUID = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");

        House houseOfResidence = houseRepository.findByUuid(houseOfResidenceUUID).get();

        Person personToCreate = PersonTestBuilder
                .aPerson()
                .withId(null)
                .withUuid(null)
                .withCreateDate(null)
                .withUpdateDate(null)
                .withHouseOfResidence(houseOfResidence)
                .build();
        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(UUID::randomUUID)
                    .thenReturn(savedUUID);

            LocalDateTime dateBeforeCreate = LocalDateTime.now();

            // when
            Person createdPersonFromRepo = personRepository.save(personToCreate);

            testEntityManager.flush();

            Person createdPersonFromDb = personRepository.findByUuid(savedUUID).get();

            // then
            assertThat(createdPersonFromDb).isEqualTo(personToCreate);
            assertThat(createdPersonFromDb).isEqualTo(createdPersonFromRepo);

            assertThat(dateBeforeCreate).isBefore(createdPersonFromDb.getCreateDate());
            assertThat(dateBeforeCreate).isBefore(createdPersonFromDb.getUpdateDate());
        }
    }

    @Test
    void shouldMonitorUniquePassportSeriesAndPassportNumberCombination() {
        // given
        House anyHouse = houseRepository.findAll(PageRequest.of(0, 1))
                .getContent().get(0);

        Person personFirst = PersonTestBuilder.aPerson()
                .withId(null)
                .withUuid(null)
                .withHouseOfResidence(anyHouse)
                .build();
        Person personWithTheSamePassportNumberAndPassportSeriesLikePersonFirst = PersonTestBuilder.aPerson()
                .withId(null)
                .withUuid(null)
                .withPassportNumber(personFirst.getPassportNumber())
                .withPassportSeries(personFirst.getPassportSeries())
                .withHouseOfResidence(anyHouse)
                .build();

        // when, then
        testEntityManager.persist(personFirst);

        assertThatThrownBy(() -> testEntityManager.persist(personWithTheSamePassportNumberAndPassportSeriesLikePersonFirst))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
