package jie.atf.demo.domain;

import jie.atf.core.domain.AtTask;

/**
 * 管理类任务 - AT容器类demo
 * 
 * @author Jie
 *
 */
public class DemoAtContainer extends AtTask {
	@Override
	protected void doExecute() {
		System.out.println("now execute AtContainer ...");
		statusCompleted();
	}
}
