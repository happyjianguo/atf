package jie.atf.demo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import jie.atf.core.api.IAtfMetadataSvs;
import jie.atf.core.api.IAtfWorkflowSvs;
import jie.atf.core.domain.AtWorkflow;
import jie.atf.core.dto.AtTaskMode;
import jie.atf.core.dto.AtTaskRetryLogic;
import jie.atf.core.dto.AtWorkflowMetadata;
import jie.atf.core.service.AtfMetadataBuilder;
import jie.atf.core.utils.exception.AtfException;
import jie.atf.core.utils.stereotype.AtfDemo;

/**
 * 
 * 工作流名 = 场景名:appid
 * 
 * 任务名 = 类名:appid:时间戳
 * 
 * @author Jie
 *
 */
@AtfDemo
public class Demo {
	@Autowired
	IAtfMetadataSvs metadataSvs;

	@Autowired
	IAtfWorkflowSvs workflowSvs;

	private static final String appid = "appid";

	public AtWorkflowMetadata buildScenario1Metadata() throws AtfException {
		long time = new Date().getTime();
		AtfMetadataBuilder metadataBuilder = metadataSvs.getMetadataBuilder();
		AtWorkflowMetadata ret = metadataBuilder.workflowName("Scenario1:" + appid) //
				.withTask("jie.atf.demo.domain.DemoAtTask101") //
				.taskName("DemoAtTask101:" + appid + ":" + time) //
				.taskParam("agreementRequestId", "${workflow.input.agreementRequestId}") // 交易ID
				.taskParam("bankAccountId", "${workflow.input.bankAccountId}") // 银行卡ID
				.taskParam("transAmount", "${workflow.input.transAmount}") // 交易金额
				.withTask("jie.atf.demo.domain.DemoAtTask301") //
				.taskName("DemoAtTask301:" + appid + ":" + time) //
				.taskParam("agreementRequestId", "${workflow.input.agreementRequestId}") // 交易ID
				.taskParam("bankAccountId", "${workflow.input.bankAccountId}") // 银行卡ID
				.taskParam("transAmount", "${workflow.input.transAmount}") // 交易金额
				.taskRetryLogic(3L, 1000L, AtTaskRetryLogic.EXPONENTIAL_BACKOFF) //
				.build();

		metadataSvs.register(ret);
		return ret;
	}

	public AtWorkflowMetadata buildScenario1ContainerMetadata(Integer n) throws AtfException {
		long time = new Date().getTime();
		AtfMetadataBuilder metadataBuilder = metadataSvs.getMetadataBuilder();
		metadataBuilder.workflowName("Scenario1Container:" + appid) //
				.withContainer("jie.atf.demo.domain.DemoAtContainer") // AT容器
				.taskName("DemoAtContainer:" + appid + ":" + time); // AT容器名
		for (int i = 0; i != n; ++i) {
			metadataBuilder.containTask("jie.atf.demo.domain.DemoAtTask101") //
					.taskName("DemoAtTask101:" + appid + ":" + time + ":" + i) //
					.taskMode(AtTaskMode.ASYNC) //
					.taskParam("agreementRequestId", "${workflow.input.agreementRequestId}") // 交易ID
					.taskParam("bankAccountId", "${workflow.input.bankAccountId}") // 银行卡ID
					.taskParam("childTransAmount" + i, "${workflow.input.childTransAmount" + i + "}") // 第i笔交易金额
					.taskParam("totalSize", "${workflow.input.totalSize}") // 总的交易金额笔数
					.taskRetryLogic(3L, 1000L, AtTaskRetryLogic.EXPONENTIAL_BACKOFF);
		}
		metadataBuilder.endContainer();
		metadataBuilder.withTask("jie.atf.demo.domain.DemoAtTask301") //
				.taskName("DemoAtTask301:" + appid + ":" + time) //
				.taskParam("agreementRequestId", "${workflow.input.agreementRequestId}") // 交易ID
				.taskParam("bankAccountId", "${workflow.input.bankAccountId}") // 银行卡ID
				.taskParam("transAmount", "${workflow.input.transAmount}") // 交易金额
				.taskRetryLogic(3L, 3 * 1000L, AtTaskRetryLogic.FIXED);
		AtWorkflowMetadata ret = metadataBuilder.build();

		metadataSvs.register(ret);
		return ret;
	}

	/** 根据蓝图创建并执行AT工作流 */
	public AtWorkflow execute(AtWorkflowMetadata workflowMetadata, Map<String, Object> inputData) throws AtfException {
		workflowSvs.execute(workflowMetadata, inputData);
		return workflowSvs.find(workflowMetadata.getName() + ":v" + workflowMetadata.getVersion());
	}

	/** 创建inputData */
	public Map<String, Object> createInputData(Integer n) {
		Map<String, Object> ret = new HashMap<String, Object>();
		Long clientId = 10086L;
		Long agreementRequestId = 1L;
		Long bankAccountId = 1L;
		BigDecimal transAmount = BigDecimal.ZERO;
		ret.put("clientId", clientId);
		ret.put("agreementRequestId", agreementRequestId);
		ret.put("bankAccountId", bankAccountId);
		if (n > 1) {
			Long totalSize = Long.valueOf(n);
			for (int i = 0; i != n; ++i) {
				BigDecimal childTransAmount = BigDecimal.valueOf(100.0 + i * 10.0); // 第i笔交易金额
				ret.put("childTransAmount" + i, childTransAmount);
				transAmount = transAmount.add(childTransAmount);
			}
			ret.put("totalSize", totalSize);
		} else
			transAmount = BigDecimal.valueOf(100.0);
		ret.put("transAmount", transAmount);
		return ret;
	}
}
