package jie.atf.core.dto;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AT工作流蓝图
 * 
 * @author Jie
 *
 */
public class AtWorkflowMetadata extends AtAbstractMetadata {
	private String name; // 工作流名(nickname)
	private Long version; // 版本号
	private List<AtTaskMetadata> taskMetadatas; // 任务蓝图的列表

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

	public List<AtTaskMetadata> getTaskMetadatas() {
		return taskMetadatas;
	}

	public void setTaskMetadatas(List<AtTaskMetadata> taskMetadatas) {
		this.taskMetadatas = taskMetadatas;
	}
}
