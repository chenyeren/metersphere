export default {
  'apiScenario.env': 'Environment: {name}',
  'apiScenario.allScenario': 'All scenarios',
  'apiScenario.createScenario': 'Create scenario',
  'apiScenario.importScenario': 'Import scenario',
  'apiScenario.tree.selectorPlaceholder': 'Please enter the module name',
  'apiScenario.tree.folder.allScenario': 'All scenarios',
  'apiScenario.tree.recycleBin': 'Recycle bin',
  'apiScenario.tree.noMatchModule': 'No matching module/scene yet',
  'apiScenario.createSubModule': 'Create sub-module',
  'apiScenario.module.deleteTipTitle': 'Delete {name} module?',
  'apiScenario.module.deleteTipContent':
    'After deletion, all scenarios under the module will be deleted synchronously. Please operate with caution.',
  'apiScenario.deleteConfirm': 'Confirm',
  'apiScenario.deleteSuccess': 'Success',
  'apiScenario.moveSuccess': 'Success',
  'apiScenario.baseInfo': 'Base info',
  'apiScenario.step': 'Step',
  'apiScenario.params': 'Params',
  'apiScenario.prePost': 'Pre/Post',
  'apiScenario.assertion': 'Assertion',
  'apiScenario.executeHistory': 'Execute history',
  'apiScenario.changeHistory': 'Change history',
  'apiScenario.dependency': 'Dependencies',
  'apiScenario.quote': 'Reference',
  'apiScenario.params.convention': 'Convention parameter',
  'apiScenario.params.searchPlaceholder': 'Search by name or tag',
  'apiScenario.params.priority':
    'Variable priority: Temporary parameters>Scene parameters>Environment parameters>Global parameters; Note: Avoid using variables with the same name. When using variables with the same name, scene level CSV has the highest priority',
  'apiScenario.params.name': 'Variable name',
  'apiScenario.params.type': 'Type',
  'apiScenario.params.paramValue': 'Parameter value',
  'apiScenario.params.tag': 'Tag',
  'apiScenario.params.desc': 'Description',
  'apiScenario.table.columns.name': 'Name',
  'apiScenario.table.columns.level': 'Level',
  'apiScenario.table.columns.status': 'status',
  'apiScenario.table.columns.runResult': 'Run result',
  'apiScenario.table.columns.tags': 'Tags',
  'apiScenario.table.columns.scenarioEnv': 'Scenario environment',
  'apiScenario.table.columns.steps': 'Steps',
  'apiScenario.table.columns.passRate': 'Pass rate',
  'apiScenario.table.columns.module': 'Module',
  'apiScenario.table.columns.createUser': 'Create user',
  'apiScenario.table.columns.createTime': 'Create time',
  'apiScenario.table.columns.updateUser': 'Update user',
  'apiScenario.table.columns.updateTime': 'Update time',
  'apiScenario.execute': 'Execute',
  // 批量操作文案
  'api_scenario.batch_operation.success': 'Success {opt} to {name}',
  'api_scenario.table.batchMoveConfirm': 'Ready to {opt} {count} scenarios',
  // 执行历史
  'apiScenario.executeHistory.searchPlaceholder': 'Search by ID or name',
  'apiScenario.executeHistory.num': 'Number',
  'apiScenario.executeHistory.execution.triggerMode': 'Trigger mode',
  'apiScenario.executeHistory.execution.status': 'Execution result',
  'apiScenario.executeHistory.execution.operator': 'Operator',
  'apiScenario.executeHistory.execution.operatorTime': 'Operation time',
  'apiScenario.executeHistory.execution.operation': 'Execution result',
  'apiScenario.executeHistory.status.pending': 'Pending',
  'apiScenario.executeHistory.status.running': 'Running',
  'apiScenario.executeHistory.status.rerunning': 'Rerunning',
  'apiScenario.executeHistory.status.error': 'Error',
  'apiScenario.executeHistory.status.success': 'Success',
  'apiScenario.executeHistory.status.fake.error': 'Fake error',
  'apiScenario.executeHistory.status.fake.stopped': 'Stopped',
  // 操作历史
  'apiScenario.historyListTip':
    'View and compare historical changes. According to the rules set by the administrator, the change history data will be automatically deleted.',
  'apiScenario.changeOrder': 'Change serial number',
  'apiScenario.type': 'Type',
  'apiScenario.operationUser': 'Operator',
  'apiScenario.updateTime': 'Update time',

  // 回收站
  'api_scenario.recycle.recover': 'Recover',
  'api_scenario.recycle.list': 'Recycle list',
  'api_scenario.recycle.batchCleanOut': 'Delete',
  'api_scenario.table.searchPlaceholder': 'Search by ID/Name/Tag',

  'apiScenario.scriptOperationName': 'Script operation name',
  'apiScenario.scriptOperationNamePlaceholder': 'Please enter the script operation name',

  'apiScenario.setting.cookie.config': 'Cookie configuration',
  'apiScenario.setting.environment.cookie': 'Environment Cookie',
  'apiScenario.setting.share.cookie': 'Shared Cookie',
  'apiScenario.setting.run.config': 'Run configuration',
  'apiScenario.setting.step.waitTime': 'Step wait time',
  'apiScenario.setting.waitTime': 'Wait time',
  'apiScenario.setting.step.rule': 'Step execution failure rule',
  'apiScenario.setting.step.rule.ignore': 'Ignore error and continue execution',
  'apiScenario.setting.step.rule.stop': 'Stop/end execution',
  'apiScenario.setting.cookie.config.tip':
    'When there are both global and scene variable cookies, shared cookies will overwrite both global and scene variable cookies',
  'apiScenario.setting.share.cookie.tip':
    'As long as the system extracts the returned cookie information from the result of a certain step, the subsequent steps will use this cookie. If a cookie variable is added to the request, it will also be overwritten',
  'apiScenario.setting.waitTime.tip':
    'When running a scenario, each step of the scenario will wait for a certain time after execution before triggering the next step to start execution',
};
