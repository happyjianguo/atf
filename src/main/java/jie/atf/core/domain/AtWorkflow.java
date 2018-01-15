package jie.atf.core.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jie.atf.core.dto.AtWorkflowStatus;
import jie.atf.core.utils.exception.AtfException;

/**
 * AT工作流
 * 
 * @author Jie
 *
 */
public class AtWorkflow {
	private Long id;
	private Long currentTaskId; // 当前执行的任务ID
	// === AtWorkflowMetadata ===
	private String name; // 工作流名(nickname)
	private Long version; // 版本号
	private List<AtTask> tasks = new ArrayList<AtTask>(); // 任务列表

	private String scenario; // 交易场景
	private Long clientId; // 客户ID
	private AtWorkflowStatus status; // 生命周期
	private Long statusDate;
	private Map<String, Object> inputData = new HashMap<String, Object>(); // 输入数据
	private Map<String, Object> outputData = new HashMap<String, Object>(); // 输出数据
	// === 时间戳 ===
	private Long startDate; // 开始执行的时间
	private Long endDate; // 执行完成的时间

	/**
	 * 
	 * @param taskId
	 * @return taskId对应的AT任务
	 */
	public AtTask getTask(Long taskId) {
		for (AtTask ret : tasks)
			if (taskId.equals(ret.getId()))
				return ret;
		return null;
	}

	/**
	 * 
	 * @param taskName
	 * @return taskName对应的AT任务
	 */
	public AtTask getTask(String taskName) {
		for (AtTask ret : tasks)
			if (StringUtils.equalsIgnoreCase(taskName, ret.getName()))
				return ret;
		return null;
	}

	public void statusInProgress() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtWorkflowStatus.IN_PROGRESS;
		if (startDate == null)
			startDate = now;
	}

	public void statusSuspended() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtWorkflowStatus.SUSPENDED;
	}

	public void statusTerminated() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtWorkflowStatus.TERMINATED;
		endDate = now;
	}

	public void statusCompleted() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtWorkflowStatus.COMPLETED;
		endDate = now;
	}

	public void addInputData(Map<String, Object> variables) {
		if (MapUtils.isNotEmpty(variables))
			inputData.putAll(variables);
	}

	/**
	 * 替换路径占位符
	 * 
	 * @param task
	 *            AT任务
	 * @throws AtfException
	 */
	public void replacePlaceholder(AtTask task) throws AtfException {
		Map<String, String> inputParameters = task.getInputParameters();
		if (MapUtils.isNotEmpty(inputParameters))
			for (Map.Entry<String, String> entry : inputParameters.entrySet())
				task.param(entry.getKey(), doReplacePlaceholder(entry.getValue()));
	}

	/**
	 * 
	 * @param path
	 *            路径占位符
	 * @return 路径占位符对应的值
	 * @throws AtfException
	 */
	private Object doReplacePlaceholder(String path) throws AtfException {
		String[] values = path.split("(?=\\$\\{)|(?<=\\})");
		Object[] convertedValues = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			convertedValues[i] = values[i];
			if (values[i].startsWith("${") && values[i].endsWith("}")) {
				// workflow/任务名.input/output.键
				String paramPath = values[i].substring(2, values[i].length() - 1);
				String[] paramPathComponents = paramPath.split("\\.");
				if (paramPathComponents.length != 3)
					throw new AtfException("Invalid inputParameter: " + paramPath);
				String source = paramPathComponents[0]; // workflow/任务名
				String type = paramPathComponents[1]; // input/output
				String key = paramPathComponents[2]; // 键
				if (StringUtils.equalsIgnoreCase("workflow", source)) {
					if (StringUtils.equalsIgnoreCase("input", type))
						convertedValues[i] = inputData.get(key);
					else if (StringUtils.equalsIgnoreCase("output", type))
						convertedValues[i] = outputData.get(key);
					else
						convertedValues[i] = null;
				} else {
					AtTask task = getTask(source);
					if (task != null) {
						if (StringUtils.equalsIgnoreCase("input", type))
							convertedValues[i] = task.getInputData().get(key);
						else if (StringUtils.equalsIgnoreCase("output", type))
							convertedValues[i] = task.getOutputData().get(key);
					} else
						convertedValues[i] = null;
				}
			} else if (values[i].startsWith("#{") && values[i].endsWith("}")) {
				// TODO 支持系统环境变量
			}
		}

		Object retObj = convertedValues[0];
		if (convertedValues.length > 1) {
			for (int i = 0; i < convertedValues.length; i++) {
				Object val = convertedValues[i];
				if (val == null) {
					val = "";
				}
				if (i == 0) {
					retObj = val;
				} else {
					retObj = retObj + "" + val.toString();
				}
			}
		}
		return retObj;
	}

	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCurrentTaskId() {
		return currentTaskId;
	}

	public void setCurrentTaskId(Long currentTaskId) {
		this.currentTaskId = currentTaskId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public List<AtTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<AtTask> tasks) {
		this.tasks = tasks;
	}

	public String getScenario() {
		return scenario;
	}

	public void setScenario(String scenario) {
		this.scenario = scenario;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public AtWorkflowStatus getStatus() {
		return status;
	}

	@Deprecated
	public void setStatus(AtWorkflowStatus status) {
		this.status = status;
	}

	public Long getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Long statusDate) {
		this.statusDate = statusDate;
	}

	public Map<String, Object> getInputData() {
		return inputData;
	}

	public void setInputData(Map<String, Object> inputData) {
		this.inputData = inputData;
	}

	public Map<String, Object> getOutputData() {
		return outputData;
	}

	public void setOutputData(Map<String, Object> outputData) {
		this.outputData = outputData;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}
}
