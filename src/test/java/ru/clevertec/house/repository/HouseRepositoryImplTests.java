package ru.clevertec.house.repository;

import jakarta.persistence.EntityManager;
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
import ru.clevertec.house.testcontainer.CommonPostgresContainerInitializer;
import ru.clevertec.house.test.util.HouseTestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles({"test"})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HouseRepositoryImplTests extends CommonPostgresContainerInitializer {
    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @ParameterizedTest
    @MethodSource
    void shouldFindAllPaginated(int page, int size, int expectedListLength) {
        // given, when
        Page<House> actual = houseRepository.findAll(PageRequest.of(page - 1, size));

        // then
        System.out.println(actual.getContent());
        assertThat(actual.getNumberOfElements()).isEqualTo(expectedListLength);
    }

    static Stream<Arguments> shouldFindAllPaginated() {
        return Stream.of(
                Arguments.arguments(1, 2, 2),
                Arguments.arguments(5, 2, 0),
                Arguments.arguments(2, 3, 2),
                Arguments.arguments(1, 7, 5)
        );
    }

    @Test
    void shouldFindHouseByUUID() {
        // given
        UUID uuidToSearchBy = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");
        House expected = HouseTestBuilder.aHouse()
                .withId(1L)
                .withUuid(uuidToSearchBy)
                .withArea(64d)
                .withCountry("Belarus")
                .withCity("Grodno")
                .withStreet("Lenina st.")
                .withNumber(101)
                .withCreateDate(LocalDateTime.parse("2022-10-29T06:12:12.123"))
                .build();
        EntityManager entityManager;

        // when
        Optional<House> actual = houseRepository.findByUuid(uuidToSearchBy);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);

    }


    @ParameterizedTest
    @MethodSource
    void shouldFindAllHousesByOwnerUUIDPaginated(UUID uuidToFindBy, int page, int size, int expectedListLength) {
        // given, when
        Page<House> actual = houseRepository.findAllHousesByOwnerUuid(uuidToFindBy, PageRequest.of(page - 1, size));

        // then
        assertThat(actual.getNumberOfElements()).isEqualTo(expectedListLength);

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
        UUID uuidToDeleteBy = UUID.fromString("e89895ef-ca4c-433b-87e8-3ead2646fed1");

        // when
        long actualRowsUpdated = houseRepository.deleteByUuid(uuidToDeleteBy);

        testEntityManager.flush();

        Optional<House> deletedHouse = houseRepository.findByUuid(uuidToDeleteBy);

        // then
        assertThat(actualRowsUpdated).isPositive();
        assertThat(deletedHouse).isEmpty();
    }

    @Test
    void shouldUpdate() {
        // given
        UUID uuid = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");

        House foundHouseToUpdate = houseRepository.findByUuid(uuid).get();

        House houseToUpdate = HouseTestBuilder.aHouse()
                .withUuid(uuid)
                .withId(foundHouseToUpdate.getId())
                .withCreateDate(foundHouseToUpdate.getCreateDate())
                .withResidents(List.of())
                .build();

        // when
        House savedHouseFromRepo = houseRepository.save(houseToUpdate);
        testEntityManager.flush();

        House updatedHouseFromDb = houseRepository.findByUuid(houseToUpdate.getUuid()).get();

        // then

        assertThat(savedHouseFromRepo).isEqualTo(updatedHouseFromDb);
        assertThat(savedHouseFromRepo).isEqualTo(houseToUpdate);
    }

    @Test
    void shouldCreate() {
        // given
        UUID savedUUID = UUID.fromString("8d2ea5e7-7fbf-4427-8f79-2f534574b15c");
        House houseToCreate = HouseTestBuilder
                .aHouse()
                .withId(null)
                .withUuid(null)
                .build();
        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID)
                    .thenReturn(savedUUID);

            LocalDateTime dateBeforeCreate = LocalDateTime.now();

            // when
            House savedHouseFromRepo = houseRepository.save(houseToCreate);

            testEntityManager.flush();

            House savedHouseFromDb = houseRepository.findByUuid(savedUUID).get();

            // then
            assertThat(savedHouseFromRepo).isEqualTo(houseToCreate);
            assertThat(savedHouseFromRepo).isEqualTo(savedHouseFromDb);

            assertThat(dateBeforeCreate).isBefore(savedHouseFromDb.getCreateDate());
        }
    }
}
