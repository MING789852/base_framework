<script lang="ts">
import {defineComponent} from 'vue'
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import flowableApi from "@/api/flowableApi";
import common from "@/utils/common";
import {taskStatusDictEnum} from "@/components/flowable/enums/taskStatusEnum"
import {message} from "@/utils/message";

export default defineComponent({
  name: "flowableDetail",
  props:{
    // flag: {type: Boolean, required: true, default: false},
    processId: {type: String, required: true}
  },
  computed:{
    ColumnTypeEnum() {
      return ColumnTypeEnum
    },
    // dialogFlag:{
    //   get() {
    //     return this.flag
    //   },
    //   // setter
    //   set(newValue) {
    //     this.$emit('closeDialog',newValue)
    //   }
    // }
  },
  data(vm) {
      return {
        tableData: [],
        dictList: {
          status: taskStatusDictEnum
        },
        base64Image: '',
        columns:[
          {prop: "status", label: "状态",fixed: false, type: ColumnTypeEnum.DICT_COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
          {prop: "name", label: "审批人",fixed: false, type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
          {prop: "msg", label: "审批意见",fixed: false, type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
          {prop: "createTime", label: "开始时间",fixed: false, type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
          {prop: "actionTime", label: "审批时间",fixed: false, type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ}
        ]
      }
  },
  mounted() {
    common.showGlobalLoading()
    flowableApi.viewProcess(this.processId).then((res:HttpResult)=>{
      if (res.code === 200) {
        this.tableData = res.data.taskInfoVoList
        this.base64Image = 'data:image/png;base64,' + res.data.base64
      }else {
        message(res.msg,{type:'error'})
      }
    }).finally(()=>{
      common.closeGlobalLoading()
    })
  },
  methods: {
    dictView (item:any,scope:any){
      let data = scope.row[item.prop]
      if (item.prop === 'status'){
        if (common.isStrBlank(data)){
          return '审批中'
        }
      }
      return this.dictList[item.prop][data]
    }
  }
})
</script>

<template>
<!--  <el-dialog v-model="dialogFlag" append-to-body>-->
  <div class="w-full h-full">
    <div class="w-full flex flex-col items-center">
      <el-image  :src="base64Image"/>
    </div>
    <div style="height: 20px"/>
    <el-table
      :data="tableData"
      :stripe="true">
      <!-- 普通-->
      <template v-for="item in columns" :key="item.prop">
        <el-table-column  v-if="item.type === ColumnTypeEnum.COMMON" :fixed="item.fixed" show-overflow-tooltip header-align="center" align="center" :label="item.label">
          <template #default="scope">
            {{scope.row[item.prop]}}
          </template>
        </el-table-column>
        <el-table-column   v-else-if="item.type === ColumnTypeEnum.DICT_COMMON" show-overflow-tooltip header-align="center" align="center" :label="item.label">
          <template #default="scope">
            {{dictView(item,scope)}}
          </template>
        </el-table-column>
      </template>
    </el-table>
  </div>
<!--  </el-dialog>-->
</template>

<style scoped lang="scss">

</style>
