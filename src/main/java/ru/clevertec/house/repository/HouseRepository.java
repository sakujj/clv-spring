package ru.clevertec.house.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.clevertec.house.entity.House;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HouseRepository extends JpaRepository<House, Long> {

    Optional<House> findByUuid(UUID uuid);

    Page<House> findAll(Pageable pageable);

    @Query(value = """
            SELECT
                h.id,
                h."uuid",
                h.street,
                h.city,
                h.country,
                h.number,
                h.area,
                h.create_date
            FROM Person p
            JOIN owner_to_owned_house oto
                ON p.id = oto.person_id
            JOIN House h
                ON h.id = oto.house_id
            WHERE p."uuid" = :ownerUuid
            """, nativeQuery = true)
    Page<House> findAllHousesByOwnerUuid(UUID ownerUuid, Pageable pageable);

    long deleteByUuid(UUID uuid);

    House save(House house);
}
