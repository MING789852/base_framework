<template>
    <card-container :show-footer="false">
      <template #header>
        <div style="display: flex;justify-content: space-between">
          <el-button-group>
            <el-button v-if="judgeComponent" class="font-bold" size="small" type="primary" @click="componentBack">返回</el-button>
            <el-button v-if="judgeComponent" class="font-bold" size="small" type="primary" style="font-weight: bold" @click="check">选中</el-button>
            <el-button class="font-bold" size="small" type="primary" @click="queryFn">查询</el-button>
            <el-button class="font-bold" size="small" type="primary" @click="refreshFn">刷新</el-button>
            <el-button class="font-bold" size="small" type="primary" @click="initFn">同步数据</el-button>
          </el-button-group>

          <div style="display: flex;justify-content: flex-end;font-weight: bolder;color: red">
            当前查看部门: {{currentDeptName}}
          </div>
        </div>
      </template>
      <div class="w-full h-full flex gap-6">
        <div class="basis-1/2 h-full xm-card p-4">
          <flex-full-scroll-container>
            <el-scrollbar height="100%">
              <el-tree
                  ref="treeRef"
                  :data="treeData"
                  check-strictly
                  :show-checkbox="judgeComponent"
                  node-key="id"
                  :default-expanded-keys="expandedData"
                  :default-checked-keys="selectData"
                  :highlight-current="true"
                  :props="defaultProps"
                  :filter-node-method="filterNode"
              >
                <template #default="{ node, data }">
                  <div style="display: flex;justify-content: space-between;width: 100%">
                    <div>{{ node.label }}</div>
                    <div v-if="!judgeComponent">
                      <a v-if="data.level!==1" style="color: #409EFF" @click.stop="checkDept(node, data)"> 查看</a>
                      <a style="color: #409EFF;margin-left: 3px" @click.stop="syncUser(node, data)"> 同步人员 </a>
                      <a style="color: #409EFF;margin-left: 3px" @click.stop="assignRoleByDept(node, data)"> 分配角色 </a>
                    </div>
                  </div>
                </template>
              </el-tree>
            </el-scrollbar>
          </flex-full-scroll-container>
        </div>
        <div class="basis-1/2">
          <common-table ref="commonTableRef" :table-type="'action'" :api="systemDeptApi" :columns="columns"
                        :table-button="tableButton" :table-fn="tableFn" :show-header="false"
                        :show-page="false" :show-selection="false">
            <template #column>
              <el-table-column header-align="center" align="center" label="操作" >
                <template #default="scope">
                  <a style="cursor: pointer;color: #409EFF" @click="assignRoleByUser(scope.row)">分配角色</a>
                </template>
              </el-table-column>
            </template>
          </common-table>
        </div>
      </div>
    </card-container>
</template>

<script setup lang="tsx">
import {onMounted, ref} from "vue";
import {message} from "@/utils/message";
import {addDialog, closeDialog,} from "@/components/ReDialog/index";
import systemDeptApi from "@/api/systemDeptApi";
import SearchForm from "@/components/searchForm/searchForm.vue";
import {ElTree} from "element-plus";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import common from "@/utils/common";
import CommonTable from "@/components/table/commonTable.vue";
import TableFnClass from "@/class/TableFnClass";
import systemRoleApi from "@/api/systemRoleApi";
import {isNullOrUnDef} from "@pureadmin/utils";
import CardContainer from "@/components/CardContainer/CardContainer.vue";
import FlexFullScrollContainer from "@/components/FlexFullScrollContainer/FlexFullScrollContainer.vue";


const treeRef = ref<InstanceType<typeof ElTree>>()
const commonTableRef = ref()
const defaultProps = {
  children: 'children',
  label: 'name',
  isLeaf: 'isLeaf'
}

defineOptions({
  name: "system-dept"
});

const props=defineProps({
  judgeComponent: {type: Boolean, default: false},
  checkList: {type: Array<any>, default: []},
  multiCheck: {type: Boolean, default: false}
})
const emits = defineEmits(['componentBack', 'check'])
const selectData = ref([])
const expandedData = ref([])
const judgeComponent = ref(props.judgeComponent)

