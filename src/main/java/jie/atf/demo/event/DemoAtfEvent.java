package jie.atf.demo.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jie.atf.core.api.IAtfWorkflowSvs;
import jie.atf.core.domain.AtWorkflow;
import jie.atf.core.dto.AtfEventPayload;
import jie.atf.core.service.event.IAtfEvent;
import jie.atf.core.utils.stereotype.AtfDemo;

@AtfDemo
public class DemoAtfEvent implements IAtfEvent {
	@Autowired
	private IAtfWorkflowSvs workflowSvs;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void asyncExecute(AtWorkflow workflow) {
		try {
			final AtfEventPayload payload = new AtfEventPayload();
			payload.setWorkflowName(workflow.getName());
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
					@Override
					public void afterCommit() { // 事务提交成功之后发送消息
						send(payload);
					}
				});
			} else { // 发送消息
				send(payload);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delayExecute(AtWorkflow workflow, Long delaySeconds) {
		try {
			final AtfEventPayload payload = new AtfEventPayload();
			payload.setWorkflowName(workflow.getName());
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
					@Override
					public void afterCommit() {
						sendDelay(payload);
					}
				});
			} else {
				sendDelay(payload);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 委托RabbitMQ发送消息
	 * 
	 * @param payload
	 */
	public void send(AtfEventPayload payload) {
		rabbitTemplate.convertAndSend("atf.exchange.event", "atf.routingkey.event", payload);
	}

	/**
	 * 消费消息并继续执行AT工作流
	 * 
	 * @param payload
	 */
	@RabbitListener(queues = "atf.queue.event", containerFactory = "rabbitListenerContainerFactory")
	public void consume(AtfEventPayload payload) {
		try {
			AtWorkflow workflow = workflowSvs.find(payload.getWorkflowName());
			workflowSvs.execute(workflow, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 委托RabbitMQ发送消息（30s延迟队列）
	 * 
	 * @param payload
	 */
	public void sendDelay(AtfEventPayload payload) {
		rabbitTemplate.convertAndSend("atf.exchange.delay", "atf.routingkey.delay.30s", payload);
	}

	/**
	 * 消费延迟消息并继续执行AT工作流
	 * 
	 * @param payload
	 */
	@RabbitListener(queues = "atf.queue.delay.dlq", containerFactory = "rabbitListenerContainerFactory")
	public void consumeDelay(AtfEventPayload payload) {
		try {
			AtWorkflow workflow = workflowSvs.find(payload.getWorkflowName());
			workflowSvs.execute(workflow, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
