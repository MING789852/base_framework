<script setup lang="ts">

import {ElTree} from "element-plus";
import {onMounted, ref} from "vue";
import common from "@/utils/common";
import systemRouterActionApi from "@/api/systemRouterActionApi";
import {message} from "@/utils/message";
import systemMenuApi from "@/api/systemMenuApi";
import CardContainer from "@/components/CardContainer/CardContainer.vue";
import FlexFullScrollContainer from "@/components/FlexFullScrollContainer/FlexFullScrollContainer.vue";
const defaultProps = {
  children: 'children',
  label: 'title',
  isLeaf: 'isLeaf'
}
interface Role {
  roleCode?:string,
  roleName?:string
}
const pros = defineProps({
  role:{type:Object as PropType<Role>,required:true}
})
const emits = defineEmits(['finish'])
const treeData = ref<Array<any>>([]);
const treeRef = ref<InstanceType<typeof ElTree>>()
const selectKey = ref([])


onMounted(()=>{
  common.handleRequestApi(systemMenuApi.getRouterWithAction(pros.role)).then(routerWithAction=>{
    treeData.value = routerWithAction.data
    common.handleRequestApi(systemRouterActionApi.getRouterActionRefByRole(pros.role)).then(routerActionRef=>{
      selectKey.value = routerActionRef.data
    })
  })
})
const filterNode = (value: string, data: any) => {
  let reg = new RegExp('.*'+value+'.*','i')
  return reg.test(data.name)
}
const difference = (arr1:any[], arr2:any[])=>{
  return arr1.filter(item => !arr2.includes(item));
}
const checkNode = (node,status) => {
  let currentCheckedKeys:string[] = status.checkedKeys
  let currentNodeChecked=currentCheckedKeys.includes(node.id)
  let relationKey:string[]=[]
  //设置自己
  relationKey.push(node.id)
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
const saveRef = () => {
  let data = {
    role: pros.role,
    routerActionIdList:treeRef.value.getCheckedKeys()
  }
  common.handleRequestApi(systemRouterActionApi.saveRouterActionRef(data)).then(res=>{
    message('操作成功',{type:'success'})
    emits('finish')
  })
}
</script>

<template>
  <div class="parent">
    <card-container :show-footer="false">
      <template #header>
        <div style="display: flex;justify-content: space-between">
          <el-button-group>
            <el-button class="font-bold" size="small" type="primary" @click="saveRef()">保存</el-button>
          </el-button-group>

          <div v-if="role!==null" style="display: flex;justify-content: flex-end;font-weight: bolder;color: red">
            分配角色: {{role.roleName + '('+role.roleCode+')'}}
          </div>
        </div>
      </template>
      <template #default>
        <flex-full-scroll-container>
          <el-scrollbar height="100%">
            <el-tree
                ref="treeRef"
                :data="treeData"
                node-key="id"
                :default-expand-all="true"
                :show-checkbox="true"
                :check-strictly="true"
                :default-checked-keys="selectKey"
                :highlight-current="true"
                :props="defaultProps"
                :filter-node-method="filterNode"
                @check="checkNode"
            />
          </el-scrollbar>
        </flex-full-scroll-container>
      </template>
    </card-container>
  </div>
</template>

<style scoped lang="scss">
.parent {
  height: 100%;
}
.parent> :first-child{
  height: 100%;
}
</style>