package jie.atf.core.dto;

/**
 * AT任务的重试逻辑
 * 
 * @author Jie
 *
 */
public enum AtTaskRetryLogic {
	FIXED, // retryDelaySeconds后重试
	EXPONENTIAL_BACKOFF, // (retryDelaySeconds * 剩余重试次数)后重试
}
