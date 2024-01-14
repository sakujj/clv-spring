package ru.clevertec.house.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.IllegalQueryOperationException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.model.House;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class HouseRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;

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

    public List<House> findAll(int page, int size) {
        Session session = entityManager.unwrap(Session.class);

        Query<House> query = session.createQuery("FROM House", House.class);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.list();
    }

    public List<House> findAllByOwnerUUID(UUID ownerUUID, int page, int size) {
        Session session = entityManager.unwrap(Session.class);

        NativeQuery<House> query = session.createNativeQuery("""
                SELECT h.id, h."uuid", h.area, h.country, h.city, h.street, h.number, h.create_date
                    FROM Person p JOIN Owner_OwnedHouse o
                        ON p."uuid" = :ownerUUID AND p.id = person_id
                    JOIN House h
                        ON h.id = o.house_id
                LIMIT :pagesize
                OFFSET :offset""", House.class);

        query.setParameter("ownerUUID", ownerUUID);
        query.setParameter("pagesize", size);
        query.setParameter("offset", (page - 1) * size);

        return query.list();
    }

    public boolean deleteByUUID(UUID uuid) throws DataIntegrityViolationException {
        int rowsUpdated = jdbcTemplate.update("""
                DELETE FROM House WHERE "uuid"= ?""", uuid);

        return rowsUpdated > 0;
    }

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

    public void create(House house) {
        Session session = entityManager.unwrap(Session.class);

        UUID randomUUID = UUID.randomUUID();
        LocalDateTime createDate = LocalDateTime.now();
        house.setUuid(randomUUID);
        house.setCreateDate(createDate);

        session.persist(house);
        session.flush();
    }
}
