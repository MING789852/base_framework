<template>
  <div class="parent">
    <common-table ref="commonTableRef"  :columns="columns"
                  :queryColumns="queryColumns" :queryInHeader="false" :dictList="dictMapping"
                  :api="systemUserApi" :tableFn="tableFn" :table-button="tableButton">
      <template #button_end>
        <el-button class="font-bold" size="small" type="primary" @click="changeEnableFn(0)">禁用</el-button>
        <el-button class="font-bold" size="small" type="primary" @click="changeEnableFn(1)">启用</el-button>

        <el-button v-if="common.authActionCode('currentLoginUser')" class="font-bold" size="small" type="primary" @click="currentLoginUser">当前登录用户</el-button>
        <el-button v-if="authButton()" class="font-bold" size="small" type="primary" @click="createDDUser">创建钉钉用户（id）</el-button>
        <el-button v-if="authButton()" class="font-bold" size="small" type="primary" @click="createDDUserByName">创建钉钉用户(姓名)</el-button>
        <el-button v-if="authButton()" class="font-bold" size="small" type="primary" @click="createDDUserByWorkNum">创建钉钉用户(工号)</el-button>
        <el-button v-if="authButton()" class="font-bold" size="small" type="primary" @click="createDDUserWithLeader">创建钉钉用户(带出领导)</el-button>
      </template>
      <template #column>
        <el-table-column header-align="center" align="center" label="操作" >
          <template #default="scope">
            <a style="cursor: pointer;color: #409EFF" @click="assignRoles(scope.row)">分配角色</a>
          </template>
        </el-table-column>
      </template>
      <template #dialog>
        <el-dialog v-model="loginDetailFlag" destroy-on-close append-to-body fullscreen>
          <common-table ref="loginDetailCommonTableRef"  :columns="loginDetailColumns" height="76vh" :dictList="dictMapping" :show-page="false"
                        :queryInHeader="false" :tableFn="loginDetailTableFn" :table-button="loginDetailTableButton">
            <template #column>
              <el-table-column header-align="center" align="center" label="操作" >
                <template #default="scope">
                  <div class="flex gap-2 w-full justify-center">
                    <el-link :underline="false" type="primary" style="font-size: 12px" class="text-nowrap" @click="viewRequestAction(scope.row)">操作历史</el-link>
                    <el-link :underline="false" type="danger" style="font-size: 12px" class="text-nowrap" @click="logout(scope.row)">强制退出</el-link>
                  </div>
                </template>
              </el-table-column>
            </template>
            <template #dialog>
              <el-drawer v-model="requestActionFlag" size="50%" append-to-body>
                <template #header>
                  <div class="flex justify-between items-center">
                    <div style="font-size: 18px;font-weight: bolder;color: black">操作历史</div>
                  </div>
                </template>
                <el-table
                    style="font-size: 12px;"
                    element-loading-text="加载中..."
                    :data="requestActionData"
                    :border="false">
                  <template v-for="item in requestActionColumns" :key="item.prop">
                    <CommonTableColumn  :item="item" :dict-list="dictMapping"/>
                  </template>
                </el-table>
              </el-drawer>
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
import {isNullOrUnDef} from "@pureadmin/utils";
import {addDialog, } from "@/components/ReDialog";
import common from '@/utils/common'
import systemUserApi from "@/api/systemUserApi";
import systemRoleApi from "@/api/systemRoleApi";
import {getUserInfo} from "@/utils/auth";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import DetailForm from "@/components/detailForm/detailForm.vue";
import commonTable from "@/components/table/commonTable.vue";
import TableFnClass from "@/class/TableFnClass";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";

defineOptions({
  name: "system-user"
});

