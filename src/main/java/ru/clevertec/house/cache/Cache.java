package ru.clevertec.house.cache;


import ru.clevertec.house.entity.IdentifiableByUUID;

import java.util.Optional;

public interface Cache {
    void addOrUpdate(IdentifiableByUUID identifiableByUUID);

    Optional<IdentifiableByUUID> getById(Object id);

    void removeById(Object id);

    int getSize();

    void clear();
}
