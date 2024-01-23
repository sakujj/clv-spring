package ru.clevertec.house.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.enumeration.Sex;
import ru.clevertec.house.test.util.PersonTestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@Rollback
@Transactional
public class PersonRepositoryImplTests extends AbstractDatabaseIntegrationTests {
    @Autowired
    private PersonRepository personRepository;

    @ParameterizedTest
    @MethodSource
    void shouldFindAllPaginated(int page, int size, int expectedListLength) {
        // given, when
        List<Person> actual = personRepository.findAll(page, size);

        // then
        assertThat(actual.size()).isEqualTo(expectedListLength);
    }

    static Stream<Arguments> shouldFindAllPaginated() {
        return Stream.of(
                Arguments.arguments(5, 2, 2),
                Arguments.arguments(1, 7, 7),
                Arguments.arguments(1, 11, 10)
        );
    }

    @Test
    void shouldFindHouseByUUID() {
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
        Optional<Person> actual = personRepository.findByUUID(uuidToSearchBy);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);

    }

    @ParameterizedTest
    @MethodSource
    void shouldFindAllHousesByOwnerUUIDPaginated(UUID uuidToFindBy, int page, int size, int expectedListLength) {
        // given, when
        List<House> actual = personRepository.findAllHousesByOwnerUUID(uuidToFindBy, page, size);

        // then
        assertThat(actual.size()).isEqualTo(expectedListLength);

    }

    static Stream<Arguments> shouldFindAllHousesByOwnerUUIDPaginated() {
        return Stream.of(
                Arguments.arguments(UUID.fromString("26df4783-5eae-4dd7-ae62-5249ea9c3c18"), 2, 2, 1),
                Arguments.arguments(UUID.fromString("26df4783-5eae-4dd7-ae62-5249ea9c3c18"), 1, 5, 3),
                Arguments.arguments(UUID.fromString("236d7005-b86b-4697-b783-5eec2bc04dfa"), 1, 4, 2),
                Arguments.arguments(UUID.fromString("236d7005-b86b-4697-b783-5eec2bc04dfa"), 2, 1, 1),
                Arguments.arguments(UUID.fromString("1dd72b7d-9296-457d-b3e6-7a33ffe3abb2"), 1, 7, 0)
        );
    }

    @Test
    void shouldDeleteByUUID() {
        // given
        UUID uuidToDeleteBy = UUID.fromString("13985e64-a4f1-42eb-b23e-8d8c12a3c14b");

        // when
        boolean actual = personRepository.deleteByUUID(uuidToDeleteBy);
        Optional<Person> deletedPerson = personRepository.findByUUID(uuidToDeleteBy);

        // then
        assertThat(actual).isTrue();
        assertThat(deletedPerson).isEmpty();
    }

    @Test
    void shouldUpdate() {
        // given
        Person personToUpdate = PersonTestBuilder.aPerson()
                .withUuid(UUID.fromString("95f3178e-f6a5-4ca6-b4f2-f0780a3f74b0"))
                .withHouseOfResidence(House.builder()
                        .uuid(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"))
                        .build())
                .build();

        LocalDateTime dateBeforeUpdate = LocalDateTime.now();
        // when
        personRepository.update(personToUpdate, personToUpdate.getUuid());

        // then
        Optional<Person> updatedPersonOptional = personRepository.findByUUID(personToUpdate.getUuid());
        assertThat(updatedPersonOptional).isPresent();

        Person updatedPerson = updatedPersonOptional.get();
        assertThat(updatedPerson.getOwnedHouses()).isNotNull();

        LocalDateTime updatedDate = updatedPerson.getUpdateDate();
        assertThat(dateBeforeUpdate).isBefore(updatedDate);

        personToUpdate.setId(updatedPerson.getId());
        personToUpdate.setCreateDate(updatedPerson.getCreateDate());
        personToUpdate.setUpdateDate(updatedPerson.getUpdateDate());
        personToUpdate.setPassportSeries(updatedPerson.getPassportSeries());
        personToUpdate.setPassportNumber(updatedPerson.getPassportNumber());
        assertThat(updatedPerson).isEqualTo(personToUpdate);
    }

    @Test
    void shouldNotChange_UpdateDate_IfUpdateDoesNotChangeState() {
        // given
        UUID uuid = UUID.fromString("95f3178e-f6a5-4ca6-b4f2-f0780a3f74b0");
        Person personToUpdate = PersonTestBuilder.aPerson()
                .withUuid(uuid)
                .withName("Pavel")
                .withSurname("Ivanov")
                .withSex(Sex.MALE)
                .withPassportSeries("MP")
                .withPassportNumber("1234567890123")
                .withHouseOfResidence(House.builder()
                        .uuid(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"))
                        .build())
                .build();

        LocalDateTime updateDateBeforeUpdate = personRepository.findByUUID(uuid).get()
                .getUpdateDate();
        // when
        personRepository.update(personToUpdate, personToUpdate.getUuid());

        LocalDateTime updateDateAfterUpdate = personRepository.findByUUID(uuid).get()
                .getUpdateDate();

        // then
        assertThat(updateDateBeforeUpdate).isEqualTo(updateDateAfterUpdate);
    }

    @Test
    void shouldCreate() {
        // given
        UUID savedUUID = UUID.fromString("8d2ea5e7-7fbf-4427-8f79-2f534574b15c");
        House houseOfResidence = House.builder()
                .uuid(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"))
                .build();

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
            personRepository.create(personToCreate);

            // then
            Optional<Person> createdPersonOptional = personRepository.findByUUID(savedUUID);
            assertThat(createdPersonOptional).isPresent();

            Person createdPerson = createdPersonOptional.get();
            personToCreate.setId(createdPerson.getId());
            personToCreate.setCreateDate(createdPerson.getCreateDate());
            assertThat(createdPerson).isEqualTo(personToCreate);

            assertThat(dateBeforeCreate).isBefore(createdPerson.getCreateDate());
            assertThat(dateBeforeCreate).isBefore(createdPerson.getUpdateDate());
        }
    }
}
