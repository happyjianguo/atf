# ATF - Atomic Transaction Framework, 原子交易框架
  (Jie © 2017)<br>

AT（原子交易，Atomic Transaction）是指一笔交易中的原子步骤，不可细分的最小单位。AT的不同组合，构成了具体的交易场景。AT的核心业务逻辑是会计记账。
## 1 分层架构设计
ATF（原子交易框架，Atomic Transaction Framework）是流程引擎+业务场景化编制+金融资产会计记账。<br />
ATF支持：
1. 基于业务流(business flow)场景化编制AT任务(**AtTask**)
2. 基于JSON DSL (domain-specific language)的蓝图，用来定义AT工作流(**AtWorkflow**) 
3. 同步、异步化执行能力
4. 重试(RETRY)、超时(TIMEOUT)、挂起(SUSPEND) AT任务
5. AT任务可重用
6. MQ服务(**AtfEvent**)解耦领域服务，便于通过扩容来提升执行能力


![ATF分层架构](/dev-book/uml/Architecture.png)<br />
ATF默认使用RabbitMQ消息队列和MySQL持久化方案，也可以通过实现IAtfEvent和DAO接口来切换成其它方案。
### 1.1 时序图
![时序图](/dev-book/uml/SequenceDiagram.png)<br />
### 1.2 Quick start
```
@Autowired
IAtfMetadataSvs metadataSvs;
@Autowired
IAtfWorkflowSvs workflowSvs;

// 生成工作流蓝图
AtfMetadataBuilder metadataBuilder = metadataSvs.getMetadataBuilder();
AtWorkflowMetadata workflowMetadata = metadataBuilder.workflowName("Scenario1:" + appid)
	.withTask("jie.atf.demo.domain.DemoAtTask101")
	.taskParam("agreementRequestId", "${workflow.input.agreementRequestId}") // 交易ID
	.build();
// 注册蓝图
metadataSvs.register(workflowMetadata);
// 执行工作流
workflowSvs.execute(workflowMetadata, inputData);

```
Spring Boot App - Run<br />
http://localhost:8080/ 执行Groovy脚本
## 2 AtfMetadataSvs服务: 生成、注册蓝图
AtfMetadataSvs服务负责AT工作流蓝图（基于JSON DSL）的注册、更新、读取查询等操作，并提供了蓝图生成器**AtfMetadataBuilder**快速生成蓝图。ATF根据已注册的蓝图创建并执行AT工作流实例。
> 建议的命名规范：
> 1. AT工作流蓝图名 = 场景名:appid，确保name:version唯一；则AT工作流的别名 = 蓝图名:v版本号
> 2. AT任务蓝图名 = 类名:appid:时间戳，确保name唯一；则AT任务的别名 = 蓝图名

示例：

