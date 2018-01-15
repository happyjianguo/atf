package jie.atf.core.dto;

/**
 * AT任务的超时策略
 * 
 * @author Jie
 *
 */
public enum AtTaskTimeoutPolicy {
	RETRY, // 重试
	TIME_OUT_WF, // 任务被标记为TIMEOUT状态并终止
	ALERT_ONLY, //
}
