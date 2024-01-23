package ru.clevertec.house.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.IllegalQueryOperationException;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.repository.PersonRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class PersonRepositoryImpl implements PersonRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
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

    @Override
    public List<Person> findAll(int page, int size) {
        Session session = entityManager.unwrap(Session.class);

        Query<Person> query = session.createQuery("""
                FROM Person p LEFT JOIN FETCH p.houseOfResidence""", Person.class);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.list();
    }

    @Override
    public List<House> findAllHousesByOwnerUUID(UUID ownerUUID, int page, int size) {
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

    @Override
    public boolean deleteByUUID(UUID uuid) {
        Session session = entityManager.unwrap(Session.class);

        MutationQuery query = session.createNativeMutationQuery("""
                DELETE FROM Person p WHERE p."uuid" = :uuid
                """);
        query.setParameter("uuid", uuid);

        return query.executeUpdate() > 0;
    }

    @Override
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

        session.persist(person);
        session.flush();
    }

    @Override
    public void update(Person personToUpdate, UUID personUUID) {
        Session session = entityManager.unwrap(Session.class);
        Query<Person> query = session.createQuery("""
                SELECT p FROM Person p WHERE p.uuid = :uuid
                """, Person.class);
        query.setParameter("uuid", personUUID);
        Person currentlyPresentVersion = query.getSingleResultOrNull();

        if (currentlyPresentVersion == null) {
            throw new IllegalQueryOperationException("The specified person does not exist");
        }

        if (willUpdateChangePerson(personToUpdate, currentlyPresentVersion)) {
            return;
        }
        updateCurrentlyPresentVersionFields(personToUpdate, currentlyPresentVersion, session);

        session.persist(currentlyPresentVersion);
        session.flush();
    }

    private static void updateCurrentlyPresentVersionFields(Person personToUpdate, Person currentlyPresentVersion, Session session) {
        Query<House> queryForHouseOfResidence = session.createQuery("""
                SELECT h FROM House h WHERE h.uuid = :uuid
                """, House.class);
        queryForHouseOfResidence.setParameter("uuid", personToUpdate.getHouseOfResidence().getUuid());
        House newHouseOfResidence = queryForHouseOfResidence.getSingleResult();

        currentlyPresentVersion.setName(personToUpdate.getName());
        currentlyPresentVersion.setSurname(personToUpdate.getSurname());
        currentlyPresentVersion.setSex(personToUpdate.getSex());
        currentlyPresentVersion.setPassportNumber(personToUpdate.getPassportNumber());
        currentlyPresentVersion.setPassportSeries(personToUpdate.getPassportSeries());
        currentlyPresentVersion.setHouseOfResidence(newHouseOfResidence);
        
        currentlyPresentVersion.setUpdateDate(LocalDateTime.now());
    }

    private static boolean willUpdateChangePerson(Person personToUpdate, Person found) {
        return found.getName().equals(personToUpdate.getName())
                && found.getSurname().equals(personToUpdate.getSurname())
                && found.getSex().equals(personToUpdate.getSex())
                && found.getPassportSeries().equals(personToUpdate.getPassportSeries())
                && found.getPassportNumber().equals(personToUpdate.getPassportNumber())
                && found.getHouseOfResidence().getUuid().equals(personToUpdate.getHouseOfResidence().getUuid());
    }
}
