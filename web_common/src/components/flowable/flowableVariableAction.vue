<script setup lang="ts">
import IconifyIconOnline from "@/components/ReIcon/src/iconifyIconOnline";
import {variableDataTypeDict} from "@/enums/TaskStatusEnum";

interface Prop{
  canAdd?:boolean,
  canDelete?:boolean
}
const prop=withDefaults(defineProps<Prop>(),{
  canAdd:()=>true,
  canDelete:()=>true
})

const variableDataList = defineModel<VariableDataProp[]>('variableDataList')
const addVariableDataFn = () => {
  variableDataList.value.push( {
    key: "",
    type: 'String',
    value: ''
  })
}
const deleteVariableDataFn = (index:number) => {
  variableDataList.value.splice(index,1)
}
</script>

<template>
  <div class="w-full h-full flex flex-col justify-start gap-2">
    <el-button-group>
      <el-button v-if="canAdd"  style="font-weight: bold" @click="addVariableDataFn()">新增</el-button>
    </el-button-group>
    <el-table
        :data="variableDataList"
        border>
      <el-table-column v-if="canDelete" header-align="center" align="center" label="操作" width="100">
        <template #default="dictScope">
          <div class="w-full h-full flex justify-center">
            <iconify-icon-online icon="ri:delete-bin-5-fill"
                                 class="text-red-600 cursor-pointer w-[20px] h-[20px]"
                                 @click="deleteVariableDataFn(dictScope.$index)"/>
          </div>
        </template>
      </el-table-column>
      <el-table-column  header-align="center" align="center" label="type">
        <template #default="dictScope">
          <el-select v-model="dictScope.row['type']" placeholder="请选择">
            <el-option
                v-for="(value,key) in variableDataTypeDict"
                :key="key"
                :label="key"
                :value="value">
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column  header-align="center" align="center" label="key">
        <template #default="dictScope">
          <el-input v-model="dictScope.row['key']"/>
        </template>
      </el-table-column>
      <el-table-column  header-align="center" align="left" label="value">
        <template #default="dictScope">
          <el-input v-model="dictScope.row['value']"/>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped lang="scss">

</style>