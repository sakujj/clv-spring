package io.github.sakujj.cache;


import io.github.sakujj.cache.collections.DoublyLinkedList;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Optional;

/**
 * LRU cache implementation
 */
@Slf4j
@ToString
public class LRUCache implements Cache {
    private DoublyLinkedList<IdentifiableByUUID> entities;
    private final HashMap<Object, DoublyLinkedList.Node<IdentifiableByUUID>> nodesById;
    private final int capacity;

    /**
     * Used to get current cache size.
     *
     * @return number of entities in cache
     */
    public synchronized int getSize() {
        return entities.getSize();
    }

    @Override
    public void clear() {
        entities = new DoublyLinkedList<>();
        nodesById.clear();
    }

    public LRUCache(int capacity) {
        this.capacity = capacity;
        entities = new DoublyLinkedList<>();
        nodesById = new HashMap<>();
    }

    /**
     * Used to add or update entity with cache.
     *
     * @param identifiableByUUID instance to add or update
     */
    public synchronized void addOrUpdate(IdentifiableByUUID identifiableByUUID) {
        if (capacity == 0) {
            return;
        }

        if (entities.getSize() == capacity) {
            var lruEntity = entities.getLast();
            Object lruId = lruEntity.getUuid();

            entities.removeLast();
            nodesById.remove(lruId);
        }

        entities.removeByCondition(e -> e.getUuid().equals(identifiableByUUID.getUuid()));
        var node = entities.addFirst(identifiableByUUID);
        nodesById.put(identifiableByUUID.getUuid(), node);
    }

    /**
     * Used to get by id from cache.
     *
     * @param id id to get by
     */
    public synchronized Optional<IdentifiableByUUID> getById(Object id) {
        if (!nodesById.containsKey(id)) {
            log.info("CACHE MISS");
            return Optional.empty();
        }

        log.info("CACHE HIT");

        var nodeToGet = nodesById.get(id);
        entities.removeNode(nodeToGet);
        entities.addNodeFirst(nodeToGet);
        return Optional.of(entities.getFirst());
    }

    /**
     * Used to remove by id from cache.
     *
     * @param id id to remove by
     */
    public synchronized void removeById(Object id) {
        if (!nodesById.containsKey(id)) {
            return;
        }

        var nodeToDelete = nodesById.get(id);
        entities.removeNode(nodeToDelete);
        nodesById.remove(id);
    }


}
