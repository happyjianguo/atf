package jie.atf.core.dao;

import jie.atf.core.domain.AtTask;

/**
 * Data access layer for the AtTask
 * 
 * @author Jie
 *
 */
public interface IAtTaskDAO {
	/**
	 * 持久化AT任务
	 * 
	 * @param task
	 */
	void create(AtTask task);

	/**
	 * 
	 * @param id
	 * @return AT任务
	 */
	AtTask find(Long id);

	/**
	 * 更新AT任务
	 * 
	 * @param task
	 */
	void update(AtTask task);
}
