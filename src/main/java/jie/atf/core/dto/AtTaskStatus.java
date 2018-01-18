package jie.atf.core.dto;

/**
 * AT任务的生命周期
 * 
 * @author Jie
 *
 */
public enum AtTaskStatus {
	IN_PROGRESS(false, true, true), //
	CONTINUED(false, true, true), // 保存点
	SUSPENDED(false, true, false), // 挂起
	FAILED(true, false, true), // 失败
	COMPLETED(true, true, true); // 成功

	private boolean terminal;
	private boolean successful;
	private boolean retry;

	AtTaskStatus(boolean terminal, boolean successful, boolean retry) {
		this.terminal = terminal;
		this.successful = successful;
		this.retry = retry;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public boolean canRetry() {
		return retry;
	}
}