onMounted(() => {
  if (judgeComponent.value === true){
    expandedData.value = ['1',...props.checkList]
    selectData.value = props.checkList
  } else {
    expandedData.value = ['1']
    selectData.value = []
  }
  //加载数据
  getData()
})

const columns = ref<Array<ColumnDefine>>([
  {fixed: true, label: "用户", prop: "userName", query: false, type: ColumnTypeEnum.COMMON},
  {fixed: true, label: "所属部门", prop: "deptName", query: false, type: ColumnTypeEnum.COMMON}
])
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: false,
  addFn: false,
  saveFn: false,
  deleteFn: false,
  refreshFn: false,
  initQueryFn: false,
  exportExcelFn: false
})
const treeData = ref<Array<any>>([]);
const query = ref<QueryDefine>({
  current: 1,
  size:10,
  total: 0,
  queryParams:{}
})
tableFn.initFn = () => {

}
const checkDeptByDeptId = (deptId) => {
  common.handleRequestApi(systemDeptApi.findUserAndDeptRefByDeptId(deptId)).then((res:HttpResult)=>{
    commonTableRef.value.tableData = res.data
  })
}
const currentDeptName = ref('')
const checkDept = (node, data) => {
  currentDeptName.value = data.name
  checkDeptByDeptId(data.id)
}
const syncUser = (node, data) => {
  common.handleRequestApi(systemDeptApi.createTcUserWithDeptId([data.id])).then(()=>{
    checkDeptByDeptId(data.id)
    message('操作成功',{type:'success'})
  })
}
const assignRoleByUser = async (row) => {
  const assignRolesData = ref({})
  const assignRolesColumns:Array<DetailColumnDefine> = [
    {prop: "nickName", label: "姓名", disabled: true, type: QueryTypeEnum.INPUT, placeholder: ''},
    {prop: "roleList", label: "角色列表", type: QueryTypeEnum.MULTIPLE_OPTION, placeholder: ''}
  ];
  let assignRolesDict = {}

  let roleListRes: any = await systemRoleApi.getRoleList()
  let roleIdListRes: any = await systemRoleApi.getRoleIdListByUser(row.userId)
  if (roleListRes.code !== 200) {
    return message('获取角色列表失败', {type: 'error'})
  }
  if (roleIdListRes.code !== 200) {
    return message('用户获取角色列表失败', {type: 'error'})
  }

  assignRolesData.value['nickName'] = row.userName
  assignRolesData.value['roleList'] = roleIdListRes.data

  let roleList: Array<any> = roleListRes.data
  assignRolesDict['roleList'] = {}
  if (!isNullOrUnDef(roleList)) {
    roleList.forEach((item) => {
      assignRolesDict['roleList'][item.id] = item.roleName
    })
  }
  let params:OpenInputDialogDefine = {
    columns: assignRolesColumns,
    dictMapping:assignRolesDict,
    defaultValue:assignRolesData,
    callBack: (result) => {
      let propData: any = result.data
      if (isNullOrUnDef(propData.roleList)) {
        propData.roleList = []
      }
      let data = {
        userId: row.userId,
        roleIdList: propData.roleList
      }
      systemRoleApi.saveUserRoleRel(data).then((res: any) => {
        if (res.code === 200) {
          message('分配成功', {type: 'success'})
          getData()
          result.done()
        } else {
          message(res.msg, {type: 'error'})
        }
      }).catch(e => {
        message(e, {type: 'error'})
      })
    }
  }
  common.openInputDialog(params)
}
const assignRoleByDept = async (node, data) => {
  const assignRoleByDeptData = ref({})
  const assignRoleByDeptColumns:Array<DetailColumnDefine> = [
    {prop: "deptName", label: "部门名称", disabled: true, type: QueryTypeEnum.INPUT, placeholder: ''},
    {prop: "roleList", label: "角色列表", type: QueryTypeEnum.MULTIPLE_OPTION, placeholder: ''}
  ];
  let assignRoleByDeptDict = {}

  let roleListRes: any = await systemRoleApi.getRoleList()
  if (roleListRes.code !== 200) {
    return message('获取角色列表失败', {type: 'error'})
  }
  assignRoleByDeptData.value['deptName'] = data.name

  let roleList: Array<any> = roleListRes.data
  assignRoleByDeptDict['roleList'] = {}
  if (!isNullOrUnDef(roleList)) {
    roleList.forEach((item) => {
      assignRoleByDeptDict['roleList'][item.id] = item.roleName
    })
  }
  let params:OpenInputDialogDefine = {
    columns: assignRoleByDeptColumns,
    dictMapping:assignRoleByDeptDict,
    defaultValue:assignRoleByDeptData,
    callBack: (result) => {
      let propData: any = result.data
      if (isNullOrUnDef(propData.roleList)) {
        propData.roleList = []
      }
      common.handleRequestApi(systemDeptApi.allocateRoleToDept(data.id,propData.roleList)).then(()=>{
        message('操作成功',{type:'success'})
        result.done()
      })
    }
  }
  common.openInputDialog(params)
}

