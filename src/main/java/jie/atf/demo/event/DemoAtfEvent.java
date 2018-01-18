package jie.atf.demo.event;

import org.springframework.beans.factory.annotation.Autowired;

import jie.atf.core.api.IAtfWorkflowSvs;
import jie.atf.core.domain.AtWorkflow;
import jie.atf.core.dto.AtfEventPayload;
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
			AtfEventPayload payload = new AtfEventPayload();
			payload.setWorkflowName(workflow.getName());
			System.out.println("MQ发送消息：消息载体" + payload);
			System.out.println("......\nMQ接收消息：执行工作流");
			workflowSvs.execute(workflowSvs.find(payload.getWorkflowName()), null);
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
