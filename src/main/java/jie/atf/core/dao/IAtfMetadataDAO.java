package jie.atf.core.dao;

import java.util.List;

import jie.atf.core.dto.AtWorkflowMetadata;

/**
 * Data access layer for the AtWorkflowMetadata
 * 
 * @author Jie
 *
 */
public interface IAtfMetadataDAO {
	/**
	 * 持久化AT工作流蓝图
	 * 
	 * @param workflowMetadata
	 *            AT工作流蓝图
	 */
	void create(AtWorkflowMetadata workflowMetadata);

	/**
	 * 
	 * @param name
	 *            工作流名(nickname)
	 * @return 最新版本的AT工作流蓝图
	 */
	AtWorkflowMetadata findLatest(String name);

	/**
	 * 
	 * @param name
	 *            工作流名(nickname)
	 * @param version
	 *            版本号
	 * @return 指定版本的AT工作流蓝图
	 */
	AtWorkflowMetadata find(String name, Long version);

	/**
	 * 
	 * @param name
	 *            工作流名(nickname)
	 * @return 所有版本的AT工作流蓝图
	 */
	List<AtWorkflowMetadata> findAllVersions(String name);

	/**
	 * 更新AT工作流蓝图
	 * 
	 * @param workflowMetadata
	 */
	void update(AtWorkflowMetadata workflowMetadata);
}
