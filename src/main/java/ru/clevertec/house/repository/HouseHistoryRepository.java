package ru.clevertec.house.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.clevertec.house.entity.HouseHistory;
import ru.clevertec.house.enumeration.PersonType;

import java.util.UUID;

@Repository
public interface HouseHistoryRepository extends JpaRepository<HouseHistory, Long> {

    @EntityGraph(attributePaths = {"person.houseOfResidence"})
    Page<HouseHistory> findAllByHouseUuidAndType(UUID uuid, PersonType type, Pageable pageable);

    @EntityGraph(attributePaths = {"house"})
    Page<HouseHistory> findAllByPersonUuidAndType(UUID uuid, PersonType type, Pageable pageable);
}
