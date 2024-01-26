package ru.clevertec.house.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.clevertec.house.entity.Person;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    @EntityGraph(attributePaths = {"houseOfResidence"})
    Optional<Person> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"houseOfResidence"})
    Page<Person> findAllResidentsByHouseOfResidenceUuid(UUID houseOfResidenceUuid, Pageable pageable);

    @EntityGraph(attributePaths = {"houseOfResidence"})
    Page<Person> findAll(Pageable pageable);

    long deleteByUuid(UUID uuid);

    Person save(Person person);
}
