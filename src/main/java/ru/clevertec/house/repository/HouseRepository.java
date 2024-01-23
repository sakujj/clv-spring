package ru.clevertec.house.repository;

import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HouseRepository {

    Optional<House> findByUUID(UUID uuid);

    List<House> findAll(int page, int size);

    List<Person> findAllResidentsByHouseUUID(UUID houseUUID, int page, int size);

    boolean deleteByUUID(UUID uuid);

    void update(House houseToUpdate, UUID houseUUID);

    void create(House house);
}
