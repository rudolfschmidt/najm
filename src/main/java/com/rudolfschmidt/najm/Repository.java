package com.rudolfschmidt.najm;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
	/**
	 *
	 * @param entity
	 */
    void save(T entity);

	/**
	 *
	 * @param entities
	 */
    void saveAll(Iterable<T> entities);

	/**
	 *
	 * @return
	 */
    List<T> findAll();

	/**
	 *
	 * @param id
	 * @return
	 */
    T findOne(String id);

	/**
	 *
	 * @param id
	 * @return
	 */
    boolean exists(String id);

	/**
	 *
	 * @return
	 */
    long count();

	/**
	 *
	 * @param id
	 * @return
	 */
    long delete(String id);

	/**
	 *
	 * @param entity
	 * @return
	 */
    long delete(T entity);

	/**
	 *
	 * @return
	 */
    long deleteAll();
}
