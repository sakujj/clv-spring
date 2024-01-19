package ru.clevertec.house.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.IllegalQueryOperationException;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import ru.clevertec.house.model.House;
import ru.clevertec.house.model.Person;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PersonRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    public Optional<Person> findByUUID(UUID uuid) {
        Session session = entityManager.unwrap(Session.class);

        Query<Person> query = session.createQuery("""
                SELECT p FROM Person p
                    LEFT JOIN FETCH p.ownedHouses
                WHERE p.uuid = :uuid""", Person.class);
        query.setParameter("uuid", uuid);
        Person found = query.getSingleResultOrNull();

        return Optional.ofNullable(found);
    }

    public List<Person> findAll(int page, int size) {
        Session session = entityManager.unwrap(Session.class);

        Query<Person> query = session.createQuery("""
                FROM Person p LEFT JOIN FETCH p.houseOfResidence""", Person.class);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.list();
    }

    public List<Person> findAllByHouseOfResidenceUUID(UUID houseOfResidenceUUID, int page, int size) {
        Session session = entityManager.unwrap(Session.class);

        NativeQuery<Person> query = session.createNativeQuery("""
                SELECT p.id, p."uuid", p.name, p.surname, p.sex, p.passport_series, p.passport_number,
                       p.create_date, p.update_date, p.house_of_residence_id
                    FROM House h JOIN Person p
                        ON h."uuid" = :residenceUUID AND h.id = p.house_of_residence_id
                LIMIT :pagesize
                OFFSET :offset""", Person.class);

        query.setParameter("residenceUUID", houseOfResidenceUUID);
        query.setParameter("pagesize", size);
        query.setParameter("offset", (page - 1) * size);

        return query.list();
    }

    public boolean deleteByUUID(UUID uuid) throws DataIntegrityViolationException {
        Session session = entityManager.unwrap(Session.class);

        MutationQuery query = session.createNativeMutationQuery("""
                DELETE FROM Person p WHERE p."uuid" = :uuid
                """);
        query.setParameter("uuid", uuid);

        return query.executeUpdate() > 0;
    }

    public void update(Person personToUpdate, UUID personUUID) {
        Session session = entityManager.unwrap(Session.class);
        Query<Person> query = session.createQuery("""
                SELECT p FROM Person p WHERE p.uuid = :uuid
                """, Person.class);
        query.setParameter("uuid", personUUID);
        Person found = query.getSingleResultOrNull();

        if (found == null) {
            throw new IllegalQueryOperationException("The specified person does not exist");
        }

        if (found.getName().equals(personToUpdate.getName())
        && found.getSurname().equals(personToUpdate.getSurname())
        && found.getSex().equals(personToUpdate.getSex())) {
            return;
        }

        found.setName(personToUpdate.getName());
        found.setSurname(personToUpdate.getSurname());
        found.setSex(personToUpdate.getSex());
        found.setUpdateDate(LocalDateTime.now());

        session.persist(found);
        session.flush();
    }

    public void create(Person person) {
        Session session = entityManager.unwrap(Session.class);

        UUID uuid = person.getHouseOfResidence().getUuid();

        House foundHouseOfResidence = session.createQuery("""
                        SELECT h FROM House h WHERE h.uuid = :uuid""", House.class)
                .setParameter("uuid", uuid)
                .getSingleResultOrNull();

        if (foundHouseOfResidence == null) {
            throw new IllegalQueryOperationException("The specified house of residence does not exist");
        }
        person.setHouseOfResidence(foundHouseOfResidence);

        UUID randomUUID = UUID.randomUUID();
        LocalDateTime createDate = LocalDateTime.now();
        person.setUuid(randomUUID);
        person.setCreateDate(createDate);
        person.setUpdateDate(createDate);

        session.persist(person);
        session.flush();
    }
}
