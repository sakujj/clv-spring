package io.github.sakujj.cache;


import java.util.Optional;

public interface Cache {
    void addOrUpdate(IdentifiableByUUID identifiableByUUID);

    Optional<IdentifiableByUUID> getById(Object id);

    void removeById(Object id);

    int getSize();

    void clear();
}
