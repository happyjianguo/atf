package jie.atf.core.api;

import java.util.Map;

import jie.atf.core.domain.AtWorkflow;
import jie.atf.core.dto.AtWorkflowMetadata;
import jie.atf.core.service.AtWorkflowBuilder;
import jie.atf.core.utils.exception.AtfException;

/**
 * Workflow service for the ATF
 * 
 * @author Jie
 *
 */
public interface IAtfWorkflowSvs {
	/**
	 * 持久化AT工作流：CREATE or UPDATE
	 * 
	 * @param workflow
	 */
	void save(AtWorkflow workflow);

	/**
	 * 
	 * @param name
	 *            AT工作流名
	 * @return AT工作流
	 */
	AtWorkflow find(String name);

	/**
	 * 
	 * @return AT工作流生成器
	 */
	AtWorkflowBuilder getWorkflowBuilder();

	/**
	 * 根据蓝图创建并执行AT工作流
	 * 
	 * @param workflowMetadata
	 *            AT工作流蓝图
	 * @param inputData
	 *            输入数据
	 * @throws AtfException
	 */
	void execute(AtWorkflowMetadata workflowMetadata, Map<String, Object> inputData) throws AtfException;

	/**
	 * 执行AT工作流
	 * 
	 * @param workflow
	 *            AT工作流
	 * @param variables
	 *            流程变量
	 * @throws AtfException
	 */
	void execute(AtWorkflow workflow, Map<String, Object> variables) throws AtfException;
}
