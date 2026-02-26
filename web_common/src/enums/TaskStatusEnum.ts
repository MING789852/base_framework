export const taskStatusEnum = {
    success: 'success',
    reject: 'reject',
    suspend: 'suspend',
    rollback: 'rollback',
    rollBackRecordMsg: 'rollBackRecordMsg',
    transfer: 'transfer',
    // delete:'delete'
}

export const taskStatusDictEnum = {
    success: '同意',
    reject: '拒绝',
    suspend: '中止',
    rollback: '回滚',
    rollBackRecordMsg: '退回',
    transfer: '转办',
    // delete:'删除'
}

export const userTaskApprovalTypeEnum = {
    single: 'single',
    all: 'all',
    or: 'or'
}

export const userTaskApprovalTypeDictEnum = {
    single: '单签',
    all: '会签',
    or: '或签'
}

export const serviceTaskApprovalTypeEnum = {
    cc: 'cc',
    customExpression: 'customExpression'
}
export const serviceTaskApprovalTypeDictEnum = {
    cc: '抄送',
    customExpression: '自定义表达式'
}

export const bpmnTypeEnum = {
    startEvent: 'bpmn:startEvent',
    userTask: 'bpmn:userTask',
    serviceTask: 'bpmn:serviceTask',
    parallelGateway: 'bpmn:parallelGateway',
    exclusiveGateway: 'bpmn:exclusiveGateway',
    endEvent: 'bpmn:endEvent',
    sequenceFlow: 'bpmn:sequenceFlow',
}

export const userConfigActionTypeEnum = {
    fixedSelect: 'fixedSelect',
    customVar: 'customVar'
}

export const userConfigActionTypeDictEnum = {
    fixedSelect: '固定人员选择',
    customVar: '自定义变量'
}

export const variableDataTypeDict = {
    'String':  'String',
    'BigDecimal': 'BigDecimal',
    'Integer': 'Integer',
    'Boolean': 'Boolean',
    'Double': 'Double',
    'Float': 'Float',
    'Long':'Long',
    'ListJson': 'ListJson'
}