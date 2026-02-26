<script setup lang="ts">

import {taskStatusEnum} from "@/enums/TaskStatusEnum";
import CardContainer from "@/components/CardContainer/CardContainer.vue";
import FlexFullScrollContainer from "@/components/FlexFullScrollContainer/FlexFullScrollContainer.vue";
import FlowableDetail from "@/components/flowable/flowableDetail.vue";
import {ref} from "vue";
import common from "@/utils/common";
import flowableApi from "@/api/flowableApi";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {message} from "@/utils/message";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import IconifyIconOnline from "@/components/ReIcon/src/iconifyIconOnline";
import FlowableVariableAction from "@/components/flowable/flowableVariableAction.vue";


defineOptions({
  name: 'flowableInsAction'
})

interface Prop {
  processId:string,
  allowAction?:boolean
}

const prop = withDefaults(defineProps<Prop>(), {
  allowAction: ()=>true
})
const flowableDetailRef = ref()
const refreshTestProcInsDetail = () => {
  flowableDetailRef.value.refreshData()
}

const dictMapping = ref({})

type ValueOf<T> = T[keyof T];
const testExecute = async (executeType:ValueOf<typeof taskStatusEnum>) =>{
  let defaultData = ref({})
  let executeInfoRes:HttpResult = await common.handleRequestApi(flowableApi.getExecuteInfoByProcessId(prop.processId))
  let taskIdAndUserIdMapping = executeInfoRes.data.taskIdAndUserIdMapping
  dictMapping.value = {
    taskId:executeInfoRes.data.taskIdAndUserNameMapping,
    rollbackTargetActId:executeInfoRes.data.allowRollBackIdAndNameMapping,
    transferUserId:executeInfoRes.data.userIdAndNameMapping
  }

  let columns: Array<DetailColumnDefine> = []
  if (taskStatusEnum.success===executeType
      ||taskStatusEnum.reject===executeType
      ||taskStatusEnum.suspend===executeType){
    columns = [
      {prop: 'taskId', label: '执行节点',type: QueryTypeEnum.OPTION},
      {prop: 'msg', label: '原因',type: QueryTypeEnum.AREA_INPUT,placeholder:'为空则使用默认值'},
    ]
  }
  if (taskStatusEnum.rollback===executeType
      ||taskStatusEnum.rollBackRecordMsg===executeType){
    columns = [
      {prop: 'rollbackTargetActId', label: '目标回滚节点',type: QueryTypeEnum.MULTIPLE_OPTION,placeholder:'不选择目标回滚节点则为直接回滚'},
      {prop: 'taskId', label: '执行节点',type: QueryTypeEnum.OPTION},
      {prop: 'msg', label: '原因',type: QueryTypeEnum.AREA_INPUT},
    ]
  }
  if (taskStatusEnum.transfer===executeType){
    columns = [
      {prop: 'taskId', label: '执行节点',type: QueryTypeEnum.OPTION},
      {prop: 'transferUserId', label: '转办人员',type: QueryTypeEnum.OPTION},
    ]
  }
  let params:OpenInputDialogDefine = {
    columns: columns,
    defaultValue: defaultData,
    dictMapping:dictMapping,
    callBack:(result)=>{
      console.log(result)
      let data = {
        processInstanceId: prop.processId,
        taskId: result.data.taskId,
        userId: taskIdAndUserIdMapping[result.data.taskId],
        rollbackTargetActIdList: result.data.rollbackTargetActId,
        executeType: executeType,
        transferUserId: result.data.transferUserId,
        msg: result.data.msg
      }
      common.handleRequestApi(flowableApi.executeProcess(data)).then(res=>{
        message(res.msg,{type:'success'})
        refreshTestProcInsDetail()
        result.done()
      })
    }
  }
  common.openInputDialog(params)
}

/*
流程变量操作
 */
