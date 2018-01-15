package jie.atf.core.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jie.atf.core.dto.AtTaskMode;
import jie.atf.core.dto.AtTaskRetryLogic;
import jie.atf.core.dto.AtTaskState;
import jie.atf.core.dto.AtTaskStatus;
import jie.atf.core.dto.AtTaskTimeoutPolicy;
import jie.atf.core.utils.exception.AtfException;

/**
 * AT抽象任务类
 * 
 * @author Jie
 *
 */
public abstract class AtTask {
	private Long id;
	private Long parentId; // 父级任务ID
	private Long nextId; // 下一个任务ID

	// === AtTaskMetadata ===
	private String name; // 任务名(nickname)。UNIQUE
	private String type; // 全限定类名。反射
	private AtTaskMode mode; // 执行模式
	private Long step; // 交易中的第几步
	private Map<String, String> inputParameters = new HashMap<String, String>(); // 任务的输入参数路径定义
	// === Retry Logic ===
	private Long retryCount; // 当任务被标记为FAILED时，尝试重试的次数
	private Long retryDelaySeconds; // 重试的延时阈值（毫秒）
	private AtTaskRetryLogic retryLogic; // 重试逻辑
	private Long retriedCount; // 已重试的次数
	// === Timeout Policy ===
	private Long timeoutSeconds; // 超时阈值（毫秒）
	private AtTaskTimeoutPolicy timeoutPolicy; // 超时策略

	private AtTaskStatus status; // 生命周期
	private Long statusDate;
	protected AtTaskState state = AtTaskState.STATE1; // 有限状态机
	private String transType; // 交易类型
	private Map<String, Object> inputData = new HashMap<String, Object>(); // 输入数据
	private Map<String, Object> outputData = new HashMap<String, Object>(); // 输出数据
	// === AtWorkflow ===
	private Long workflowId; // 工作流ID
	private String scenario; // 交易场景
	private Long clientId; // 客户ID
	// === 时间戳 ===
	private Long startDate; // 开始执行的时间
	private Long endDate; // 执行完成的时间
	// === 交易数据 ===
	private Long agreementRequestId; // 交易ID
	private Long bankAccountId; // 银行卡ID
	private BigDecimal transAmount; // 交易金额

	public void execute() throws AtfException {
		if (status.equals(AtTaskStatus.COMPLETED))
			throw new AtfException("can NOT execute task's status: COMPLETED");
		doExecute();
	}

	/**
	 * 抽象方法。委托具体At任务实现
	 */
	protected abstract void doExecute();

	public void statusInProgress() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtTaskStatus.IN_PROGRESS;
		if (startDate == null)
			startDate = now;
	}

	public void statusContinued() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtTaskStatus.CONTINUED;
	}

	public void statusSuspended() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtTaskStatus.SUSPENDED;
	}

	public void statusFailed() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtTaskStatus.FAILED;
	}

	public void statusCompleted() {
		Long now = new Date().getTime();
		statusDate = now;
		status = AtTaskStatus.COMPLETED;
		endDate = now;
	}

	/**
	 * 有限状态机的状态迁移
	 * 
	 * @param state
	 */
	public void transition(AtTaskState state) {
		this.state = state;
	}

	public void param(String key, Object value) {
		inputData.put(key, value);
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

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Long getNextId() {
		return nextId;
	}

	public void setNextId(Long nextId) {
		this.nextId = nextId;
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

	public Long getStep() {
		return step;
	}

	public void setStep(Long step) {
		this.step = step;
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

	public Long getRetriedCount() {
		return retriedCount;
	}

	public void setRetriedCount(Long retriedCount) {
		this.retriedCount = retriedCount;
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

	public AtTaskStatus getStatus() {
		return status;
	}

	@Deprecated
	public void setStatus(AtTaskStatus status) {
		this.status = status;
	}

	public Long getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Long statusDate) {
		this.statusDate = statusDate;
	}

	public AtTaskState getState() {
		return state;
	}

	@Deprecated
	public void setState(AtTaskState state) {
		this.state = state;
	}

	public String getTransType() {
		return transType;
	}

	public void setTransType(String transType) {
		this.transType = transType;
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

	public Long getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(Long workflowId) {
		this.workflowId = workflowId;
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

	public Long getAgreementRequestId() {
		return agreementRequestId;
	}

	public void setAgreementRequestId(Long agreementRequestId) {
		this.agreementRequestId = agreementRequestId;
	}

	public Long getBankAccountId() {
		return bankAccountId;
	}

	public void setBankAccountId(Long bankAccountId) {
		this.bankAccountId = bankAccountId;
	}

	public BigDecimal getTransAmount() {
		return transAmount;
	}

	public void setTransAmount(BigDecimal transAmount) {
		this.transAmount = transAmount;
	}
}
