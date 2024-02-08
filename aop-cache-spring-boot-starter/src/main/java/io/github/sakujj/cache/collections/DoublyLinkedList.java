package io.github.sakujj.cache.collections;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.function.Predicate;

public class DoublyLinkedList<T> {
    private static final String MESSAGE_ON_EMPTY = "DLList is empty";

    Node<T> head;
    Node<T> tail;
    @Getter
    int size = 0;

    public Node<T> addFirst(T value) {
        Node<T> nodeToAdd = new Node<>(value);
        size++;

        if (head == null && tail == null) {
            head = nodeToAdd;
            tail = nodeToAdd;
            return nodeToAdd;
        }

        head.prev = nodeToAdd;
        nodeToAdd.next = head;
        head = nodeToAdd;
        return nodeToAdd;
    }

    public void addNodeFirst(Node<T> nodeToAdd) {
        size++;

        if (head == null && tail == null) {
            head = nodeToAdd;
            tail = nodeToAdd;
            return;
        }

        head.prev = nodeToAdd;
        nodeToAdd.next = head;
        head = nodeToAdd;
    }


    public T getLast() {
        if (tail == null) {
            throw new IllegalStateException(MESSAGE_ON_EMPTY);
        }

        return tail.value;
    }

    public T getFirst() {
        if (head == null) {
            throw new IllegalStateException(MESSAGE_ON_EMPTY);
        }

        return head.value;
    }

    public void removeLast() {
        if (tail == null) {
            throw new IllegalStateException(MESSAGE_ON_EMPTY);
        }

        removeNode(tail);
    }

    public void removeByCondition(Predicate<T> removePredicate) {
        var curNode = head;
        while (curNode != null) {
            if (!removePredicate.test(curNode.value)) {
                curNode = curNode.next;
                continue;
            }

            var nodeToRemove = curNode;
            curNode = curNode.next;
            removeNode(nodeToRemove);
        }
    }

    public void removeNode(Node<T> node) {
        Objects.requireNonNull(node);

        Node<T> next = node.next;
        Node<T> prev = node.prev;

        node.next = null;
        node.prev = null;

        if (head == node && tail == node) {
            size = 0;
            head = null;
            tail = null;
            return;
        }

        size--;

        if (head == node) {
            head = next;
        }

        if (tail == node) {
            tail = prev;
        }

        if (next != null) {
            next.prev = prev;
        }

        if (prev != null) {
            prev.next = next;
        }
    }

    @EqualsAndHashCode
    public static class Node<V> {
        private Node(V value) {
            this.value = value;
        }
        private final V value;
        @EqualsAndHashCode.Exclude
        private Node<V> prev;
        @EqualsAndHashCode.Exclude
        private Node<V> next;
    }
}
