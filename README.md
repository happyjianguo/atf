# ATF - Atomic Transaction Framework, 原子交易框架
  (Jie © 2017)<br>

AT（原子交易，Atomic Transaction）是指一笔交易中的原子步骤，不可细分的最小单位。AT的不同组合，构成了具体的交易场景。AT的核心业务逻辑是会计记账。
## 1 架构设计
ATF（原子交易框架，Atomic Transaction Framework）是流程编制引擎+会计记账规则<br />
ATF支持：
1. 基于业务流(business flow)编制AT任务(**AtTask**)
2. 基于JSON DSL (domain-specific language)的蓝图，用来定义AT工作流(**AtWorkflow**) 
3. 同步、异步化执行能力
4. 重试(RETRY)、超时(TIMEOUT)、挂起(SUSPEND) AT任务
5. AT任务可重用
6. MQ服务(**AtfEvent**)解耦领域服务，便于通过扩容来提升执行能力

ATF默认使用RabbitMQ消息队列和MySQL持久化方案，也可以通过实现IAtfEvent和IAtRepository接口来切换成其它方案。
## 2 AtfMetadataSvs服务: 生成、注册蓝图
AtfMetadataSvs服务负责AT工作流蓝图（基于JSON DSL）的注册、更新、读取查询等操作，并提供了蓝图生成器AtfMetadataBuilder快速生成蓝图。ATF根据已注册的蓝图创建并执行AT工作流实例。
> 建议的命名规范：
> 1. AT工作流蓝图名 = 场景名:appid，确保name:version唯一；则AT工作流的别名 = 蓝图名:v版本号
> 2. AT任务蓝图名 = 类名:appid:时间戳，确保name唯一；则AT任务的别名 = 蓝图名

示例：

```
{
    "id": 1,
    "createDate": 1515724926676,
    "updateDate": null,
    "name": "Scenario1Container:appid",
    "version": 1,
    "taskMetadatas": [
        {
            "name": "AtContainer:appid:1515724926674",
            "type": "jie.atf.core.domain.AtContainer",
            "mode": "SYNC",
            "step": 1,
            "inputParameters": {},
            "retryCount": null,
            "retryDelaySeconds": null,
            "retryLogic": null,
            "timeoutSeconds": null,
            "timeoutPolicy": null,
            "parentName": null
        },
        {
            "name": "DemoAtTask101:appid:1515724926674:0",
            "type": "jie.atf.demo.domain.DemoAtTask101",
            "mode": "SYNC",
            "step": 2,
            "inputParameters": {
                "agreementRequestId": "${workflow.input.agreementRequestId}",
                "childTransAmount0": "${workflow.input.childTransAmount0}",
                "totalSize": "${workflow.input.totalSize}",
                "bankAccountId": "${workflow.input.bankAccountId}"
            },
            "retryCount": 3,
            "retryDelaySeconds": 1000,
            "retryLogic": "EXPONENTIAL_BACKOFF",
            "timeoutSeconds": 30000,
            "timeoutPolicy": "RETRY",
            "parentName": "AtContainer:appid:1515724926674"
        },
        {
            "name": "DemoAtTask301:appid:1515724926674",
            "type": "jie.atf.demo.domain.DemoAtTask301",
            "mode": "SYNC",
            "step": 4,
            "inputParameters": {
                "transAmount": "${workflow.input.transAmount}",
                "agreementRequestId": "${workflow.input.agreementRequestId}",
                "bankAccountId": "${workflow.input.bankAccountId}"
            },
            "retryCount": 5,
            "retryDelaySeconds": 5000,
            "retryLogic": "FIXED",
            "timeoutSeconds": 60000,
            "timeoutPolicy": "TIME_OUT_WF",
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
step | 交易中的第几步 | 
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
ATF基于该参数路径占位符，从AT工作流或已执行的某个AT任务的inputData或outputData中的相应字段取值，来初始化待执行的AT任务的inputData或outputData。
系统参数路径占位符：
```
#{}
```
## 3 AtfWorkflowSvs服务: 创建、执行AT工作流
AtfWorkflowSvs服务负责AT工作流的执行和持久化、读取查询等操作，并提供了生成器AtWorkflowBuilder基于已注册的蓝图创建AT执行流实例。
### 3.1 AtWorkflow: AT工作流
基于JSON DSL (domain-specific language)蓝图定义的AT工作流，包含一组AT任务。
#### 3.1.1 AT工作流的生命周期
![image](/dev-book/uml/AtWorkflowStatus.png)
**SUSPENDED**: 挂起。代表AT工作流中的某一个任务要求挂起。<br />
**TERMINATED**: 终止。代表AT工作流中的某一个任务失败。<br />
**COMPLETED**: 成功。代表AT工作流中的所有任务都是COMPLETED。<br />
### 3.2 AtTask: AT抽象任务类
AT任务分为原子交易类任务、管理类任务。
一个AT任务可以在多个AT工作流内重用。
#### 3.2.1 AT任务的生命周期
![image](/dev-book/uml/AtWorkflowStatus.png)
**CONTINUED**: 保存点。代表AT任务在执行过程中的某个时间点（通常是处于状态机的某个状态），需要先保存再继续执行。（注意AT任务必须实现有限状态机！）<br />
**SUSPENDED**: 挂起。代表AT任务在执行过程中的某个时间点（通常是处于状态机的某个状态），所依赖的外部资源未准备就绪（如等待客户付款），需要保存后立即挂起；直到外部资源准备就绪后再唤起流程继续执行。（注意AT任务必须实现有限状态机！）<br />
**FAILED**: 失败。代表AT任务执行失败，先触发重试逻辑，否则终止工作流。<br />
**COMPLETED**: 成功。代表AT任务执行成功，此时ATF会将当前执行的任务ID下移，保存后继续执行。<br />
