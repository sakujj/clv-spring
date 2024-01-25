package ru.clevertec.house.cache;

import ru.clevertec.house.entity.IdentifiableByUUID;

import java.util.Optional;

public class NoOpCache implements Cache{
    @Override
    public void addOrUpdate(IdentifiableByUUID identifiableByUUID) {

    }

    @Override
    public Optional<IdentifiableByUUID> getById(Object id) {
        return Optional.empty();
    }

    @Override
    public void removeById(Object id) {

    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void clear() {

    }
}
