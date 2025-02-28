<script setup lang="ts">

import {onMounted, ref, watch} from "vue";
import { VueDraggable } from 'vue-draggable-plus'
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";
import columnConfigApi from "@/api/columnConfigApi";
import {message} from "@/utils/message";

interface ColumnConfig {
  inputDisabledList?: InputDisabled,
  //组名称
  // groupName: string,
  //是否可排序
  sortableEdit?: boolean,
  //初始化
  initFn?:()=>void,
  //标题
  title?:string,
  //初始化时是否自动从数据库读取列配置(自动加载列配置，适合列配置已固定，不适合动态列配置)
  initLoadDataBase?: boolean,
  //是否存储数据库
  saveDataBase?: boolean,
  //全局配置(全部用户共用一个配置,非一个用户一个配置)
  global?: boolean,
}
const props = withDefaults(defineProps<ColumnConfig>(), {
  inputDisabledList: ()=>({} as InputDisabled),
  sortableEdit: false,
  title: '列配置',
  saveDataBase: true,
  global: true,
  initLoadDataBase: true
})
const groupName = defineModel<string>('groupName',{required: true})
const showFlag = defineModel<boolean>('showFlag',{required: true})
const defaultColumnsDataProp = defineModel<ColumnDefine[]>("defaultColumns",{required:false})
const columnsDataProp = defineModel<ColumnDefine[]>("columns",{required:true})
const columnsData = ref([])
const emits=defineEmits(['save'])

watch(columnsDataProp,()=>{
  cloneData()
})

const cloneData=()=>{
  columnsData.value = cloneDeep(columnsDataProp.value)
}

const loadColumnsConfig = async (defaultColumns?:ColumnDefine[],groupNameParams?:string) => {
  //加载列配置
  let data = {
    defaultColumns: isNullOrUnDef(defaultColumns)?columnsDataProp.value:defaultColumns,
    groupName: isNullOrUnDef(groupNameParams)?groupName.value:groupNameParams,
    global: props.global
  }
  let configRes = await common.handleRequestApi(columnConfigApi.getColumnPropsConfig(data))
  columnsDataProp.value = configRes.data
  return configRes.data
}


onMounted(() => {
  if (!isNullOrUnDef(props.initFn)) {
    props.initFn()
  } else {
    cloneData()
    if (props.initLoadDataBase){
      loadColumnsConfig()
    }
  }
})

const reset = () => {
  common.showMsgDialog('是否还原默认列配置').then(()=>{
    let data = {
      groupName:groupName.value,
      global: props.global
    }
    common.handleRequestApi(columnConfigApi.resetColumnPropsConfig(data)).then(()=>{
      message('操作成功',{type:'success'})
      columnsDataProp.value = defaultColumnsDataProp.value
      emits('save',defaultColumnsDataProp.value)
      showFlag.value = false
    })
  })
}
const save = ()=>{
  if (props.saveDataBase){
    let cloneConfig = cloneDeep(columnsData.value)
    let data = {
      defaultColumns:cloneConfig,
      groupName:groupName.value,
      global: props.global
    }
    common.handleRequestApi(columnConfigApi.saveColumnPropsConfig(data)).then(()=>{
      message('操作成功',{type:'success'})
      columnsDataProp.value = cloneConfig
      emits('save',cloneConfig)
      showFlag.value = false
    })
  }else {
    emits('save',columnsData.value)
    showFlag.value = false
  }
}

const showChange = (value:any,row:any)=>{
  let children=row['children'] as ColumnDefine[]
  if (!isNullOrUnDef(children)){
    children.forEach((item)=>{
      item.show = value
      //递归调用子项
      showChange(value,item)
    })
  }
}
const fixedChange = (value:any,row:any) => {
  let children=row['children'] as ColumnDefine[]
  if (!isNullOrUnDef(children)){
    children.forEach((item)=>{
      item.fixed = value
      //递归调用子项
      fixedChange(value,item)
    })
  }
}

//子项列配置
const childrenColumnConfigFlag = ref(false)
const childrenColumn = ref([])
const childrenTitle = ref('')
const childrenColumnConfigRef = ref()
let saveChildrenColumnConfig: (children:ColumnDefine[]) => void = ()=>{}
const childrenColumnConfig = (row:any)=>{
  childrenTitle.value = row['label'] + '列配置'
  childrenColumn.value = row['children']
  childrenColumnConfigFlag.value = true
  saveChildrenColumnConfig = (childrenList:ColumnDefine[])=>{
    //判断子项是否有显示,有一个或一个以上的子项显示则显示父项
    let parentShow = false
    for (let i = 0;i<childrenList.length;i++){
      if (childrenList[i].show){
        parentShow = true
        break
      }
    }
    row['children'] = childrenList
    row['show'] = parentShow
  }
}

defineExpose({
  loadColumnsConfig,
  columnsData
})
</script>

<template>
  <div>
    <ColumnConfig v-if="childrenColumnConfigFlag" ref="childrenColumnConfigRef"
                  v-model:show-flag="childrenColumnConfigFlag"
                  v-model:columns="childrenColumn" :title="childrenTitle"
                  group-name="childrenList"
                  :init-load-data-base="false"
                  :save-data-base="false"
                  @save="saveChildrenColumnConfig"/>

    <el-dialog v-model="showFlag" style="font-size: 12px;" destroy-on-close :title="title" append-to-body width="800" top="5vh">
      <template #default>
        <VueDraggable v-model="columnsData" :animation="150" target="tbody">
          <el-table
              element-loading-text="加载中..."
              :data="columnsData"
              height="600"
              :border="false">
            <el-table-column header-align="center" align="center" label="列名">
              <template #default="scope">
                {{scope.row['label']}}
              </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" label="列宽" width="120">
              <template #default="scope">
                <el-input v-model="scope.row['width']"/>
              </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" label="是否展示" width="120">
              <template #default="scope">
                <el-checkbox v-model="scope.row['show']" @change="(checkboxValue)=>{showChange(checkboxValue,scope.row)}"/>
              </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" label="是否固定" width="120">
              <template #default="scope">
                <el-checkbox v-model="scope.row['fixed']" @change="(checkboxValue)=>{fixedChange(checkboxValue,scope.row)}"/>
              </template>
            </el-table-column>
            <el-table-column v-if="sortableEdit" header-align="center" align="center" label="是否可排序" width="120">
              <template #default="scope">
                <el-checkbox v-model="scope.row['sortable']"/>
              </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" label="子项" width="120">
              <template #default="scope">
                <div v-if="!isNullOrUnDef(scope.row['children'])">
                  <el-button size="small" @click="childrenColumnConfig(scope.row)">配置</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </VueDraggable>
      </template>

      <template #footer>
        <div class="dialog-footer">
          <el-button  @click="showFlag = false">取消</el-button>
          <el-button  v-if="!isNullOrUnDef(defaultColumnsDataProp)" type="warning" @click="reset">
            还原默认配置
          </el-button>
          <el-button  type="primary" @click="save">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">

</style>