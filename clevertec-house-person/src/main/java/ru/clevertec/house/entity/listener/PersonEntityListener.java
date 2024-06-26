package ru.clevertec.house.entity.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import ru.clevertec.house.entity.Person;

import java.time.LocalDateTime;
import java.util.UUID;

public class PersonEntityListener {

    @PrePersist
    void beforePersist(Person person) {
        UUID randomUUID = UUID.randomUUID();
        person.setUuid(randomUUID);

        LocalDateTime createDate = LocalDateTime.now();
        person.setCreateDate(createDate);

        LocalDateTime updateDate = createDate;
        person.setUpdateDate(updateDate);
    }
}
