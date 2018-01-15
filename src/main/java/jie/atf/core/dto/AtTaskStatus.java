package jie.atf.core.dto;

/**
 * AT任务的生命周期
 * 
 * @author Jie
 *
 */
public enum AtTaskStatus {
	IN_PROGRESS, //
	CONTINUED, // 保存点
	SUSPENDED, // 挂起
	FAILED, // 失败
	COMPLETED, // 成功
}
