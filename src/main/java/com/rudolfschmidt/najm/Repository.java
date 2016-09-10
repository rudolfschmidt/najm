package com.rudolfschmidt.najm;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    void save(T entity);
    void saveAll(Iterable<T> entities);
    List<T> findAll();
    Optional<T> findOne(String id);
    boolean exists(String id);
    long count();
    long delete(String id);
    long delete(T entity);
    long deleteAll();
}
