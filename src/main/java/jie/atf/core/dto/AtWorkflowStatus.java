package jie.atf.core.dto;

/**
 * AT工作流的生命周期
 * 
 * @author Jie
 *
 */
public enum AtWorkflowStatus {
	IN_PROGRESS(false, false), //
	SUSPENDED(false, true), // 挂起
	TERMINATED(true, false), // 终止
	COMPLETED(true, true); // 成功

	private boolean terminal;
	private boolean successful;

	AtWorkflowStatus(boolean terminal, boolean successful) {
		this.terminal = terminal;
		this.successful = successful;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public boolean isSuccessful() {
		return successful;
	}
}