const getData = () => {
  common.showGlobalLoading()
  systemDeptApi.selectByList(query.value).then((res:any) => {
    let code = res.code
    let data = res.data
    let msg  = res.msg
    if (code === 200) {
      treeData.value = data
    } else  {
      message(msg,{type:'error'})
    }
  }).catch(res => {
    console.log(res)
    message('请求失败',{type:'error'})
  }).finally(() => {
    common.closeGlobalLoading()
  })
}

const filterNode = (value: string, data: any) => {
  let reg = new RegExp('.*'+value+'.*','i')
  return reg.test(data.name)
}

const check = () => {
  let data = treeRef.value.getCheckedNodes(false)
  if (data.length===0){
    return message('请选中数据后操作')
  }
  if (props.multiCheck===false){
    if (data.length !== 1) {
      return message('请选中一条数据')
    }
  }
  emits('check',data)
}

const componentBack = () => {
  emits('componentBack')
}

const queryFn = () => {
  const queryColumns = ref<Array<ColumnDefine>>([
    {prop: "name", label: "部门名称",headerAlign:'left',align: 'left', type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
  ]);
  addDialog({
    width: "40%",
    title: "查询",
    props: {
      columns: queryColumns,
      query: query
    },
    contentRenderer: () => SearchForm,
    footerButtons: [
      {
        label: "取消",
        text: true,
        bg: true,
        type: "info",
        btnClick: ({dialog: {options, index}, button}) => {
          closeDialog(options, index, {command: 'cancel'});
        }
      },
      {
        label: "清空",
        text: true,
        bg: true,
        type: "info",
        btnClick: ({dialog: {options, index}, button}) => {
          query.value.queryParams = {}
        }
      },
      {
        label: "确认",
        text: true,
        bg: true,
        type: "primary",
        btnClick: ({dialog: {options, index}, button}) => {
          closeDialog(options, index, {command: 'sure'});
        }
      }
    ],
    closeCallBack: ({ options, args }) => {
      query.value = options.props.query as QueryDefine
      if (args?.command === "cancel") {
        // 您点击了取消按钮
        message(`取消查询`);
      } else if (args?.command === "sure") {
        let searchKeyList = Object.keys(query.value.queryParams)
        if (searchKeyList.length>0){
          let searchText=query.value.queryParams[searchKeyList[0]]
          treeRef.value.filter(searchText)
        }else {
          getData()
        }
      } else {
        message(
          `关闭查询窗口`
        );
      }
    }
  });
}

const refreshFn = () => {
  getData()
}

const initFn = () => {
  common.showGlobalLoading()
  systemDeptApi.initDept().then((res:HttpResult)=>{
    if (res.code===200){
      message('操作成功',{type:'success'})
      refreshFn()
    }else {
      message(res.msg,{type:'error'})
    }
  }).finally(()=>{
    common.closeGlobalLoading();
  })
}
</script>

<style scoped lang="scss">
</style>
