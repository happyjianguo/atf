package jie.atf.core.dao;

import jie.atf.core.domain.AtWorkflow;

/**
 * Data access layer for the AtWorkflow
 * 
 * @author Jie
 *
 */
public interface IAtWorkflowDAO {
	/**
	 * 持久化AT工作流
	 * 
	 * @param workflow
	 */
	void create(AtWorkflow workflow);

	/**
	 * 
	 * @param name
	 *            AT工作流名
	 * @return AT工作流
	 */
	AtWorkflow find(String name);

	/**
	 * 更新AT工作流
	 * 
	 * @param workflow
	 */
	void update(AtWorkflow workflow);
}
