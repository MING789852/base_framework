<template>
  <card-container :show-footer="false">
    <template #header>
      <div style="display: flex;justify-content: space-between">
        <el-button-group>
          <template v-if="judgeComponent">
            <el-button class="font-bold" size="small" type="primary" @click="closeFn">关闭</el-button>
            <el-button class="font-bold" size="small" type="primary" @click="checkFn">保存</el-button>
            <el-button class="font-bold" size="small" type="primary" @click="refreshFn">刷新</el-button>
          </template>
          <template v-else>
            <el-button class="font-bold" size="small" type="primary" @click="addData(null,0)">新增</el-button>
            <el-button class="font-bold" size="small" type="primary" @click="refreshFn">刷新</el-button>
          </template>
        </el-button-group>

        <div v-if="currentRouter!==null" style="display: flex;justify-content: flex-end;font-weight: bolder;color: red">
          当前查看路由: {{currentRouter.title}}
        </div>
      </div>
    </template>


    <template #default>
      <div class="w-full h-full flex gap-6">
        <div class="basis-1/2 h-full xm-card p-4">
          <flex-full-scroll-container>
            <el-scrollbar height="100%">
              <el-tree
                  ref="treeRef"
                  :data="treeData"
                  node-key="id"
                  :default-expand-all="true"
                  :show-checkbox="judgeComponent"
                  :check-strictly="judgeComponent"
                  :default-checked-keys="selectKey"
                  :highlight-current="true"
                  :props="defaultProps"
                  :filter-node-method="filterNode"
                  @check="checkNode"
              >
                <template #default="{ node, data }">
                <span class="custom-tree-node">
                  <span>{{ node.label }}</span>
                  <span v-if="!judgeComponent">
                    <a v-if="data.level===0" style="color: #409EFF" @click.stop="addData(data, 1)"> 新增 </a>
                    <a v-if="data.level===1" style="color: #409EFF" @click.stop="checkRouterAction(node, data)"> 查看</a>
                    <a style="color: #409EFF;margin-left: 8px" @click.stop="updateData(node, data)"> 修改 </a>
                    <a style="color: #409EFF;margin-left: 8px" @click.stop="removeData(node, data)"> 删除 </a>
                  </span>
                </span>
                </template>
              </el-tree>
            </el-scrollbar>
          </flex-full-scroll-container>
        </div>
        <div class="basis-1/2">

          <common-table v-show="currentRouter!==null" ref="commonTableRef" :table-type="'action'"  :columns="columns"
                        :table-button="tableButton" :table-fn="tableFn" :dict-list="dictList"
                        :show-page="false" :show-selection="false">
            <template #header_left>
              <el-button-group>
                <el-button size="small" type="primary" @click="addRouterAction">新增</el-button>
                <el-button size="small" type="primary" @click="refreshRouterActionTable">刷新</el-button>
                <el-button size="small" type="primary" @click="copyRouterAction">复制</el-button>
                <el-button size="small" type="primary" @click="pasteRouterAction">粘贴</el-button>
              </el-button-group>
            </template>

            <template #column>
              <el-table-column header-align="center" align="center" label="操作" >
                <template #default="scope">
                  <a style="cursor: pointer;color: #409EFF" @click="updateRouterAction(scope.row)">修改</a>
                  <a style="cursor: pointer;color: #409EFF;margin-left: 8px" @click="deleteRouterAction(scope.row)">删除</a>
                </template>
              </el-table-column>
            </template>
          </common-table>
        </div>
      </div>
    </template>
  </card-container>
</template>