interface ProcessVariableProp {
  key:string,
  value:string
}
const processVariableFlag = ref(false)
const processVariableData = ref<ProcessVariableProp[]>([])
const processVariableColumns = ref<ColumnDefine[]>([
  {prop: 'key', label: '流程变量KEY',type: ColumnTypeEnum.COMMON},
  {prop: 'value', label: '流程变量VALUE',type: ColumnTypeEnum.COMMON}
])
const processVariable =  () =>{
  common.handleRequestApi(flowableApi.getProcessVariableList(prop.processId)).then(res=>{
    processVariableData.value = res.data
    processVariableFlag.value = true
  })
}
const saveOrUpdateProcessVariable = (data:VariableDataProp)=>{
  variableDataList.value = []
  variableDataList.value.push(data)
  variableChangeFlag.value = true
  variableChangeFn = ()=>{
    common.handleRequestApi(flowableApi.saveOrUpdateProcessVariable(prop.processId,data)).then(res=>{
      message(res.msg,{type:'success'})
      processVariable()
      variableChangeFlag.value = false
    })
  }
}
const deleteProcessVariable =(row:ProcessVariableProp, index)=>{
  common.showMsgDialog('是否删除流程变量->'+row.key).then(()=>{
    let data:VariableDataProp = {
      key:row.key
    }
    common.handleRequestApi(flowableApi.deleteProcessVariable(prop.processId,data)).then(res=>{
      message(res.msg,{type:'success'})
      processVariable()
    })
  })
}


/*
任务变量操作
 */
interface TaskVariableDetailProp {
  key:string,
  value:string
}
interface TaskVariableProp {
  taskId:string,
  name:string,
  variableList:TaskVariableDetailProp[]
}
const taskVariableFlag = ref(false)
const taskVariableData = ref<TaskVariableProp[]>([])
const taskVariableColumns = ref<ColumnDefine[]>([
  {prop: 'key', label: '流程变量KEY',type: ColumnTypeEnum.COMMON},
  {prop: 'value', label: '流程变量VALUE',type: ColumnTypeEnum.COMMON}
])
const taskVariable = () => {
  common.handleRequestApi(flowableApi.getTaskVariableList(prop.processId)).then(res=>{
    taskVariableData.value = res.data
    taskVariableFlag.value = true
  })
}

const saveOrUpdateTaskVariable = (data:VariableDataProp, taskId:string)=>{
  variableDataList.value = []
  variableDataList.value.push(data)
  variableChangeFlag.value = true
  variableChangeFn = ()=>{
    common.handleRequestApi(flowableApi.saveOrUpdateTaskVariable(prop.processId,taskId,data)).then(res=>{
      message(res.msg,{type:'success'})
      taskVariable()
      variableChangeFlag.value = false
    })
  }
}
const deleteTaskVariable = (row:TaskVariableDetailProp, taskId:string)=>{
  common.showMsgDialog('是否删除任务变量->'+row.key).then(()=>{
    let data:VariableDataProp = {
      key:row.key
    }
    common.handleRequestApi(flowableApi.deleteTaskVariable(prop.processId,taskId,data)).then(res=>{
      message(res.msg,{type:'success'})
      taskVariable()
    })
  })
}


/*
变量变更操作
 */
const variableChangeFlag = ref(false)
const variableDataList = ref<VariableDataProp[]>([])
let variableChangeFn:()=>void
const variableChange = ()=>{
  variableChangeFn?.()
}
</script>

