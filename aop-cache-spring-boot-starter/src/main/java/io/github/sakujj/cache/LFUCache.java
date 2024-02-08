package io.github.sakujj.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Optional;

@Slf4j
public class LFUCache implements Cache {
    private final HashMap<Object, IdentifiableByUUID> entityById;
    private final HashMap<Object, Integer> countById;
    private final HashMap<Integer, LinkedHashSet<Object>> idsByCount;
    private final int capacity;
    private int min = -1;

    public synchronized int getSize() {
        return entityById.size();
    }

    @Override
    public synchronized void clear() {
        entityById.clear();
        countById.clear();
        idsByCount.clear();
        idsByCount.put(1, new LinkedHashSet<>());
        min = -1;
    }

    public LFUCache(int capacity) {
        this.capacity = capacity;
        entityById = new HashMap<>();
        countById = new HashMap<>();
        idsByCount = new HashMap<>();
        idsByCount.put(1, new LinkedHashSet<>());
    }

    public synchronized Optional<IdentifiableByUUID> getById(Object id) {
        if (!entityById.containsKey(id)) {
            log.info("CACHE MISS");
            return Optional.empty();
        }

        log.info("CACHE HIT");
        // Get the count from counts map
        int count = countById.get(id);
        // increase the counter
        countById.put(id, count + 1);
        // remove the element from the counter to linkedhashset
        idsByCount.get(count).remove(id);

        // when current min does not have any data, next one would be the min
        if (count == min && idsByCount.get(count).isEmpty()) {
            min++;
        }
        if (!idsByCount.containsKey(count + 1)) {
            idsByCount.put(count + 1, new LinkedHashSet<>());
        }
        idsByCount.get(count + 1).add(id);
        return Optional.of(entityById.get(id));
    }

    @Override
    public synchronized void removeById(Object id) {
        if (!entityById.containsKey(id)) {
            return;
        }

        int count = countById.get(id);
        countById.remove(id);
        idsByCount.get(count).remove(id);
        entityById.remove(id);
        if (count == min && idsByCount.get(count).isEmpty()) {
            while (count > -1
                    && (idsByCount.get(count) == null
                    || idsByCount.get(count).isEmpty())) {
                count--;
            }
            min = count;
        }
    }

    public synchronized void addOrUpdate(IdentifiableByUUID identifiableByUUID) {
        Object id = identifiableByUUID.getUuid();
        if (capacity == 0) {
            return;
        }
        // If key does exist, we are returning from here
        if (entityById.containsKey(id)) {
            entityById.put(id, identifiableByUUID);
            getById(id);
            return;
        }
        if (entityById.size() == capacity) {
            Object toEvict = idsByCount.get(min).iterator().next();
            idsByCount.get(min).remove(toEvict);
            entityById.remove(toEvict);
            countById.remove(toEvict);
        }
        // If the key is new, insert the value and current min should be 1 of course
        entityById.put(id, identifiableByUUID);
        countById.put(id, 1);
        min = 1;
        idsByCount.get(1).add(id);
    }
}