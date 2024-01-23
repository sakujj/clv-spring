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
import ru.clevertec.house.test.util.HouseTestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@Rollback
@Transactional
public class HouseRepositoryImplTests extends AbstractDatabaseIntegrationTests {
    @Autowired
    private HouseRepository houseRepository;

    @ParameterizedTest
    @MethodSource
    void shouldFindAllPaginated(int page, int size, int expectedListLength) {
        // given, when
        List<House> actual = houseRepository.findAll(page, size);

        // then
        assertThat(actual.size()).isEqualTo(expectedListLength);
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
                .withArea(64)
                .withCountry("Belarus")
                .withCity("Grodno")
                .withStreet("Lenina st.")
                .withNumber(101)
                .withCreateDate(LocalDateTime.parse("2022-10-29T06:12:12.123"))
                .build();

        // when
        Optional<House> actual = houseRepository.findByUUID(uuidToSearchBy);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);

    }

    @ParameterizedTest
    @MethodSource
    void shouldFindAllResidentsByHouseUUIDPaginated(UUID uuidToFindBy, int page, int size, int expectedListLength) {
        // given, when
        List<Person> actual = houseRepository.findAllResidentsByHouseUUID(uuidToFindBy, page, size);

        // then
        assertThat(actual.size()).isEqualTo(expectedListLength);
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
        UUID uuidToDeleteBy = UUID.fromString("e89895ef-ca4c-433b-87e8-3ead2646fed1");

        // when
        boolean actual = houseRepository.deleteByUUID(uuidToDeleteBy);
        Optional<House> deletedHouse = houseRepository.findByUUID(uuidToDeleteBy);

        // then
        assertThat(actual).isTrue();
        assertThat(deletedHouse).isEmpty();
    }

    @Test
    void shouldUpdate() {
        // given
        House houseToUpdate = HouseTestBuilder.aHouse()
                .withUuid(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"))
                .build();

        // when
        houseRepository.update(houseToUpdate, houseToUpdate.getUuid());

        // then
        Optional<House> updatedHouseOptional = houseRepository.findByUUID(houseToUpdate.getUuid());
        assertThat(updatedHouseOptional).isPresent();

        House updatedHouse = updatedHouseOptional.get();
        assertThat(updatedHouse.getResidents()).isNotNull();

        houseToUpdate.setId(updatedHouse.getId());
        houseToUpdate.setCreateDate(updatedHouse.getCreateDate());
        assertThat(updatedHouse).isEqualTo(houseToUpdate);
    }

    @Test
    void shouldCreate() {
        // given
        UUID savedUUID = UUID.fromString("8d2ea5e7-7fbf-4427-8f79-2f534574b15c");
        House houseToCreate = HouseTestBuilder
                .aHouse()
                .withId(null)
                .withUuid(null)
                .withOwners(null)
                .withResidents(null)
                .withCreateDate(null)
                .build();
        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)){
            mockedStatic.when(UUID::randomUUID)
                    .thenReturn(savedUUID);

            LocalDateTime dateBeforeCreate = LocalDateTime.now();

            // when
            houseRepository.create(houseToCreate);

            // then
            Optional<House> createdHouseOptional = houseRepository.findByUUID(savedUUID);
            assertThat(createdHouseOptional).isPresent();

            House createdHouse = createdHouseOptional.get();
            houseToCreate.setId(createdHouse.getId());
            houseToCreate.setCreateDate(createdHouse.getCreateDate());
            assertThat(createdHouse).isEqualTo(houseToCreate);

            assertThat(dateBeforeCreate).isBefore(createdHouse.getCreateDate());
        }
    }
}
