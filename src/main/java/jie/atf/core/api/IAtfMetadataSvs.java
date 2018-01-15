package jie.atf.core.api;

import jie.atf.core.dto.AtWorkflowMetadata;
import jie.atf.core.service.AtfMetadataBuilder;
import jie.atf.core.utils.exception.AtfException;

/**
 * Metadata service for the ATF
 * 
 * @author Jie
 *
 */
public interface IAtfMetadataSvs {
	/**
	 * 注册AT工作流蓝图：CREATE or UPDATE
	 * 
	 * @param workflowMetadata
	 * @throws AtfException
	 */
	void register(AtWorkflowMetadata workflowMetadata) throws AtfException;

	/**
	 * 更新AT工作流蓝图：UPDATE
	 * 
	 * @param workflowMetadata
	 * @throws AtfException
	 */
	void update(AtWorkflowMetadata workflowMetadata) throws AtfException;

	/**
	 * 
	 * @param name
	 *            工作流名(nickname)
	 * @param version
	 *            版本号
	 * @return 指定版本或最新版本的AT工作流蓝图
	 * @throws AtfException
	 */
	AtWorkflowMetadata find(String name, Long version) throws AtfException;

	/**
	 * 
	 * @param name
	 *            工作流名(nickname)
	 * @return 最新版本的AT工作流蓝图
	 * @throws AtfException
	 */
	AtWorkflowMetadata findLatest(String name) throws AtfException;

	/**
	 * 
	 * @return AT工作流蓝图生成器
	 */
	AtfMetadataBuilder getMetadataBuilder();
}
