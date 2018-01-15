package jie.atf.core.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import jie.atf.core.dto.AtTaskMetadata;
import jie.atf.core.dto.AtTaskMode;
import jie.atf.core.dto.AtTaskRetryLogic;
import jie.atf.core.dto.AtTaskTimeoutPolicy;
import jie.atf.core.dto.AtWorkflowMetadata;
import jie.atf.core.utils.AtfUtils;
import jie.atf.core.utils.exception.AtfException;

/**
 * AT工作流蓝图生成器
 * 
 * @author Jie
 *
 */
public class AtfMetadataBuilder {
	// === AtWorkflowMetadata ===
	private String name;
	private Long version;
	private List<AtTaskMetadata> taskMetadatas = new ArrayList<AtTaskMetadata>();

	private Long currentStep = 1L; // 添加一个AT任务蓝图则step+1
	private AtTaskMetadata currentTaskMetadata; // 当前操作的AT任务蓝图
	// === Container ===
	private AtTaskMetadata parentTaskMetadata; // 父级AT任务蓝图

	/**
	 * 设置AT工作流的名称
	 * 
	 * @param name
	 *            工作流名(nickname)
	 * @return
	 */
	public AtfMetadataBuilder workflowName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * 设置AT工作流的版本号
	 * 
	 * @param version
	 *            版本号
	 * @return
	 */
	public AtfMetadataBuilder workflowVersion(Long version) {
		this.version = version;
		return this;
	}

	/**
	 * 添加AT任务蓝图
	 * 
	 * @param type
	 *            全限定类名。反射
	 * @return
	 */
	public AtfMetadataBuilder withTask(String type) {
		AtTaskMetadata taskMetadata = createTaskMeta(type);
		taskMetadatas.add(taskMetadata);

		currentTaskMetadata = taskMetadata;
		return this;
	}

	/**
	 * 设置AT任务的名称
	 * 
	 * @param name
	 * @return
	 * @throws AtfException
	 */
	public AtfMetadataBuilder taskName(String name) throws AtfException {
		AtfUtils.checkNotNull(currentTaskMetadata, "currentTaskMetadata can NOT be null");
		currentTaskMetadata.setName(name);
		return this;
	}

	/**
	 * 设置AT任务的执行模式
	 * 
	 * @param mode
	 *            执行模式
	 * @return
	 * @throws AtfException
	 */
	public AtfMetadataBuilder taskMode(AtTaskMode mode) throws AtfException {
		AtfUtils.checkNotNull(currentTaskMetadata, "currentTaskMetadata can NOT be null");
		currentTaskMetadata.setMode(mode);
		return this;
	}

	/**
	 * 添加AT任务的输入参数路径定义
	 * 
	 * @param key
	 * @param path
	 *            参数路径占位符：${workflow/任务名.input/output.键}
	 * @return
	 * @throws AtfException
	 */
	public AtfMetadataBuilder taskParam(String key, String path) throws AtfException {
		AtfUtils.checkNotNull(currentTaskMetadata, "currentTaskMetadata can NOT be null");
		currentTaskMetadata.param(key, path);
		return this;
	}

	/**
	 * 设置AT任务的重试逻辑
	 * 
	 * @param retryCount
	 *            当任务被标记为FAILED或RETRY时，尝试重试的次数
	 * @param retryDelaySeconds
	 *            重试的延时阈值（毫秒）
	 * @param retryLogic
	 *            重试逻辑
	 * @return
	 * @throws AtfException
	 */
	public AtfMetadataBuilder taskRetryLogic(Long retryCount, Long retryDelaySeconds, AtTaskRetryLogic retryLogic)
			throws AtfException {
		AtfUtils.checkNotNull(currentTaskMetadata, "currentTaskMetadata can NOT be null");
		currentTaskMetadata.setRetryCount(retryCount);
		currentTaskMetadata.setRetryDelaySeconds(retryDelaySeconds);
		currentTaskMetadata.setRetryLogic(retryLogic);
		return this;
	}

	/**
	 * 设置AT任务的超时策略
	 * 
	 * @param timeoutSeconds
	 *            超时阈值（毫秒）
	 * @param timeoutPolicy
	 *            超时策略
	 * @return
	 * @throws AtfException
	 */
	public AtfMetadataBuilder taskTimeoutPolicy(Long timeoutSeconds, AtTaskTimeoutPolicy timeoutPolicy)
			throws AtfException {
		AtfUtils.checkNotNull(currentTaskMetadata, "currentTaskMetadata can NOT be null");
		currentTaskMetadata.setTimeoutSeconds(timeoutSeconds);
		currentTaskMetadata.setTimeoutPolicy(timeoutPolicy);
		return this;
	}

	/**
	 * 添加AT容器蓝图
	 * 
	 * @param type
	 *            全限定类名。反射
	 * @return
	 */
	public AtfMetadataBuilder withContainer(String type) {
		AtTaskMetadata containerMetadata = createTaskMeta(type);
		taskMetadatas.add(containerMetadata);

		currentTaskMetadata = containerMetadata;
		parentTaskMetadata = containerMetadata;
		return this;
	}

	/**
	 * 向容器中添加AT任务蓝图
	 * 
	 * @param type
	 *            全限定类名。反射
	 * @return
	 * @throws AtfException
	 */
	public AtfMetadataBuilder containTask(String type) throws AtfException {
		AtfUtils.checkNotNull(parentTaskMetadata, "parentTaskMetadata can NOT be null");
		AtTaskMetadata taskMetadata = createTaskMeta(type);
		taskMetadata.setParentName(parentTaskMetadata.getName());
		taskMetadatas.add(taskMetadata);

		currentTaskMetadata = taskMetadata;
		return this;
	}

	/**
	 * 停止操作AT容器蓝图
	 * 
	 * @return
	 * @throws AtfException
	 */
	public AtfMetadataBuilder endContainer() throws AtfException {
		AtfUtils.checkNotNull(parentTaskMetadata, "parentTaskMetadata can NOT be null");
		currentTaskMetadata = null;
		parentTaskMetadata = null;
		return this;
	}

	/**
	 * 生成AT工作流蓝图
	 * 
	 * @return AtWorkflowMetadata
	 * @throws AtfException
	 */
	public AtWorkflowMetadata build() throws AtfException {
		AtfUtils.checkNotNull(taskMetadatas, "taskMetadatas can NOT be empty");
		AtWorkflowMetadata ret = new AtWorkflowMetadata();
		ret.setName(name);
		ret.setVersion(version);
		ret.setTaskMetadatas(taskMetadatas);
		return ret;
	}

	/**
	 * 任务名默认取类名，执行模式默认同步
	 * 
	 * @param type
	 *            全限定类名。反射
	 * @return AT任务蓝图
	 */
	private AtTaskMetadata createTaskMeta(String type) {
		AtTaskMetadata taskMetadata = new AtTaskMetadata();
		taskMetadata.setName(StringUtils.substring(type, StringUtils.lastIndexOf(type, ".") + 1, type.length()));
		taskMetadata.setType(type);
		taskMetadata.setMode(AtTaskMode.SYNC);
		taskMetadata.setStep(currentStep++);
		return taskMetadata;
	}
}
