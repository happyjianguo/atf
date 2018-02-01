##########
#AT任务
CREATE TABLE AtTask (
Id INT(16) NOT NULL AUTO_INCREMENT, #任务ID
ParentId INT(16), #父级任务ID
NextId INT(16), #下一个任务ID
Name VARCHAR(64), #任务名(nickname)。UNIQUE
Type VARCHAR(64), #全限定类名。反射
Mode VARCHAR(64), #执行模式
InputParameters VARCHAR(1024), #任务的输入参数JSON定义
RetryCount INT(8), #Retry机制：尝试重试的次数
RetryDelaySeconds INT(8), #Retry机制：重试的延时阈值（毫秒）
RetryLogic VARCHAR(64), #Retry机制：重试逻辑
RetriedCount INT(8), #Retry机制：已重试的次数
TimeoutSeconds INT(8), #Timeout机制：超时阈值（毫秒）
TimeoutPolicy VARCHAR(64), #Timeout机制：超时策略
Status VARCHAR(64), #生命周期
StatusDate DATETIME,
State VARCHAR(64), #有限状态机
TransType VARCHAR(64), #交易类型
InputData VARCHAR(1024), #输入数据
OutputData VARCHAR(1024), #输出数据
WorkflowId INT(16) NOT NULL, #工作流ID
Scenario VARCHAR(64), #交易场景
ClientId INT(16), #客户ID
StartDate DATETIME, #开始执行的时间
EndDate DATETIME, #执行完成的时间
AgreementRequestId INT(16), #交易ID
BankAccountId INT(16), #银行卡ID
TransAmount VARCHAR(1024), #交易金额
GroovyCondition VARCHAR(2048), #Groovy脚本：判断条件
GroovyBody VARCHAR(2048), #Groovy脚本：执行体
UNIQUE(Name),
CONSTRAINT pk PRIMARY KEY (Id)
);
#AT流程
CREATE TABLE AtWorkflow (
Id INT(16) NOT NULL AUTO_INCREMENT, #工作流ID
CurrentTaskId INT(16), #当前执行的任务ID
Name VARCHAR(64), #工作流名(nickname)。UNIQUE
Version INT(8), #版本号
tasks VARCHAR(4096), #任务列表的JSON
Scenario VARCHAR(64), #场景
ClientId INT(16), #客户ID
Status VARCHAR(64), #生命周期
StatusDate DATETIME,
InputData VARCHAR(1024), #输入数据
OutputData VARCHAR(1024), #输出数据
StartDate DATETIME, #开始执行的时间
EndDate DATETIME, #执行完成的时间
UNIQUE(Name),
CONSTRAINT pk PRIMARY KEY (Id)
);

##########
#AT任务蓝图
CREATE TABLE AtTaskDef (
Id INT(16) NOT NULL AUTO_INCREMENT,
Name VARCHAR(64), #任务名(nickname)。UNIQUE
Type VARCHAR(64), #全限定类名。反射
Mode VARCHAR(64), #执行模式
InputParameters VARCHAR(1024), #任务的输入参数JSON定义
RetryCount INT(8), #Retry机制：尝试重试的次数
RetryDelaySeconds INT(8), #Retry机制：重试的延时阈值（毫秒）
RetryLogic VARCHAR(64), #Retry机制：重试逻辑
TimeoutSeconds INT(8), #Timeout机制：超时阈值（毫秒）
TimeoutPolicy VARCHAR(64), #Timeout机制：超时策略
ParentName VARCHAR(64), #父级任务蓝图名
UNIQUE(Name),
CONSTRAINT pk PRIMARY KEY (Id)
);
#AT工作流蓝图
CREATE TABLE AtWorkflowMetadata (
Id INT(16) NOT NULL AUTO_INCREMENT,
Name VARCHAR(64), #工作流名(nickname)
Version INT(8), #版本号
taskMetadatas VARCHAR(4096), #任务蓝图列表的JSON
CONSTRAINT pk PRIMARY KEY (Id)
);
