package ru.clevertec.house.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.IllegalQueryOperationException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.repository.HouseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class HouseRepositoryImpl implements HouseRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<House> findByUUID(UUID uuid) {
        Session session = entityManager.unwrap(Session.class);

        House found = session.createQuery("""
                        SELECT h FROM House h
                            LEFT JOIN FETCH h.residents
                        WHERE h.uuid = :uuid
                        """, House.class)
                .setParameter("uuid", uuid)
                .getSingleResultOrNull();

        if (found == null) {
            return Optional.empty();
        }

        found = entityManager.createQuery("""
                        select distinct h
                        from House h
                        left join fetch h.owners
                        where h in :house
                        """, House.class)
                .setParameter("house", found)
                .getSingleResult();

        return Optional.of(found);
    }

    @Override
    public List<House> findAll(int page, int size) {
        Session session = entityManager.unwrap(Session.class);

        Query<House> query = session.createQuery("FROM House", House.class);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.list();
    }

    @Override
    public List<Person> findAllResidentsByHouseUUID(UUID houseUUID, int page, int size) {
        Session session = entityManager.unwrap(Session.class);

        NativeQuery<Person> query = session.createNativeQuery("""
                SELECT p.id, p."uuid", p.name, p.surname, p.sex, p.passport_series, p.passport_number,
                       p.create_date, p.update_date, p.house_of_residence_id
                    FROM House h JOIN Person p
                        ON h."uuid" = :residenceUUID AND h.id = p.house_of_residence_id
                LIMIT :pagesize
                OFFSET :offset""", Person.class);

        query.setParameter("residenceUUID", houseUUID);
        query.setParameter("pagesize", size);
        query.setParameter("offset", (page - 1) * size);

        return query.list();
    }

    @Override
    public boolean deleteByUUID(UUID uuid) {
        int rowsUpdated = jdbcTemplate.update("""
                DELETE FROM House WHERE "uuid"= ?""", uuid);

        return rowsUpdated > 0;
    }

    @Override
    public void update(House houseToUpdate, UUID houseUUID) {
        Session session = entityManager.unwrap(Session.class);

        Query<House> query = session.createQuery("""
                SELECT h FROM House h WHERE h.uuid = :uuid
                """, House.class);
        query.setParameter("uuid", houseUUID);
        House found = query.getSingleResultOrNull();

        if (found == null) {
            throw new IllegalQueryOperationException("The specified house does not exist");
        }

        found.setArea(houseToUpdate.getArea());
        found.setCity(houseToUpdate.getCity());
        found.setNumber(houseToUpdate.getNumber());
        found.setCountry(houseToUpdate.getCountry());
        found.setStreet(houseToUpdate.getStreet());

        session.persist(found);
        session.flush();
    }

    @Override
    public void create(House house) {
        Session session = entityManager.unwrap(Session.class);
        session.persist(house);
        session.flush();
    }
}
