<template>
  <div class="parent">
    <el-card shadow="never">
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

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" style="height: 73vh">
            <el-scrollbar :style="{height: height}">
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
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card v-show="currentRouter!==null" shadow="never" style="height: 73vh">
            <div class="w-full">
              <div class="w-full flex justify-start">
                <el-button size="small" type="primary" @click="addRouterAction">新增</el-button>
                <el-button size="small" type="primary" @click="refreshRouterActionTable">刷新</el-button>
                <el-button size="small" type="primary" @click="initCommonTableRouterAction">初始化通用表格操作</el-button>
              </div>
              <el-table
                  style="font-size: 12px;"
                  element-loading-text="加载中..."
                  :data="routerActionTableData"
                  :border="false">
                <template v-for="item in columns" :key="item.prop">
                  <CommonTableColumn :item="item" :dict-list="routerActionDictMapping"/>
                </template>
                <el-table-column header-align="center" align="center" label="操作" >
                  <template #default="scope">
                    <a style="cursor: pointer;color: #409EFF" @click="updateRouterAction(scope.row)">修改</a>
                    <a style="cursor: pointer;color: #409EFF;margin-left: 8px" @click="deleteRouterAction(scope.row)">删除</a>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

  </div>
</template>

<script setup lang="tsx">
import {onMounted, ref} from "vue";
import {message} from "@/utils/message";
import {addDialog} from "@/components/ReDialog/index";
import systemMenuApi from "@/api/systemMenuApi";
import DetailForm from "@/components/detailForm/detailForm.vue";
import {ElTree} from "element-plus";
import contentConfig from "@/config/content";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import showDialog from "@/utils/ConfirmDialog";
import common from "@/utils/common";
import systemRouterActionApi from "@/api/systemRouterActionApi";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
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
const height=ref(contentConfig.dataHeight)
const treeRef = ref<InstanceType<typeof ElTree>>()
const treeData = ref<Array<any>>([]);
const loading = ref(false);
const detailData = ref({})
const detailChildrenColumns:Array<DetailColumnDefine> = [
  {prop: 'parentTitle', label: '父菜单', disabled: true, type: QueryTypeEnum.INPUT},
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
  fullscreen: defaultDict
})

const columns=ref<ColumnDefine[]>([
  {label: "操作编码", prop: "actionCode", query: false, type: ColumnTypeEnum.COMMON},
  {label: "操作名称",prop: "actionName",query: false, type: ColumnTypeEnum.COMMON},
  {label: "操作类型",prop: "actionType",query: false, type: ColumnTypeEnum.DICT_COMMON}
])
const routerActionTableData = ref([])
const routerActionDictMapping = ref({
  actionType:{
    0:'按钮',
    1:'函数',
    2:'其它'
  }
})
interface RouterInterface {
  name?:string,
  path?:string,
  title?:string,
  id?:string
}
const currentRouter = ref<RouterInterface>(null)
const refreshRouterActionTable = () => {
  common.handleRequestApi(systemRouterActionApi.getRouterActionDataByRouter(currentRouter.value)).then(res=>{
    routerActionTableData.value = res.data
  })
}
const checkRouterAction = (node, data) => {
  currentRouter.value = data
  refreshRouterActionTable()
}
const initCommonTableRouterAction = () => {

}
const addOrUpdateRouterAction = (row:any) => {
  const routerActionColumns:Array<DetailColumnDefine> = [
    {label: "操作编码", prop: "actionCode", type: QueryTypeEnum.INPUT, placeholder: '必填，一个路由内唯一'},
    {label: "操作名称",prop: "actionName", type: QueryTypeEnum.INPUT, placeholder: '选填,默认空字符串'},
    {label: "操作类型",prop: "actionType", type: QueryTypeEnum.OPTION, placeholder: '选填,默认按钮'},
  ];
  let defaultValue = ref(row)
  common.openInputDialog(routerActionColumns,routerActionDictMapping,defaultValue,(result)=>{
    let data = {
      router:currentRouter.value,
      routerActionList:[result.data]
    }
    common.handleRequestApi(systemRouterActionApi.saveRouterActionData(data)).then(res=>{
      message('操作成功',{type:'success'})
      refreshRouterActionTable()
      result.done()
    })
  })
}
const addRouterAction = () => {
  addOrUpdateRouterAction({})
}
const updateRouterAction = (row:any) => {
  addOrUpdateRouterAction(row)
}
const deleteRouterAction = (row:any) => {
  common.handleRequestApi(systemRouterActionApi.deleteRouterActionData([row])).then(res=>{
    message('操作成功',{type:'success'})
    refreshRouterActionTable()
  })
}




onMounted(() => {
  //加载数据
  getData()
})
const addData = (parentRouter, level:number) => {
  const newDetailColumns = ref([])
  detailData.value = {}
  if (level === 1) {
    detailData.value = {
      parentTitle: parentRouter.title
    }
    newDetailColumns.value=detailChildrenColumns
  } else {
    newDetailColumns.value=detailParentColumns
  }
  addDialog({
    width: "40%",
    title: "新增",
    props: {
      columns: newDetailColumns,
      propData: detailData,
      dictList: dictList
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, { options, index }) {
      let data = {
        parentRouter: parentRouter,
        addRouter: options.props.propData
      }
      systemMenuApi.addRouter(data).then((res:any)=>{
        if (res.code === 200) {
          message('新增成功',{type:'success'})
          getData()
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
const removeData = (node, data) => {
  showDialog('是否删除').then((res)=>{
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
  const newDetailColumns = ref([])
  detailData.value = data
  if (node.data.level === 0) {
    newDetailColumns.value = detailParentColumns
  }else {
    if (node.parent&&node.parent.data){
      let parentRouter = node.parent.data
      detailData.value['parentTitle'] = parentRouter.title
    }
    newDetailColumns.value = detailChildrenColumns
  }
  addDialog({
    width: "40%",
    title: "新增",
    props: {
      columns: newDetailColumns,
      propData: detailData,
      dictList: dictList
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, { options, index }) {
      systemMenuApi.updateRouter(options.props.propData).then((res:any)=>{
        if (res.code === 200) {
          message('修改成功',{type:'success'})
          getData()
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
  }).catch(res => {
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
  .parent {
    height: 100%;
  }
  .parent> :first-child{
    height: 100%;
  }
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }
</style>
