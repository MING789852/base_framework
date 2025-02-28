<template>
  <div class="parent">
    <common-table ref="commonTableRef"  :columns="columns"
                  :queryColumns="queryColumns" :queryInHeader="false" :dictList="dictMapping"
                  :api="systemRoleApi" :tableFn="tableFn" :table-button="tableButton">
      <template #column>
        <el-table-column header-align="center" align="center" label="操作" >
          <template #default="scope">
            <a style="cursor: pointer;color: #409EFF" @click="assignedUser(scope.row)">用户</a>
            <a style="cursor: pointer;color: #409EFF;margin-left: 5px" @click="assignRouters(scope.row)">分配菜单</a>
            <a style="cursor: pointer;color: #409EFF;margin-left: 5px" @click="assignRouterAction(scope.row)">分配操作</a>
          </template>
        </el-table-column>
      </template>
      <template #dialog>
        <el-dialog v-model="assignRouterActionFlag" destroy-on-close fullscreen append-to-body title="分配操作">
          <router-action-ref :role="assignRouterActionData" @finish="finishAssignRouterActionData"/>
        </el-dialog>
        <el-dialog v-model="assignedUserDialogFlag" destroy-on-close fullscreen append-to-body>
          <common-table v-if="assignedUserDialogFlag" ref="assignedUserCommonTableRef" :table-type="'action'" :api="systemRoleApi" :columns="assignedUserColumns"
                        :table-button="assignedUserTableButton" :table-fn="assignedUserTableFn" :query-columns="assignedUserQueryColumns"
                        :show-selection="false"
                        height="75vh">
            <template #button_end>
              <el-button  class="font-bold" size="small" type="primary" @click="roleAddUser">添加用户</el-button>
              <el-button  class="font-bold" size="small" type="primary" @click="unRelUserAndRoleAll">解除全部</el-button>
            </template>
            <template #column>
              <el-table-column header-align="center" align="center" label="操作" >
                <template #default="scope">
                  <a style="cursor: pointer;color: #409EFF" @click="unRelUserAndRole(scope.row)">解除</a>
                </template>
              </el-table-column>
            </template>
          </common-table>
        </el-dialog>
      </template>
    </common-table>
  </div>
</template>

<script setup lang="tsx">
import {ref, onMounted} from "vue";
import {message} from "@/utils/message";
import {addDialog, closeDialog, type DialogOptions,} from "@/components/ReDialog";
import common from '@/utils/common'
import menu from '@/views/system/menu.vue';
import systemRoleApi from "@/api/systemRoleApi";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import CommonTable from "@/components/table/commonTable.vue";
import TableFnClass from "@/class/TableFnClass";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import routerActionRef from '@/views/system/routerAction/routerActionRef.vue'
import systemUserApi from "@/api/systemUserApi";