const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: true,
  saveFn: true,
  refreshFn: true,
  deleteFn: false,
  initQueryFn: false,
  exportExcelFn: false
})
let commonTableRef = ref<CommonTableType>(null)
const columns = ref<Array<ColumnDefine>>([
  {prop: "username", label: "账号", width: 150, type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "nickName", label: "姓名", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "judgeEnable", label: "是否可用", width: 100, type: ColumnTypeEnum.DICT_COMMON, query: true, queryType: QueryTypeEnum.OPTION, queryCondition: QueryConditionEnum.IN},
  {prop: "jobNumber", label: "工号", width: 250, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "userType", label: "用户类型", width: 250, type: ColumnTypeEnum.DICT_COMMON, query: true, queryType: QueryTypeEnum.OPTION, queryCondition: QueryConditionEnum.IN},
  {prop: "email", label: "邮箱", width: 250, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);
const queryColumns = ref<Array<ColumnDefine>>([
  {prop: "username", label: "账号", width: 150, type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "nickName", label: "姓名", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "judgeEnable", label: "是否可用", width: 100, type: ColumnTypeEnum.DICT_COMMON, query: true, queryType: QueryTypeEnum.OPTION, queryCondition: QueryConditionEnum.IN},
  {prop: "jobNumber", label: "工号", width: 250, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "userType", label: "用户类型", width: 250, type: ColumnTypeEnum.DICT_COMMON, query: true, queryType: QueryTypeEnum.OPTION, queryCondition: QueryConditionEnum.IN},
  {prop: "email", label: "邮箱", width: 250, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);
const dictMapping = ref({
  judgeEnable: {
    1:'可用',
    0:'禁用'
  }
})

tableFn.initFn = async () => {
  let res = await common.handleRequestApi(systemUserApi.getUserTypeMapping())
  dictMapping.value['userType'] = res.data
  //加载数据
  commonTableRef.value.getData()
}

tableFn.addFn = () => {
  addDialog({
    width: "40%",
    title: "新增",
    props: {
      columns: detailColumns,
      propData: detailData,
      dictList: dictMapping
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, { options, index }) {
      let data = [options.props.propData]
      systemUserApi.saveOrUpdateData(data).then((res:any)=>{
        if (res.code === 200) {
          message('新增成功',{type:'success'})
          commonTableRef.value.getData()
          done()
        } else {
          message(res.msg,{type:'error'})
        }
      }).catch(e => {
        message(e,{type:'error'})
      })
    }
  })
}


/**
   登录详情
 */
const loginDetailTableFn = new TableFnClass()
const loginDetailTableButton = ref<CommonTableButton>({
  queryFn: false,
  addFn: false,
  saveFn: false,
  refreshFn: true,
  deleteFn: false,
  initQueryFn: false,
  exportExcelFn: false
})
let loginDetailCommonTableRef = ref<CommonTableType>(null)
const loginDetailColumns = ref<Array<ColumnDefine>>([
  ...[{prop: "loginTime", label: "登录时间", type: ColumnTypeEnum.COMMON}],
    ...columns.value
]);
const loginDetailFlag = ref(false)

loginDetailTableFn.initFn = ()=>{
  loginDetailCommonTableRef.value.getData()
}
loginDetailTableFn.getData = ()=>{
  common.handleRequestApi(systemUserApi.getAllLoginUser()).then(res=>{
    loginDetailCommonTableRef.value.tableData = res.data
  })
}

const currentLoginUser = ()=>{
  loginDetailFlag.value = true
}

const requestActionData = ref([])
const requestActionFlag = ref(false)
const requestActionColumns = ref<Array<ColumnDefine>>([
  {prop: "url", label: "操作详情", type: ColumnTypeEnum.COMMON},
  {prop: "actionTime", label: "操作时间", type: ColumnTypeEnum.COMMON}
]);
const viewRequestAction = (row)=>{
  common.handleRequestApi(systemUserApi.getRequestAction(row)).then(res=>{
    requestActionData.value = res.data
    requestActionFlag.value = true
  })
}
const logout = (row)=>{
  common.handleRequestApi(systemUserApi.removeLoginUser([row])).then(res=>{
    message('操作成功',{type: 'success'})
    loginDetailCommonTableRef.value.getData()
  })
}

/**
 * 页面事件
 */
const assignRoles = async (row) => {
  const assignRolesData = ref({})
  const assignRolesColumns:Array<DetailColumnDefine> = [
    {prop: "nickName", label: "姓名", disabled: true, type: QueryTypeEnum.INPUT, placeholder: ''},
    {prop: "roleList", label: "角色列表", type: QueryTypeEnum.MULTIPLE_OPTION, placeholder: ''}
  ];
  let assignRolesDict = {}

  let roleListRes:any = await systemRoleApi.getRoleList()
  let roleIdListRes:any=await systemRoleApi.getRoleIdListByUser(row.id)
  if (roleListRes.code !== 200) {
    return message('获取角色列表失败',{type:'error'})
  }
  if (roleIdListRes.code !== 200) {
    return message('用户获取角色列表失败',{type:'error'})
  }

  assignRolesData.value['nickName'] = row.nickName
  assignRolesData.value['roleList'] = roleIdListRes.data

  let roleList:Array<any> = roleListRes.data
  assignRolesDict['roleList'] = {}
  if (!isNullOrUnDef(roleList)){
    roleList.forEach((item)=>{
      // assignRolesDict['roleList'][item.roleName] = item.id
      assignRolesDict['roleList'][item.id] = item.roleName
    })
  }
  addDialog({
    width: "40%",
    title: "分配角色",
    props: {
      columns: assignRolesColumns,
      propData: assignRolesData,
      dictList: assignRolesDict
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, { options, index }) {
      let propData:any = options.props.propData
      if (isNullOrUnDef(propData.roleList)){
        propData.roleList = []
      }
      let data = {
        userId: row.id,
        roleIdList: propData.roleList
      }
      systemRoleApi.saveUserRoleRel(data).then((res:any)=>{
        if (res.code === 200) {
          message('分配成功',{type:'success'})
          commonTableRef.value.getData()
          done()
        } else {
          message(res.msg,{type:'error'})
        }
      }).catch(e => {
        message(e,{type:'error'})
      })
    }
  })
}

const detailData = ref({})
const detailColumns:Array<DetailColumnDefine> = [
  {prop: "username", label: "账号", type: QueryTypeEnum.INPUT, placeholder: ''},
  {prop: "password", label: "密码", type: QueryTypeEnum.INPUT, placeholder: ''},
  {prop: "nickName", label: "姓名", type: QueryTypeEnum.INPUT, placeholder: ''},
  {prop: "jobNumber", label: "工号", type: QueryTypeEnum.INPUT, placeholder: ''},
];

const changeEnableFn = (judgeEnable) => {
  if(common.isEmptyArr(commonTableRef.value.selectTableData)){
    message('请选择需要操作的数据后操作', { type: 'warning' })
  } else {
    let data = {
      judgeEnable: judgeEnable,
      tcUserList: commonTableRef.value.selectTableData
    }
    common.handleRequestApi(systemUserApi.changeEnable(data)).then((res)=>{
      commonTableRef.value.getData()
      message('操作成功', { type: 'success' })
    })
  }
}

const ddData = ref({})
const ddColumns:Array<DetailColumnDefine> = [
  {prop: "inputValue", label: "输入值", type: QueryTypeEnum.INPUT, placeholder: ''}
];
const authButton = () => {
  let userInfo = getUserInfo()
  if (!isNullOrUnDef(userInfo)&&!isNullOrUnDef(userInfo.username)) {
    if (userInfo.username === 'admin') {
      return  true
    }
  }

  return false
}
const createDDUserByName = () => {
  addDialog({
    width: "40%",
    title: "输入钉钉用户姓名",
    props: {
      columns: ddColumns,
      propData: ddData,
      dictList: dictMapping
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, { options, index }) {
      let inputValue = options.props.propData.inputValue
      if (isNullOrUnDef(inputValue)){
        return message('输入值不能为空',{type:'error'})
      }
      systemUserApi.createTcUserByDingDingName(inputValue).then((res:any)=>{
        if (res.code === 200) {
          message('新增成功',{type:'success'})
          commonTableRef.value.getData()
          done()
        } else {
          message(res.msg,{type:'error'})
        }
      }).catch(e => {
        message(e,{type:'error'})
      })
    }
  })
}

const createDDUserByWorkNum = () => {
  addDialog({
    width: "40%",
    title: "输入钉钉用户工号",
    props: {
      columns: ddColumns,
      propData: ddData,
      dictList: dictMapping
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, { options, index }) {
      let inputValue = options.props.propData.inputValue
      if (isNullOrUnDef(inputValue)){
        return message('输入值不能为空',{type:'error'})
      }
      systemUserApi.createTcUserByDingDingWorkNum(inputValue).then((res:any)=>{
        if (res.code === 200) {
          message('新增成功',{type:'success'})
          commonTableRef.value.getData()
          done()
        } else {
          message(res.msg,{type:'error'})
        }
      }).catch(e => {
        message(e,{type:'error'})
      })
    }
  })
}
const createDDUser = () => {
  addDialog({
    width: "40%",
    title: "新增",
    props: {
      columns: ddColumns,
      propData: ddData,
      dictList: dictMapping
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, { options, index }) {
      let inputValue = options.props.propData.inputValue
      if (isNullOrUnDef(inputValue)){
        return message('输入值不能为空',{type:'error'})
      }
      systemUserApi.createTcUserByDingDingUserId(inputValue).then((res:any)=>{
        if (res.code === 200) {
          message('新增成功',{type:'success'})
          commonTableRef.value.getData()
          done()
        } else {
          message(res.msg,{type:'error'})
        }
      }).catch(e => {
        message(e,{type:'error'})
      })
    }
  })
}

const createDDUserWithLeader = () => {
  addDialog({
    width: "40%",
    title: "输入钉钉用户id",
    props: {
      columns: ddColumns,
      propData: ddData,
      dictList: dictMapping
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, { options, index }) {
      let inputValue = options.props.propData.inputValue
      if (isNullOrUnDef(inputValue)){
        return message('输入值不能为空',{type:'error'})
      }
      systemUserApi.createTcUserWithLeaderByDingDingUserId(inputValue).then((res:any)=>{
        if (res.code === 200) {
          message('新增成功',{type:'success'})
          commonTableRef.value.getData()
          done()
        } else {
          message(res.msg,{type:'error'})
        }
      }).catch(e => {
        message(e,{type:'error'})
      })
    }
  })
}

</script>

<style scoped lang="scss">
</style>
