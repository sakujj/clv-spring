package ru.clevertec.house.entity.listener;

import jakarta.persistence.PrePersist;
import ru.clevertec.house.entity.House;

import java.time.LocalDateTime;
import java.util.UUID;

public class HouseEntityListener {

    @PrePersist
    private void beforePersist(House house) {
        LocalDateTime createDate = LocalDateTime.now();
        house.setCreateDate(createDate);

        UUID randomUUID = UUID.randomUUID();
        house.setUuid(randomUUID);
    }

}
