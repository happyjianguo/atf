package jie.atf.core.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jie.atf.core.api.IAtfWorkflowSvs;
import jie.atf.core.dao.IAtWorkflowDAO;
import jie.atf.core.domain.AtTask;
import jie.atf.core.domain.AtWorkflow;
import jie.atf.core.dto.AtTaskMode;
import jie.atf.core.dto.AtWorkflowMetadata;
import jie.atf.core.service.event.IAtfEvent;
import jie.atf.core.utils.AtfUtils;
import jie.atf.core.utils.exception.AtfException;

@Service
public class AtfWorkflowSvs implements IAtfWorkflowSvs {
	@Autowired
	private IAtWorkflowDAO workflowDAO;

	@Autowired
	private AtWorkflowBuilder workflowBuilder;

	@Autowired
	private IAtfEvent event;

	@Override
	public void save(AtWorkflow workflow) {
		if (workflow.getId() == null)
			workflowDAO.create(workflow);
		else
			workflowDAO.update(workflow);
	}

	@Override
	public AtWorkflow find(String name) {
		return workflowDAO.find(name);
	}

	@Override
	public AtWorkflowBuilder getWorkflowBuilder() {
		return workflowBuilder;
	}

	@Override
	public void execute(AtWorkflowMetadata workflowMetadata, Map<String, Object> inputData) throws AtfException {
		AtWorkflow workflow = workflowBuilder.buildWorkflow(workflowMetadata, inputData);
		execute(workflow, null);
	}

	@Override
	public void execute(AtWorkflow workflow, Map<String, Object> variables) throws AtfException {
		workflow.addInputData(variables); // 将流程变量导入inputData
		doExecute(workflow);
	}

	/**
	 * 执行AT工作流
	 * 
	 * @param workflow
	 *            AT工作流
	 * @throws AtfException
	 */
	private void doExecute(AtWorkflow workflow) throws AtfException {
		AtfUtils.checkNotNull(workflow, "workflow can NOT be null");
		// mark workflow IN_PROGRESS
		workflow.statusInProgress();

		Long currentTaskId = workflow.getCurrentTaskId();
		if (currentTaskId == null) {
			// 若当前执行的任务ID为null，则workflow COMPLETED
			workflow.statusCompleted();
			save(workflow);
			return;
		}

		AtTask currentTask = workflow.getTask(currentTaskId);
		currentTask.statusInProgress();
		workflow.replacePlaceholder(currentTask);
		switch (currentTask.getMode()) {
		case SYNC:
			// 同步
			// TODO Timeout Policy
			currentTask.execute();
			break;
		case ASYNC:
			// 先保存再异步化
			currentTask.setMode(AtTaskMode.SYNC);
			save(workflow);
			event.asyncExecute(workflow);
			return;
		}

		switch (currentTask.getStatus()) {
		case CONTINUED: // 保存点
			// 先保存再继续执行
			save(workflow);
			doExecute(workflow);
			break;
		case SUSPENDED: // 挂起
			workflow.statusSuspended();
			save(workflow);
			break;
		case FAILED: // 失败
			// 先触发重试逻辑，否则终止工作流
			Long retryDelaySeconds = check4Retry(currentTask);
			if (retryDelaySeconds != null && retryDelaySeconds > 0) {
				save(workflow);
				event.delayExecute(workflow, retryDelaySeconds);
			} else {
				workflow.statusTerminated();
				save(workflow);
			}
			break;
		case COMPLETED: // 成功
			// move currentTaskId to the next one
			workflow.setCurrentTaskId(currentTask.getNextId());
			save(workflow);
			// 递归执行
			doExecute(workflow);
			break;
		default:
			break;
		}
	}

	/**
	 * check for retry
	 * 
	 * @param task
	 *            AT任务
	 * @return
	 */
	private Long check4Retry(AtTask task) {
		Long ret = null;
		Long retriedCount = task.getRetriedCount() == null ? 0 : task.getRetriedCount();
		if (retriedCount < task.getRetryCount()) {
			task.setRetriedCount(retriedCount + 1);
			Long retryDelaySeconds = task.getRetryDelaySeconds();
			if (retryDelaySeconds != null && retryDelaySeconds > 0) {
				// Retry Logic
				// retry... - but not immediately - put a delay...
				switch (task.getRetryLogic()) {
				case FIXED:
					break;
				case EXPONENTIAL_BACKOFF:
					retryDelaySeconds = retryDelaySeconds * (1 + task.getRetriedCount());
					break;
				}
			}
			ret = retryDelaySeconds;
		}
		return ret;
	}
}