<script setup lang="tsx">
import {onMounted, ref, useTemplateRef} from "vue";
import {message} from "@/utils/message";
import systemMenuApi from "@/api/systemMenuApi";
import {ElTree} from "element-plus";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import showDialog from "@/utils/ConfirmDialog";
import common from "@/utils/common";
import systemRouterActionApi from "@/api/systemRouterActionApi";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import CardContainer from "@/components/CardContainer/CardContainer.vue";
import FlexFullScrollContainer from "@/components/FlexFullScrollContainer/FlexFullScrollContainer.vue";
import CommonTable from "@/components/table/commonTable.vue";
import TableFnClass from "@/class/TableFnClass";
defineOptions({
  name: "system-dept"
});
const defaultProps = {
  children: 'children',
  label: 'title',
  isLeaf: 'isLeaf'
}
const props=defineProps({
  judgeComponent: {type: Boolean, default: false},
  checkList: {type: Array<any>, default: []},
  multiCheck: {type: Boolean, default: false},
  closeDialogFn: {type:Function},
  receiveFn: {type:Function}
})
const selectKey = ref(props.checkList)
const emits = defineEmits(['componentBack', 'check'])
const treeRef = ref<InstanceType<typeof ElTree>>()
const treeData = ref<Array<any>>([]);
const loading = ref(false);
const detailData = ref({})
const detailChildrenColumns:Array<DetailColumnDefine> = [
  {prop: 'parentId', label: '父菜单', type: QueryTypeEnum.OPTION},
  {prop: "name", label: "CODE", type: QueryTypeEnum.INPUT, placeholder: '必填'},
  {prop: "path", label: "URL", type: QueryTypeEnum.INPUT, placeholder: '必填'},
  {prop: "title", label: "标题", type: QueryTypeEnum.INPUT, placeholder: '必填'},
  {prop: "component", label: "组件路径", type: QueryTypeEnum.INPUT, placeholder: '必填，例如/views/system/user.vue'},
  {prop: "redirect", label: "重定向", type: QueryTypeEnum.INPUT, placeholder: '选填，重定向url'},
  {prop: "judgePublic", label: "公开", type: QueryTypeEnum.OPTION, placeholder: '选填，默认为false'},
  {prop: "showLink", label: "显示菜单", type: QueryTypeEnum.OPTION, placeholder: '选填，默认为true'},
  {prop: "keepAlive", label: "缓存组件", type: QueryTypeEnum.OPTION, placeholder: '选填，默认为true'},
  {prop: "hiddenTag", label: "隐藏标签页", type: QueryTypeEnum.OPTION, placeholder: '选填，默认为false'},
  {prop: "fullscreen", label: "全屏展示", type: QueryTypeEnum.OPTION, placeholder: '选填，默认为false'}
];
const detailParentColumns:Array<DetailColumnDefine> = [
  {prop: "rankInt", label: "排序", type: QueryTypeEnum.INPUT, placeholder: '数字,值越小越靠前'},
  {prop: "icon", label: "图标", type: QueryTypeEnum.INPUT, placeholder: '选填,默认ri:information-line'},
  {prop: "name", label: "CODE", type: QueryTypeEnum.INPUT, placeholder: '必填'},
  {prop: "path", label: "URL", type: QueryTypeEnum.INPUT, placeholder: '必填'},
  {prop: "title", label: "标题", type: QueryTypeEnum.INPUT, placeholder: '必填'}
];
const defaultDict = {
  true: 'true',
  false: 'false'
}
const dictList = ref({
  showLink: defaultDict,
  keepAlive: defaultDict,
  hiddenTag: defaultDict,
  judgePublic: defaultDict,
  fullscreen: defaultDict,
  actionType:{
    0:'按钮',
    1:'函数',
    2:'其它'
  }
})