<template>
  <div class="w-full h-full">
    <card-container :show-footer="false">
      <template v-if="allowAction" #header>
        <div class="flex flex-row gap-1">
          <el-button  type="primary" size="small" @click="refreshTestProcInsDetail">刷新</el-button>
          <el-button  type="primary" size="small" @click="()=>testExecute(taskStatusEnum.success)">推进</el-button>
          <el-button  type="primary" size="small" @click="()=>testExecute(taskStatusEnum.reject)">拒绝</el-button>
          <el-button  type="primary" size="small" @click="()=>testExecute(taskStatusEnum.suspend)">中止</el-button>
          <el-button  type="primary" size="small" @click="()=>testExecute(taskStatusEnum.transfer)">转办</el-button>
          <el-button  type="primary" size="small" @click="()=>testExecute(taskStatusEnum.rollBackRecordMsg)">回滚带记录</el-button>
          <el-button  type="primary" size="small" @click="()=>testExecute(taskStatusEnum.rollback)">回滚不带记录</el-button>

          <el-button  type="primary" size="small" @click="()=>processVariable()">流程变量</el-button>
          <el-button  type="primary" size="small" @click="()=>taskVariable()">任务变量</el-button>
        </div>
      </template>
      <template #default>
        <flex-full-scroll-container>
          <el-scrollbar height="100%">
            <flowable-detail ref="flowableDetailRef" :process-id="prop.processId"/>
          </el-scrollbar>
        </flex-full-scroll-container>
      </template>
    </card-container>
    <el-drawer v-model="variableChangeFlag" size="50%" append-to-body>
      <template #header>
        <div class="flex justify-between items-center">
          <div style="font-size: 18px;font-weight: bolder;color: black">变量新增或修改</div>
        </div>
      </template>
      <div class="w-full h-full flex flex-col justify-start gap-5">
        <el-button-group>
          <el-button  style="font-weight: bold" type="primary" @click="variableChange()">保存</el-button>
        </el-button-group>
        <FlowableVariableAction v-model:variable-data-list="variableDataList" :can-add="false" :can-delete="false"/>
      </div>
    </el-drawer>

    <el-drawer v-model="processVariableFlag" size="50%" append-to-body>
      <template #header>
        <div class="flex justify-between items-center">
          <div style="font-size: 18px;font-weight: bolder;color: black">流程变量</div>
        </div>
      </template>
      <div class="w-full h-full flex flex-col justify-start gap-5">
        <el-button-group>
          <el-button  style="font-weight: bold" type="primary" @click="saveOrUpdateProcessVariable({})">新增</el-button>
        </el-button-group>
        <el-table
            style="font-size: 12px;"
            element-loading-text="加载中..."
            :data="processVariableData"
            :border="true">
          <el-table-column  header-align="center" align="center" label="删除操作" width="100">
            <template #default="scope">
              <div class="w-full h-full flex justify-center">
                <iconify-icon-online icon="ri:delete-bin-5-fill"
                                     class="text-red-600 cursor-pointer w-[20px] h-[20px]"
                                     @click="deleteProcessVariable(scope.row,scope.$index)"/>
              </div>
            </template>
          </el-table-column>
          <el-table-column  header-align="center" align="center" label="修改操作" width="100">
            <template #default="scope">
              <div class="w-full h-full flex justify-center">
                <iconify-icon-online icon="eva:edit-2-fill"
                                     class="text-red-600 cursor-pointer w-[20px] h-[20px]"
                                     @click="saveOrUpdateProcessVariable({key:scope.row.key,value:scope.row.value,type:'String'})"/>
              </div>
            </template>
          </el-table-column>
          <template v-for="item in processVariableColumns" :key="item.prop">
            <CommonTableColumn  :item="item" :dict-list="dictMapping"/>
          </template>
        </el-table>
      </div>
    </el-drawer>
    <el-drawer v-model="taskVariableFlag" size="50%" append-to-body>
      <template #header>
        <div class="flex justify-between items-center">
          <div style="font-size: 18px;font-weight: bolder;color: black">任务变量</div>
        </div>
      </template>

      <template v-for="(item,index) in taskVariableData" :key="index">
        <el-divider>{{`${item.name}`}}</el-divider>
        <el-divider>{{`${item.taskId}`}}</el-divider>
        <div class="w-full h-fit flex flex-col justify-start gap-5">
          <el-button-group>
            <el-button  style="font-weight: bold" type="primary" @click="saveOrUpdateTaskVariable({},item.taskId)">新增</el-button>
          </el-button-group>
          <el-table
              style="font-size: 12px;"
              element-loading-text="加载中..."
              :data="item.variableList"
              :border="true">
            <el-table-column  header-align="center" align="center" label="删除操作" width="100">
              <template #default="scope">
                <div class="w-full h-full flex justify-center">
                  <iconify-icon-online icon="ri:delete-bin-5-fill"
                                       class="text-red-600 cursor-pointer w-[20px] h-[20px]"
                                       @click="deleteTaskVariable(scope.row,item.taskId)"/>
                </div>
              </template>
            </el-table-column>
            <el-table-column  header-align="center" align="center" label="修改操作" width="100">
              <template #default="scope">
                <div class="w-full h-full flex justify-center">
                  <iconify-icon-online icon="eva:edit-2-fill"
                                       class="text-red-600 cursor-pointer w-[20px] h-[20px]"
                                       @click="saveOrUpdateTaskVariable({key:scope.row.key,value:scope.row.value,type:'String'},item.taskId)"/>
                </div>
              </template>
            </el-table-column>
            <template v-for="item in taskVariableColumns" :key="item.prop">
              <CommonTableColumn  :item="item" :dict-list="dictMapping"/>
            </template>
          </el-table>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped lang="scss">

</style>