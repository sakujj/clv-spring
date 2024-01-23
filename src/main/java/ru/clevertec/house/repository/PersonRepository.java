package ru.clevertec.house.repository;

import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository {

    Optional<Person> findByUUID(UUID uuid);

    List<Person> findAll(int page, int size);

    List<House> findAllHousesByOwnerUUID(UUID ownerUUID, int page, int size);

    boolean deleteByUUID(UUID uuid);

    void update(Person personToUpdate, UUID personUUID);

    void create(Person person);
}