defineOptions({
  name: "system-role"
});
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: true,
  saveFn: true,
  deleteFn: true,
  refreshFn: true,
  initQueryFn: false,
  exportExcelFn: false
})
let commonTableRef = ref<CommonTableType>(null)
const columns = ref<Array<ColumnDefine>>([
  {prop: "roleCode", label: "角色编码", width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "roleName", label: "角色名称", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
])
const queryColumns = ref<Array<ColumnDefine>>([
  {prop: "roleCode", label: "角色编码", width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "roleName", label: "角色名称", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
])
const dictMapping = ref({
  judgeEnable: {
    1:'可用',
    0:'禁用'
  }
})
onMounted(() => {
  //加载数据
  commonTableRef.value.getData()
})

/**
 * 页面事件
 */
const assignedUserDialogFlag = ref(false)
const assignedRoleId = ref('')
const assignedUserCommonTableRef = ref<CommonTableType>()
const assignedUserTableFn = new TableFnClass()
const assignedUserTableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: false,
  saveFn: false,
  deleteFn: false,
  refreshFn: false,
  initQueryFn: true,
  exportExcelFn: false
})
const assignedUserQueryColumns = ref<Array<ColumnDefine>>([
  {label: "用户", prop: "userName_$_like",queryType:QueryTypeEnum.INPUT},
  // {label: "所属部门", prop: "deptName_$_like",queryType:QueryTypeEnum.INPUT},
]);
const assignedUserColumns = ref<Array<ColumnDefine>>([
  {fixed: true, label: "用户", prop: "userName", query: false, type: ColumnTypeEnum.COMMON},
  // {fixed: true, label: "所属部门", prop: "deptName", query: false, type: ColumnTypeEnum.COMMON}
])
assignedUserTableFn.queryFn = () => {
  assignedUserCommonTableRef.value.getData()
}
assignedUserTableFn.getData = () => {
  common.handleRequestApi(systemRoleApi.selectUserAndDeptPageByRoleId(assignedRoleId.value,assignedUserCommonTableRef.value.query))
    .then((res:HttpResult)=>{
      let data = res.data
      assignedUserCommonTableRef.value.query.current = data.current
      assignedUserCommonTableRef.value.query.total = data.total
      assignedUserCommonTableRef.value.query.size = data.size
      assignedUserCommonTableRef.value.tableData = data.records
    })
}
const assignedUser = (row) => {
  assignedRoleId.value = row.id
  assignedUserDialogFlag.value = true
}
const roleAddUser = async () => {
  let userIdAndNickNameRes = await common.handleRequestApi(systemUserApi.getUserIdAndNickNameMapping())
  let columns: Array<DetailColumnDefine> = [
    {prop: 'userIdList', label: '用户', type: QueryTypeEnum.MULTIPLE_OPTION, disabled: false},
  ]
  let dictMapping = ref({
    userIdList: userIdAndNickNameRes.data
  })
  common.openInputDialog(columns, dictMapping, null, (result) => {
    common.handleRequestApi(systemRoleApi.roleAddUser(assignedRoleId.value,result.data.userIdList)).then(res => {
      assignedUserCommonTableRef.value.getData()
      message('操作成功',{type:'success'})
      result.done()
    })
  })
}
const unRelUserAndRole = (row) => {
  common.showMsgDialog('是否确认解除').then(res=>{
    common.handleRequestApi(systemRoleApi.unRelUserAndRole(assignedRoleId.value,row.userId))
      .then((res:HttpResult)=>{
        assignedUserCommonTableRef.value.getData()
        message('操作成功',{type:'success'})
      })
  })
}
const unRelUserAndRoleAll = () => {
  common.showMsgDialog('是否确认解除').then(res=>{
    common.handleRequestApi(systemRoleApi.unRelUserAndRoleAll(assignedRoleId.value,assignedUserCommonTableRef.value.query))
      .then((res:HttpResult)=>{
        assignedUserCommonTableRef.value.getData()
        message('操作成功',{type:'success'})
      })
  })
}
const assignRouterData = ref([])
const assignRouters = async (row) => {
  let res: any = await systemRoleApi.getRouterIdListByRole(row)
  if (res.code !== 200) {
    return message(res.msg, {type: "error"})
  }
  assignRouterData.value = res.data
  let options: DialogOptions = {
    fullscreen: true,
    title: "分配菜单",
    hideFooter: true,
    props: {
      judgeComponent: true,
      checkList: assignRouterData,
      multiCheck: true,
      closeDialogFn: () => {
        closeDialog(options, 0)
      },
      receiveFn: async (checkData) => {
        let data = {
          tcRole: row,
          routerList: checkData
        }
        let res: any = await systemRoleApi.saveRoleRouterRel(data)
        if (res.code !== 200) {
          return message(res.msg, {type: "error"})
        }
        message('操作成功',{type:'success'})
        closeDialog(options, 0)
      }
    },
    contentRenderer: () => menu
  }
  addDialog(options)
}

const assignRouterActionData = ref({})
const assignRouterActionFlag = ref(false)
const assignRouterAction = (row)=>{
  assignRouterActionData.value = row
  assignRouterActionFlag.value = true
}
const finishAssignRouterActionData = () => {
  assignRouterActionFlag.value = false
}
</script>

<style scoped lang="scss">
.parent {
  height: 100%;
}
.parent> :first-child{
  height: 100%;
}
</style>
