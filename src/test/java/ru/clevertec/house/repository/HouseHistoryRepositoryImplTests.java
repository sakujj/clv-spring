package ru.clevertec.house.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.clevertec.house.constant.ControllerConstants;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.enumeration.PersonType;
import ru.clevertec.house.testcontainer.CommonPostgresContainerInitializer;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"test"})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HouseHistoryRepositoryImplTests extends CommonPostgresContainerInitializer {

    @Autowired
    private HouseHistoryRepository houseHistoryRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private TestEntityManager testEntityManager;


    @ParameterizedTest
    @MethodSource
    void shouldFindAllByHouseUUIDAndType(UUID houseUUID, PersonType type, int expectedTotal) {
        // given
        PageRequest pageRequest = PageRequest.of(ControllerConstants.FIRST_PAGE_NUMBER, ControllerConstants.MAX_PAGE_SIZE);

        // when
        long actual = houseHistoryRepository.findAllByHouseUuidAndType(houseUUID, type, pageRequest).getTotalElements();

        // then
        assertThat(actual).isEqualTo(expectedTotal);
    }

    static Stream<Arguments> shouldFindAllByHouseUUIDAndType() {
        return Stream.of(
                Arguments.arguments(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"), PersonType.TENANT, 3),
                Arguments.arguments(UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e"), PersonType.OWNER, 2)
        );
    }

    @Test
    void shouldFindAllByHouseUUIDAndTypeTenant_whenTenantListWasUpdated() {
        // given
        PageRequest pageRequest = PageRequest.of(ControllerConstants.FIRST_PAGE_NUMBER, ControllerConstants.MAX_PAGE_SIZE);

        UUID houseUUID = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");
        House house = houseRepository.findByUuid(houseUUID).get();

        UUID personThatLivedInDifferentHouse = UUID.fromString("457ee2c4-7032-4adc-a294-c75adf1dc2bf");
        Person person = personRepository.findByUuid(personThatLivedInDifferentHouse).get();

        long expectedTotalAfterUpdate = 4;

        // when
        person.setHouseOfResidence(house);
        house.getResidents().add(person);
        personRepository.save(person);
        houseRepository.save(house);

        testEntityManager.flush();

        long actual = houseHistoryRepository.findAllByHouseUuidAndType(houseUUID, PersonType.TENANT, pageRequest).getTotalElements();

        // then
        assertThat(actual).isEqualTo(expectedTotalAfterUpdate);
    }

    @Test
    void shouldFindAllByHouseUUIDAndTypeOwner_whenOwnerListWasUpdated() {
        // given
        PageRequest pageRequest = PageRequest.of(ControllerConstants.FIRST_PAGE_NUMBER, ControllerConstants.MAX_PAGE_SIZE);

        UUID houseUUID = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");
        House house = houseRepository.findByUuid(houseUUID).get();

        UUID personThatLivedInDifferentHouse = UUID.fromString("457ee2c4-7032-4adc-a294-c75adf1dc2bf");
        Person person = personRepository.findByUuid(personThatLivedInDifferentHouse).get();

        long expectedTotalAfterUpdate = 3;

        // when
        house.getOwners().add(person);
        person.getOwnedHouses().add(house);
        houseRepository.save(house);
        personRepository.save(person);

        testEntityManager.flush();

        long actual = houseHistoryRepository.findAllByHouseUuidAndType(houseUUID, PersonType.OWNER, pageRequest)
                .getTotalElements();

        // then
        assertThat(actual).isEqualTo(expectedTotalAfterUpdate);
    }


    @ParameterizedTest
    @MethodSource
    void shouldFindAllByPersonUUIDAndType(UUID personUUID, PersonType type, int expectedTotal) {
        // given
        PageRequest pageRequest = PageRequest.of(ControllerConstants.FIRST_PAGE_NUMBER, ControllerConstants.MAX_PAGE_SIZE);

        // when
        long actual = houseHistoryRepository.findAllByPersonUuidAndType(personUUID, type, pageRequest).getTotalElements();

        // then
        assertThat(actual).isEqualTo(expectedTotal);
    }

    static Stream<Arguments> shouldFindAllByPersonUUIDAndType() {
        return Stream.of(
                Arguments.arguments(UUID.fromString("26df4783-5eae-4dd7-ae62-5249ea9c3c18"), PersonType.OWNER, 3),
                Arguments.arguments(UUID.fromString("26df4783-5eae-4dd7-ae62-5249ea9c3c18"), PersonType.TENANT, 1)
        );
    }

    @Test
    void shouldFindAllByPersonUUIDAndTypeTenant_whenTenantListWasUpdated() {
        // given
        PageRequest pageRequest = PageRequest.of(ControllerConstants.FIRST_PAGE_NUMBER, ControllerConstants.MAX_PAGE_SIZE);

        UUID houseUUID = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");
        House house = houseRepository.findByUuid(houseUUID).get();

        UUID personThatLivedInDifferentHouse = UUID.fromString("457ee2c4-7032-4adc-a294-c75adf1dc2bf");
        Person person = personRepository.findByUuid(personThatLivedInDifferentHouse).get();

        long expectedTotalAfterUpdate = 2;

        // when
        person.setHouseOfResidence(house);
        house.getResidents().add(person);
        personRepository.save(person);
        houseRepository.save(house);

        testEntityManager.flush();

        long actual = houseHistoryRepository
                .findAllByPersonUuidAndType(personThatLivedInDifferentHouse, PersonType.TENANT, pageRequest)
                .getTotalElements();

        // then
        assertThat(actual).isEqualTo(expectedTotalAfterUpdate);
    }

    @Test
    void shouldFindAllByPersonUUIDAndTypeOwner_whenOwnerListWasUpdated() {
        // given
        PageRequest pageRequest = PageRequest.of(ControllerConstants.FIRST_PAGE_NUMBER, ControllerConstants.MAX_PAGE_SIZE);

        UUID houseUUID = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");
        House house = houseRepository.findByUuid(houseUUID).get();

        UUID personThatLivedInDifferentHouse = UUID.fromString("457ee2c4-7032-4adc-a294-c75adf1dc2bf");
        Person person = personRepository.findByUuid(personThatLivedInDifferentHouse).get();

        long expectedTotalAfterUpdate = 1;

        // when
        house.getOwners().add(person);
        person.getOwnedHouses().add(house);
        houseRepository.save(house);
        personRepository.save(person);

        testEntityManager.flush();

        long actual = houseHistoryRepository
                .findAllByPersonUuidAndType(personThatLivedInDifferentHouse, PersonType.OWNER, pageRequest)
                .getTotalElements();

        // then
        assertThat(actual).isEqualTo(expectedTotalAfterUpdate);
    }
}
