package jie.atf.core.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * AT任务蓝图
 * 
 * @author Jie
 *
 */
public class AtTaskMetadata {
	private String name; // 任务名(nickname)。UNIQUE
	private String type; // 全限定类名。反射
	private AtTaskMode mode = AtTaskMode.SYNC; // 执行模式
	private Map<String, String> inputParameters = new HashMap<String, String>(); // 任务的输入参数路径定义
	// === Retry Logic ===
	private Long retryCount = 3L; // 当任务被标记为FAILED时，尝试重试的次数
	private Long retryDelaySeconds = 1000L; // 重试的延时阈值（毫秒）
	private AtTaskRetryLogic retryLogic = AtTaskRetryLogic.FIXED; // 重试逻辑
	// === Timeout Policy ===
	private Long timeoutSeconds; // 超时阈值（毫秒）
	private AtTaskTimeoutPolicy timeoutPolicy; // 超时策略

	private String parentName; // 父级任务蓝图名

	/**
	 * 添加输入参数路径定义
	 * 
	 * @param key
	 * @param path
	 *            参数路径占位符：${workflow/任务名.input/output.键}
	 */
	public void param(String key, String path) {
		inputParameters.put(key, path);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AtTaskMode getMode() {
		return mode;
	}

	public void setMode(AtTaskMode mode) {
		this.mode = mode;
	}

	public Map<String, String> getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(Map<String, String> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public Long getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Long retryCount) {
		this.retryCount = retryCount;
	}

	public Long getRetryDelaySeconds() {
		return retryDelaySeconds;
	}

	public void setRetryDelaySeconds(Long retryDelaySeconds) {
		this.retryDelaySeconds = retryDelaySeconds;
	}

	public AtTaskRetryLogic getRetryLogic() {
		return retryLogic;
	}

	public void setRetryLogic(AtTaskRetryLogic retryLogic) {
		this.retryLogic = retryLogic;
	}

	public Long getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(Long timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	public AtTaskTimeoutPolicy getTimeoutPolicy() {
		return timeoutPolicy;
	}

	public void setTimeoutPolicy(AtTaskTimeoutPolicy timeoutPolicy) {
		this.timeoutPolicy = timeoutPolicy;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
}