const columns=ref<ColumnDefine[]>([
  {label: "操作编码", prop: "actionCode", query: false, type: ColumnTypeEnum.COMMON},
  {label: "操作名称",prop: "actionName",query: false, type: ColumnTypeEnum.COMMON},
  {label: "操作类型",prop: "actionType",query: false, type: ColumnTypeEnum.DICT_COMMON}
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
tableFn.initFn = () => {}
const commonTableRef = useTemplateRef<InstanceType<typeof CommonTable>>('commonTableRef')

interface RouterInterface {
  name?:string,
  path?:string,
  title?:string,
  id?:string
}
const currentRouter = ref<RouterInterface>(null)
onMounted(() => {
  //加载数据
  getData()
})
const refreshRouterActionTable = () => {
  common.handleRequestApi(systemRouterActionApi.getRouterActionDataByRouter(currentRouter.value)).then(res=>{
    commonTableRef.value.tableData = res.data
  })
}
const checkRouterAction = (node, data) => {
  currentRouter.value = data
  refreshRouterActionTable()
}

interface RouterAction{
  actionCode:string,
  actionName:string
  actionType:string
}

const copyRouterAction = () => {
  let list:any[] = commonTableRef.value.tableData
  let copyList:RouterAction[] = []
  if (!common.isEmptyArr(list)){
    list.forEach((item)=>{
      copyList.push({
        actionCode: item.actionCode,
        actionName: item.actionName,
        actionType: item.actionType
      })
    })
  }
  common.copyText(JSON.stringify(copyList))
  message('已复制到粘贴板',{type:'success'})
}
const pasteRouterAction = () => {
  let columns: Array<DetailColumnDefine> = [
    {prop: 'json', label: 'JSON配置',type: QueryTypeEnum.AREA_INPUT,placeholder:'此处粘贴'},
  ]
  let params:OpenInputDialogDefine = {
    columns: columns,
    callBack: (result) => {
      let json = result.data.json
      if (common.isStrBlank(json)){
        message('请输入JSON配置', { type: 'error' })
        return
      }
      let dataList:RouterAction[] = JSON.parse(json)
      requestRouterActionApi(dataList,()=>{
        result.done()
      })
    }
  }
  common.openInputDialog(params)
}
const requestRouterActionApi = (dataList:any[],callBack:()=>void) => {
  let data = {
    router:currentRouter.value,
    routerActionList:dataList
  }
  common.handleRequestApi(systemRouterActionApi.saveRouterActionData(data)).then(()=>{
    message('操作成功',{type:'success'})
    refreshRouterActionTable()
    callBack?.()
  })
}
const addOrUpdateRouterAction = (row:any) => {
  const routerActionColumns:Array<DetailColumnDefine> = [
    {label: "操作编码", prop: "actionCode", type: QueryTypeEnum.INPUT, placeholder: '必填，一个路由内唯一'},
    {label: "操作名称",prop: "actionName", type: QueryTypeEnum.INPUT, placeholder: '选填,默认空字符串'},
    {label: "操作类型",prop: "actionType", type: QueryTypeEnum.OPTION, placeholder: '选填,默认按钮'},
  ];
  let defaultValue = ref(row)
  let params:OpenInputDialogDefine = {
    columns: routerActionColumns,
    dictMapping: dictList,
    defaultValue:defaultValue,
    callBack: (result) => {
      requestRouterActionApi([result.data],()=>{
        result.done()
      })
    }
  }
  common.openInputDialog(params)
}
const addRouterAction = () => {
  addOrUpdateRouterAction({})
}
const updateRouterAction = (row:any) => {
  addOrUpdateRouterAction(row)
}
const deleteRouterAction = (row:any) => {
  common.handleRequestApi(systemRouterActionApi.deleteRouterActionData([row])).then(()=>{
    message('操作成功',{type:'success'})
    refreshRouterActionTable()
  })
}
const addData = (parentRouter, level:number) => {
  let newDetailColumns:Array<DetailColumnDefine>
  detailData.value = {}
  if (level === 1) {
    detailData.value = {
      parentId: parentRouter.title
    }
    newDetailColumns=detailChildrenColumns
  } else {
    newDetailColumns=detailParentColumns
  }
  let params:OpenInputDialogDefine = {
    columns: newDetailColumns,
    dictMapping: dictList,
    defaultValue:detailData,
    callBack: (result) => {
      let data = {
        parentRouter: parentRouter,
        addRouter: result.data
      }
      systemMenuApi.addRouter(data).then((res:any)=>{
        if (res.code === 200) {
          message('新增成功',{type:'success'})
          getData()
          result.done()
        } else {
          message(res.msg,{type:'error'})
        }
      }).catch(e => {
        message(e,{type:'error'})
      })
    }
  }
  common.openInputDialog(params)
}
const removeData = (node, data) => {
  showDialog('是否删除').then(()=>{
    systemMenuApi.deleteRouter([data]).then((res:any)=>{
      if (res.code === 200) {
        message('操作成功',{type:'success'})
        getData()
      } else {
        message(res.msg,{type:'error'})
      }
    }).catch(e => {
      message(e,{type:'error'})
    })
  })
}
const updateData = (node, data) => {
  let newDetailColumns:Array<DetailColumnDefine>
  detailData.value = data
  if (node.data.level === 0) {
    newDetailColumns = detailParentColumns
  }else {
    if (node.parent&&node.parent.data){
      let parentRouter = node.parent.data
      detailData.value['parentId'] = parentRouter.id
    }
    let parentIdMapping = {}
    treeData.value.forEach(item=>parentIdMapping[item.id]=item.title)
    dictList.value = {...dictList.value,...{parentId:parentIdMapping}}
    newDetailColumns = detailChildrenColumns
  }
  let params:OpenInputDialogDefine = {
    columns: newDetailColumns,
    dictMapping: dictList,
    defaultValue:detailData,
    callBack: (result) => {
      systemMenuApi.updateRouter(result.data).then((res:any)=>{
        if (res.code === 200) {
          message('修改成功',{type:'success'})
          getData()
          result.done()
        } else {
          message(res.msg,{type:'error'})
        }
      }).catch(e => {
        message(e,{type:'error'})
      })
    }
  }
  common.openInputDialog(params)
}
const getData = () => {
  loading.value = true
  let dataPromise;
  if (props.judgeComponent){
    dataPromise = systemMenuApi.getPrivateRouterList()
  }else {
    dataPromise = systemMenuApi.getRouterList()
  }
  dataPromise.then((res:any) => {
    if (res.code === 200) {
      treeData.value = res.data
    } else  {
      message(res.msg,{type:'error'})
    }
  }).catch(() => {
    message('请求失败',{type:'error'})
  }).finally(() => {
    loading.value = false
  })
}
const filterNode = (value: string, data: any) => {
  let reg = new RegExp('.*'+value+'.*','i')
  return reg.test(data.name)
}
const refreshFn = () => {
  getData()
}
const closeFn = () => {
  props.closeDialogFn()
}
const checkFn = () => {
  let data = treeRef.value.getCheckedNodes(false)
  if (props.multiCheck===false){
    if (data.length !== 1) {
      return message('请选中一条数据')
    }
  }
  props.receiveFn(data)
}

function difference(arr1:any[], arr2:any[]) {
  return arr1.filter(item => !arr2.includes(item));
}
const checkNode = (node,status) => {
  let currentCheckedKeys:string[] = status.checkedKeys
  let currentNodeChecked=currentCheckedKeys.includes(node.id)
  let relationKey:string[]=[]
  //设置自己
  relationKey.push(node.id)
  //设置关联父项
  let parentId = node.parentId
  if (parentId){
    let parentNode=treeRef.value.getNode(parentId)
    if (parentNode.data){
      if (currentNodeChecked){
        //子节点被选中，父节点一定要被选中
        relationKey.push(parentNode.data.id)
      }else {
        //子节点被取消选中，取消父节点选中之前要判断父节点是否还存在已选中的子节点
        let parentChildren:any[] = parentNode.data.children
        if (common.isEmptyArr(parentChildren)){
          relationKey.push(parentNode.data.id)
        }
        let parentChildrenChecked=parentChildren.filter((item)=>currentCheckedKeys.includes(item.id))
        if (common.isEmptyArr(parentChildrenChecked)){
          relationKey.push(parentNode.data.id)
        }
      }
    }
  }
  //设置关联子项
  let children:any[] = node.children
  if (!common.isEmptyArr(children)){
    children.forEach((item)=>{
      relationKey.push(item.id)
    })
  }
  if (currentNodeChecked){
    treeRef.value.setCheckedKeys(Array.from(new Set([...relationKey,...currentCheckedKeys])))
  }else {
    let unRemoveKey = difference(currentCheckedKeys,relationKey)
    treeRef.value.setCheckedKeys(unRemoveKey)
  }
}
</script>

<style scoped lang="scss">
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
</style>
