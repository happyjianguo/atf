package jie.atf.core.service.event;

import jie.atf.core.domain.AtWorkflow;

/**
 * Event service for the ATF
 * 
 * @author Jie
 *
 */
public interface IAtfEvent {
	/**
	 * 异步执行
	 * 
	 * @param workflow
	 *            AT工作流
	 */
	void asyncExecute(AtWorkflow workflow);

	/**
	 * 延迟执行
	 * 
	 * @param workflow
	 *            AT工作流
	 * @param delaySeconds
	 *            延时
	 */
	void delayExecute(AtWorkflow workflow, Long delaySeconds);
}
