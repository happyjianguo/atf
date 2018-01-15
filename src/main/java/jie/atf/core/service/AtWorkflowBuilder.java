package jie.atf.core.service;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jie.atf.core.dao.IAtTaskDAO;
import jie.atf.core.dao.IAtWorkflowDAO;
import jie.atf.core.domain.AtTask;
import jie.atf.core.domain.AtWorkflow;
import jie.atf.core.dto.AtTaskMetadata;
import jie.atf.core.dto.AtWorkflowMetadata;
import jie.atf.core.utils.exception.AtfException;

/**
 * AT工作流生成器
 * 
 * @author Jie
 *
 */
@Component
public class AtWorkflowBuilder {
	@Autowired
	private IAtTaskDAO taskDAO;

	@Autowired
	private IAtWorkflowDAO workflowDAO;

	/**
	 * 通过反射创建AT任务实例
	 * 
	 * @param type
	 *            全限定类名。反射
	 * @return AT任务
	 * @throws AtfException
	 */
	public AtTask buildTask(String type) throws AtfException {
		try {
			Class<?> clazz = Class.forName(type);
			Constructor<?> constructor = clazz.getConstructor();
			AtTask ret = (AtTask) constructor.newInstance();
			ret.setType(type);
			taskDAO.create(ret);
			return ret;
		} catch (Exception e) {
			throw new AtfException(e);
		}
	}

	/**
	 * 生成AT工作流
	 * 
	 * @param workflowMetadata
	 *            AT工作流蓝图
	 * @param inputData
	 *            输入数据
	 * @return AT工作流
	 * @throws AtfException
	 */
	public AtWorkflow buildWorkflow(AtWorkflowMetadata workflowMetadata, Map<String, Object> inputData)
			throws AtfException {
		AtWorkflow ret = new AtWorkflow();
		// AT工作流的别名 = 蓝图名:v版本号
		ret.setName(workflowMetadata.getName() + ":v" + workflowMetadata.getVersion());
		ret.setVersion(workflowMetadata.getVersion());
		ret.setScenario(workflowMetadata.getName());
		ret.setClientId((Long) inputData.get("clientId"));

		List<AtTask> tasks = new ArrayList<AtTask>();
		for (AtTaskMetadata taskMetadata : workflowMetadata.getTaskMetadatas()) {
			AtTask task = buildTask(taskMetadata.getType());
			task.setName(taskMetadata.getName());
			task.setMode(taskMetadata.getMode());
			task.setStep(taskMetadata.getStep());
			task.setInputParameters(taskMetadata.getInputParameters());
			task.setRetryCount(taskMetadata.getRetryCount());
			task.setRetryDelaySeconds(taskMetadata.getRetryDelaySeconds());
			task.setRetryLogic(taskMetadata.getRetryLogic());
			task.setTimeoutSeconds(taskMetadata.getTimeoutSeconds());
			task.setTimeoutPolicy(taskMetadata.getTimeoutPolicy());

			// === Container ===
			String parentName = taskMetadata.getParentName();
			if (StringUtils.isNotEmpty(parentName)) {
				AtTask parent = findByName(parentName, tasks);
				task.setParentId(parent.getId());
			}

			tasks.add(task);
		}
		ret.setTasks(tasks);
		ret.setCurrentTaskId(tasks.get(0).getId()); // 初始化指向第一个任务
		ret.setInputData(inputData);

		workflowDAO.create(ret);
		for (AtTask task : ret.getTasks()) {
			AtTask nextTask = findNext(task, ret.getTasks());
			if (nextTask != null)
				task.setNextId(nextTask.getId());

			task.setWorkflowId(ret.getId());
			task.setScenario(ret.getScenario());
			task.setClientId(ret.getClientId());
		}
		workflowDAO.update(ret);
		return ret;
	}

	/**
	 * 根据任务名查询AT任务
	 * 
	 * @param name
	 *            任务名
	 * @param tasks
	 * @return
	 */
	private AtTask findByName(String name, List<AtTask> tasks) {
		for (AtTask ret : tasks)
			if (StringUtils.equals(name, ret.getName()))
				return ret;
		return null;
	}

	/**
	 * 查询指定任务的下一个AT任务
	 * 
	 * @param task
	 * @param tasks
	 * @return
	 */
	private AtTask findNext(AtTask task, List<AtTask> tasks) {
		int index = tasks.indexOf(task);
		if ((index == -1) || (index == tasks.size() - 1))
			return null;
		else
			return tasks.get(index + 1);
	}
}
