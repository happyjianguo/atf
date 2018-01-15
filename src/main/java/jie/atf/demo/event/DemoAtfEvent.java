package jie.atf.demo.event;

import org.springframework.beans.factory.annotation.Autowired;

import jie.atf.core.api.IAtfWorkflowSvs;
import jie.atf.core.domain.AtWorkflow;
import jie.atf.core.service.event.IAtfEvent;
import jie.atf.core.utils.exception.AtfException;
import jie.atf.core.utils.stereotype.AtfDemo;

@AtfDemo
public class DemoAtfEvent implements IAtfEvent {
	@Autowired
	private IAtfWorkflowSvs workflowSvs;

	@Override
	public void asyncExecute(AtWorkflow workflow) {
		try {
			// TODO
			workflowSvs.execute(workflow, null);
		} catch (AtfException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delayExecute(AtWorkflow workflow, Long delaySeconds) {
		try {
			// TODO
			Thread.sleep(delaySeconds);
			workflowSvs.execute(workflow, null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (AtfException e) {
			e.printStackTrace();
		}
	}
}