```
{
    "id": 3,
    "createDate": 1516181165746,
    "updateDate": null,
    "name": "Scenario1:appid",
    "version": 1,
    "taskMetadatas": [
        {
            "name": "DemoAtTask101:appid:1516181165746",
            "type": "jie.atf.demo.domain.DemoAtTask101",
            "mode": "SYNC",
            "step": null,
            "inputParameters": {
                "transAmount": "${workflow.input.transAmount}",
                "agreementRequestId": "${workflow.input.agreementRequestId}",
                "bankAccountId": "${workflow.input.bankAccountId}"
            },
            "retryCount": null,
            "retryDelaySeconds": null,
            "retryLogic": null,
            "timeoutSeconds": null,
            "timeoutPolicy": null,
            "parentName": null
        },
        {
            "name": "DemoAtTask301:appid:1516181165746",
            "type": "jie.atf.demo.domain.DemoAtTask301",
            "mode": "SYNC",
            "step": null,
            "inputParameters": {
                "transAmount": "${workflow.input.transAmount}",
                "agreementRequestId": "${workflow.input.agreementRequestId}",
                "bankAccountId": "${workflow.input.bankAccountId}"
            },
            "retryCount": 3,
            "retryDelaySeconds": 1000,
            "retryLogic": "EXPONENTIAL_BACKOFF",
            "timeoutSeconds": null,
            "timeoutPolicy": null,
            "parentName": null
        }
    ]
}
```
### 2.1 AtWorkflowMetadata类：AT工作流蓝图
域 | 描述 | 备注
---|---| ---
name | 工作流名(nickname) | name + version *UNIQUE*
version | 版本号 | 若不指定版本号，则使用最新版本的工作流蓝图
taskMetadatas | 任务蓝图的列表 | 具体参考AT任务蓝图定义
### 2.2 AtTaskMetadata类：AT任务蓝图
域 | 描述 | 备注
---|---| ---
name | 任务名(nickname) | *UNIQUE*
type | 全限定类名 | 反射
mode | 执行模式 | **SYNC**: 同步 **ASYNC**: 异步
inputParameters | 任务的输入参数路径定义 | ATF据此路径来解析实际输入数据
retryCount | 当任务被标记为FAILED时，尝试重试的次数 | 
retryDelaySeconds | 重试的延时阈值（毫秒） | 
retryLogic | 重试逻辑 | **FIXED**: retryDelaySeconds后重试 **EXPONENTIAL_BACKOFF**: (retryDelaySeconds * 剩余重试次数)后重试
timeoutSeconds | 超时阈值（毫秒） | 设为0则表示不启用超时策略
timeoutPolicy | 超时策略 | **RETRY**: 重试 **TIME_OUT_WF**: 任务被标记为TIMEOUT状态并终止
parentName | 父级任务蓝图名 | 
### 2.3 自动装配AT任务和AT工作流的输入与输出
参数路径占位符：
```
${workflow/任务名.input/output.键}
```
ATF基于该参数路径占位符，从AT工作流或已执行的某个AT任务的inputData或outputData中的相应字段取值，来初始化待执行的AT任务的inputData或outputData。<br />
系统参数路径占位符：
```
#{}
```
## 3 AtfWorkflowSvs服务: 创建、执行AT工作流
AtfWorkflowSvs服务负责AT工作流的执行和持久化、读取查询等操作，并提供了生成器AtWorkflowBuilder基于已注册的蓝图创建AT执行流实例。
### 3.1 AtWorkflow: AT工作流
基于JSON DSL (domain-specific language)蓝图定义的AT工作流，包含一组AT任务。
#### 3.1.1 AT工作流的生命周期
![AT工作流Status](/dev-book/uml/AtWorkflowStatus.png)<br />
**SUSPENDED**: 挂起。代表AT工作流中的某一个任务要求挂起。<br />
**TERMINATED**: 终止。代表AT工作流中的某一个任务失败。<br />
**COMPLETED**: 成功。代表AT工作流中的所有任务都是COMPLETED。<br />
### 3.2 AtTask: AT抽象任务类
AT任务分为原子交易类任务、管理类任务。
一个AT任务可以在多个AT工作流内重用。
#### 3.2.1 AT任务的有限状态机
如果AT任务涉及状态迁移，可实现有限状态机。当前状态的动作执行完成后，需迁移到下一个指定状态并置AtTaskStatus，然后通知ATF保存并做后续处理。
#### 3.2.2 AT任务的生命周期
![AT任务Status](/dev-book/uml/AtTaskStatus.png)<br />
**CONTINUED**: 保存点。代表AT任务在执行过程中的某个时间点（通常是处于状态机的某个状态），需要先保存再继续执行。（注意AT任务必须实现有限状态机！）<br />
**SUSPENDED**: 挂起。代表AT任务在执行过程中的某个时间点（通常是处于状态机的某个状态），所依赖的外部资源未准备就绪（如等待客户付款），需要保存后立即挂起；直到外部资源准备就绪后再唤起流程继续执行。（注意AT任务必须实现有限状态机！）<br />
**FAILED**: 失败。代表AT任务执行失败，先触发重试逻辑，否则终止工作流。<br />
**COMPLETED**: 成功。代表AT任务执行成功，此时ATF会将当前执行的任务ID下移，保存后继续执行。<br />
#### 3.2.3 原子交易类任务
原子交易类任务与具体业务相关，并且COMPLETED会做一笔会计记账。
#### 3.2.4 AtTaskScript: Groovy脚本类任务
将“变化的部分”通过Groovy脚本来实现并与业务代码解耦合，脚本通过Groovy Bindings可使用ATF框架基础服务bean、获取业务数据（如AT任务的inputData）、修改输出数据（如AT任务的outputData）等。
#### 3.2.5 AtContainer: 容器类
## 4 IAtfEvent接口：MQ服务
AtfEvent服务负责异步化、延迟执行AT工作流等操作。ATF默认使用RabbitMQ消息队列，也可以通过实现IAtfEvent接口来切换成其它方案。
## 5 事务
> 注意：ATF接口的调用者，不再做事务管理，统一交由ATF自身控制事务边界。
### 5.1 AtfRedisTransaction服务: Redis事务 - 乐观锁