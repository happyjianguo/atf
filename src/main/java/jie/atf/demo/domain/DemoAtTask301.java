package jie.atf.demo.domain;

import jie.atf.core.domain.AtTask;
import jie.atf.core.dto.AtTaskState;

/**
 * 原子交易类任务 - Demo101
 * 
 * @author Jie
 *
 */
public class DemoAtTask301 extends AtTask {
	@Override
	protected void doExecute() {
		switch (state) {
		case STATE1:
			System.out.println("[DemoAtTask301]保存点");
			statusContinued();
			transition(AtTaskState.STATE2);
			break;
		case STATE2:
			System.out.println("[DemoAtTask301]失败");
			statusFailed();
			break;
		default:
			break;
		}
	}
}
