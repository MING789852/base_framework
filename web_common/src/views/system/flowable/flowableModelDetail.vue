<script setup lang="ts">
import LogicFlow from "@logicflow/core";
import "@logicflow/core/lib/style/index.css";
import '@logicflow/extension/lib/style/index.css'
import './style.css'
import {computed, nextTick, onMounted, ref} from "vue";
import {BPMNElements ,Control,SelectionSelect,DndPanel,Menu} from "@logicflow/extension";
import Dagre from './Dagre'
import {ShapeItem} from "@logicflow/extension/src/components/dnd-panel";
import {MenuConfig} from "@logicflow/extension/src/components/menu";
import flowableApi from "@/api/flowableApi";
import {isNullOrUnDef} from "@pureadmin/utils";
import {message} from "@/utils/message";
import common from "@/utils/common";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
LogicFlow.use(BPMNElements);
LogicFlow.use(DndPanel);
LogicFlow.use(SelectionSelect);
LogicFlow.use(Control);
LogicFlow.use(Menu);
LogicFlow.use(Dagre);

defineOptions({
  name: 'flowable-model-detail'
})
const height = ref((document.body.clientHeight - 160)+'px')
const distList = ref({})
const detailData=defineModel('detailData',{type:Object,required:true})
const detailFlag=defineModel('detailFlag',{type:Boolean,required:true})
const actionType = defineModel('actionType',{type:String,required:true})
const emits = defineEmits(['finish'])
interface UserTaskEditProp {
  userIdMapping: string,
  category: string,
  textValue: string
}

let lf:LogicFlow=null
onMounted(async ()=>{
  let res = await flowableApi.getDictList()
  if (res.code !== 200) {
    return message('获取数据字典失败',{type:'error'})
  }
  distList.value = res.data
  nextTick(()=>{
    lf = new LogicFlow({
      container: document.querySelector("#flowableModel"),
      snapline: true,
      grid: true
    });
    lf.setTheme({
      snapline: {
        stroke: '#1E90FF', // 对齐线颜色
        strokeWidth: 3, // 对齐线宽度
      },
    })
    //右键菜单
    const menu=lf.extension.menu as Menu
    const menuConfig:MenuConfig = {
      nodeMenu: [
        {
          callback(element: any): void {
            lf.deleteNode(element.id);
          }, className: "", icon: false, text: "删除"
        }
      ]
    }
    menu.setMenuConfig(menuConfig)
    menu.setMenuByType({
      type: "bpmn:userTask",
      menu: [
        {
          callback(element: any): void {
            lf.deleteNode(element.id);
          }, className: "", icon: false, text: "删除"
        },
        {
          callback(element: any) {
            if (isNullOrUnDef(element.properties)){
              element.properties={}
            }
            if (isNullOrUnDef(element.text)){
              element.text = {}
            }
            let columns: Array<DetailColumnDefine> = [
              {prop: 'userIdMapping', label: '人员选择',type: QueryTypeEnum.OPTION, disabled: false},
              {prop: 'category', label: '节点分类',type: QueryTypeEnum.INPUT, disabled: false},
              {prop: 'textValue', label: '节点名称',type: QueryTypeEnum.INPUT, disabled: false}
            ]
            let defaultData = ref<UserTaskEditProp>({
              userIdMapping:element.properties.assignee,
              category: element.properties.category,
              textValue: element.text.value
            })
            common.openInputDialog(columns,distList,defaultData,(result)=>{
              let data:UserTaskEditProp = result.data
              if (!isNullOrUnDef(data.textValue)){
                element.text.value = data.textValue
              }
              if (!isNullOrUnDef(data.category)){
                element.properties.category = data.category
              }
              if (!isNullOrUnDef(data.userIdMapping!=null)){
                element.properties.assignee = data.userIdMapping
                element.properties.assigneeName = distList.value['userIdMapping'][data.userIdMapping]
              }
              lf.setProperties(element.id,element.properties)
              lf.updateText(element.id,element.text.value)
              result.done()
            })
          },className: "", icon: false,text: "节点配置"
        },
      ],
    });
    //拖拽面板
    const dndPanel = lf.extension.dndPanel as DndPanel
    const shapeList:ShapeItem[] = [
      {
        label: '选区',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAAH6ji2bAAAABGdBTUEAALGPC/xhBQAAAOVJREFUOBGtVMENwzAIjKP++2026ETdpv10iy7WFbqFyyW6GBywLCv5gI+Dw2Bluj1znuSjhb99Gkn6QILDY2imo60p8nsnc9bEo3+QJ+AKHfMdZHnl78wyTnyHZD53Zzx73MRSgYvnqgCUHj6gwdck7Zsp1VOrz0Uz8NbKunzAW+Gu4fYW28bUYutYlzSa7B84Fh7d1kjLwhcSdYAYrdkMQVpsBr5XgDGuXwQfQr0y9zwLda+DUYXLaGKdd2ZTtvbolaO87pdo24hP7ov16N0zArH1ur3iwJpXxm+v7oAJNR4JEP8DoAuSFEkYH7cAAAAASUVORK5CYII=',
        callback: () => {
          const selectionSelect = lf.extension.selectionSelect as SelectionSelect
          selectionSelect.openSelectionSelect();
          lf.once('selection:selected', () => {
            selectionSelect.closeSelectionSelect();
          });
        }
      },
      {
        type: 'bpmn:startEvent',
        text: '开始',
        label: '开始节点',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAAH6ji2bAAAABGdBTUEAALGPC/xhBQAAAnBJREFUOBGdVL1rU1EcPfdGBddmaZLiEhdx1MHZQXApraCzQ7GKLgoRBxMfcRELuihWKcXFRcEWF8HBf0DdDCKYRZpnl7p0svLe9Zzbd29eQhTbC8nv+9zf130AT63jvooOGS8Vf9Nt5zxba7sXQwODfkWpkbjTQfCGUd9gIp3uuPP8bZ946g56dYQvnBg+b1HB8VIQmMFrazKcKSvFW2dQTxJnJdQ77urmXWOMBCmXM2Rke4S7UAW+/8ywwFoewmBps2tu7mbTdp8VMOkIRAkKfrVawalJTtIliclFbaOBqa0M2xImHeVIfd/nKAfVq/LGnPss5Kh00VEdSzfwnBXPUpmykNss4lUI9C1ga+8PNrBD5YeqRY2Zz8PhjooIbfJXjowvQJBqkmEkVnktWhwu2SM7SMx7Cj0N9IC0oQXRo8xwAGzQms+xrB/nNSUWVveI48ayrFGyC2+E2C+aWrZHXvOuz+CiV6iycWe1Rd1Q6+QUG07nb5SbPrL4426d+9E1axKjY3AoRrlEeSQo2Eu0T6BWAAr6COhTcWjRaYfKG5csnvytvUr/WY4rrPMB53Uo7jZRjXaG6/CFfNMaXEu75nG47X+oepU7PKJvvzGDY1YLSKHJrK7vFUwXKkaxwhCW3u+sDFMVrIju54RYYbFKpALZAo7sB6wcKyyrd+aBMryMT2gPyD6GsQoRFkGHr14TthZni9ck0z+Pnmee460mHXbRAypKNy3nuMdrWgVKj8YVV8E7PSzp1BZ9SJnJAsXdryw/h5ctboUVi4AFiCd+lQaYMw5z3LGTBKjLQOeUF35k89f58Vv/tGh+l+PE/wG0rgfIUbZK5AAAAABJRU5ErkJggg==',
      },
      {
        type: 'bpmn:userTask',
        label: '用户任务',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAATCAYAAAEFVwZaAAAABGdBTUEAALGPC/xhBQAAAqlJREFUOBF9VM9rE0EUfrMJNUKLihGbpLGtaCOIR8VjQMGDePCgCCIiCNqzCAp2MyYUCXhUtF5E0D+g1t48qAd7CCLqQUQKEWkStcEfVGlLdp/fm3aW2QQdyLzf33zz5m2IsAZ9XhDpyaaIZkTS4ASzK41TFao88GuJ3hsr2pAbipHxuSYyKRugagICGANkfFnNh3HeE2N0b3nN2cgnpcictw5veJIzxmDamSlxxQZicq/mflxhbaH8BLRbuRwNtZp0JAhoplVRUdzmCe/vO27wFuuA3S5qXruGdboy5/PRGFsbFGKo/haRtQHIrM83bVeTrOgNhZReWaYGnE4aUQgTJNvijJFF4jQ8BxJE5xfKatZWmZcTQ+BVgh7s8SgPlCkcec4mGTmieTP4xd7PcpIEg1TX6gdeLW8rTVMVLVvb7ctXoH0Cydl2QOPJBG21STE5OsnbweVYzAnD3A7PVILuY0yiiyDwSm2g441r6rMSgp6iK42yqroI2QoXeJVeA+YeZSa47gZdXaZWQKTrG93rukk/l2Al6Kzh5AZEl7dDQy+JjgFahQjRopSxPbrbvK7GRe9ePWBo1wcU7sYrFZtavXALwGw/7Dnc50urrHJuTPSoO2IMV3gUQGNg87IbSOIY9BpiT9HV7FCZ94nPXb3MSnwHn/FFFE1vG6DTby+r31KAkUktB3Qf6ikUPWxW1BkXSPQeMHHiW0+HAd2GelJsZz1OJegCxqzl+CLVHa/IibuHeJ1HAKzhuDR+ymNaRFM+4jU6UWKXorRmbyqkq/D76FffevwdCp+jN3UAN/C9JRVTDuOxC/oh+EdMnqIOrlYteKSfadVRGLJFJPSB/ti/6K8f0CNymg/iH2gO/f0DwE0yjAFO6l8JaR5j0VPwPwfaYHqOqrCI319WzwhwzNW/aQAAAABJRU5ErkJggg==',
        className: 'important-node'
      },
      {
        type: 'bpmn:parallelGateway',
        label: '并行网关',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAexJREFUWEfNl80qRVEUx3+3zCjckTJWRD5KwhMIeQAReQIDDKQoMTHyBD7LWDFSPjISyoyBKXMZ3AzEXbV3rXbHOWufq3vvGd2z7ur8f2vttdbeu0CNn0IF+nPACtAG7ANrQCn2e3kBVoGtQOwSmIyFyAMQin8AzQ4mGiIWYAnYUZHLMjwCt0Crs18DY8CXZTliADaAdffRH2AeOHTv3XkhrAAStUQvTyjuA80FYQGwiOeGyAKIEc8FkQaQJV4st90D0AAMA++q6MzL8RdAlrhoTQBnTnQaOAmq3gSRBGARF60p4NSJzgLHCW2XCRECWMWtAOKXCqEBYsRjAFIhPMAMcKRSuADsqffGcroHXMF586jaD7aBC+X/DTwBn8rWB8iUbHG2AxlmHuAZ6EwZMvflghu0jFbl8wp0ZBRmuwe4A4accxi9mPX/Vo4XoCtw7gFuAGlheYoeYBw4d8akUdsE9Fe4BGEx7gKLugg33aHCQydlQgdkaUPv3wtcqcjlt8yRUl21oae1tqMlA9GDKAZC182/jmIrhJyA5DQkm9EI8KYKJDNy71vX27E1E7ozzJFbMxADES0uH89aAh1dWnfkEo8FEH8NIe9VPZb7bIQTs6oXk6Sa8LaoC0lsESbtgHIxWa7V5dS6JWf6/QL7rLtMQZnv1wAAAABJRU5ErkJggg=='
      },
      // {
      //   type:'bpmn:exclusiveGateway',
      //   label: '排他网关',
      //   icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAglJREFUWEfNlztLXzEYhx8HcVG8TIKzxeKl4A39BmL7EQoVFxGXFlRQBAURBBEUxNVqP4Or10mwdRGrH8NJnNQXEgghOXkT4f/3bOckJ7/nvSZpoM5Pwzv0fwCLQCfwG1gBnnLXKwVYBjY8sRPgWy5ECYAv/gi0GphsiFyAeWDLsVzC8Be4BNrN9zNgAnjWhCMHYA1YNYu+AFPAkXnvLYXQAojVYr08vrg1tAhCA6ARL4ZIAeSIF0FUAZSIZ0PEAGLiw0ALcBrJcBlvBqQSVDkRAoiJdwMPgPyzBGx6ELPAvvk28taUrjUQPkCV210A0XEhXHEZGwL+aUrUBdDE3BcSCOmE1nIfLJkTFuA78Mdx6TRwEImzD+FOC4XGjn8xudFmPhxKM7MA/4Geiibjs4QgqsRjnuiyAFfAqJlVZb1daMbsCVIR8sg2LJvUTqL/9wHnQIeZ12EBJoHjRKu1a1eFQNr1dgTCL8td4KebhOvmUGH/D3lCk4S/Ap4YML3DWi595Kt4LqcMPwN3jnVuzOeAPWdsELjJLUP7f6wcPwH3ykbUD9yWNKIUxBjQZBIpFOrxt6NaI3ChEZcFPuxmlPJEVbWpNiG7QOo8IPM0LTrZcmPEGgAtRJblOR7QhKNIPJWEIa+54ZDxmh7LLZDfMWt6MQmFw37LupCU5IAfErmYLNTrcprYdfXDr1I/vkxj15XkAAAAAElFTkSuQmCC'
      // },
      {
        type: 'bpmn:endEvent',
        text: '结束',
        label: '结束节点',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAAH6ji2bAAAABGdBTUEAALGPC/xhBQAAA1BJREFUOBFtVE1IVUEYPXOf+tq40Y3vPcmFIdSjIorWoRG0ERWUgnb5FwVhYQSl72oUoZAboxKNFtWiwKRN0M+jpfSzqJAQclHo001tKkjl3emc8V69igP3znzfnO/M9zcDcKT67azmjYWTwl9Vn7Vumeqzj1DVb6cleQY4oAVnIOPb+mKAGxQmKI5CWNJ2aLPatxWa3aB9K7/fB+/Z0jUF6TmMlFLQqrkECWQzOZxYGjTlOl8eeKaIY5yHnFn486xBustDjWT6dG7pmjHOJd+33t0iitTPkK6tEvjxq4h2MozQ6WFSX/LkDUGfFwfhEZj1Auz/U4pyAi5Sznd7uKzznXeVHlI/Aywmk6j7fsUsEuCGADrWARXXwjxWQsUbIupDHJI7kF5dRktg0eN81IbiZXiTESic50iwS+t1oJgL83jAiBupLDCQqwziaWSoAFSeIR3P5Xv5az00wyIn35QRYTwdSYbz8pH8fxUUAtxnFvYmEmgI0wYXUXcCCSpeEVpXlsRhBnCEATxWylL9+EKCAYhe1NGstUa6356kS9NVvt3DU2fd+Wtbm/+lSbylJqsqkSm9CRhvoJVlvKPvF1RKY/FcPn5j4UfIMLn8D4UYb54BNsilTDXKnF4CfTobA0FpoW/LSp306wkXM+XaOJhZaFkcNM82ASNAWMrhrUbRfmyeI1FvRBTpN06WKxa9BK0o2E4Pd3zfBBEwPsv9sQBnmLVbLEIZ/Xe9LYwJu/Er17W6HYVBc7vmuk0xUQ+pqxdom5Fnp55SiytXLPYoMXNM4u4SNSCFWnrVIzKG3EGyMXo6n/BQOe+bX3FClY4PwydVhthOZ9NnS+ntiLh0fxtlUJHAuGaFoVmttpVMeum0p3WEXbcll94l1wM/gZ0Ccczop77VvN2I7TlsZCsuXf1WHvWEhjO8DPtyOVg2/mvK9QqboEth+7pD6NUQC1HN/TwvydGBARi9MZSzLE4b8Ru3XhX2PBxf8E1er2A6516o0w4sIA+lwURhAON82Kwe2iDAC1Watq4XHaGQ7skLcFOtI5lDxuM2gZe6WFIotPAhbaeYlU4to5cuarF1QrcZ/lwrLaCJl66JBocYZnrNlvm2+MBCTmUymPrYZVbjdlr/BxlMjmNmNI3SAAAAAElFTkSuQmCC',
      }
    ]
    dndPanel.setPatternItems(shapeList);
    //控制面板
    const control = lf.extension.control as Control
    const dagre =  lf.extension.dagre as Dagre
    control.addItem({
      key: "beautify-flow",
      iconClass: "beautify-flow",
      title: "",
      text: "一键美化",
      onClick: (lf, ev) => {
        dagre.render(lf)
        dagre && dagre.layout({});
      }
    })
    //渲染
    let config:LogicFlow.GraphConfigData
    if (actionType.value === 'add'){
      config = {}
      lf.render(config);
    }else {
      common.handleRequestApi(flowableApi.fillModel(detailData.value)).then(res=> {
        config = res.data
        lf.render(config);
      })
    }
  })
})
const saveModel = () => {
  let graphRawData = lf.getGraphRawData()
  detailData.value.edges = graphRawData.edges
  detailData.value.nodes = graphRawData.nodes
  common.handleRequestApi(flowableApi.saveModel(detailData.value)).then(res=>{
    message(res.msg,{type:'success'})
    detailFlag.value = false
    emits('finish')
  })
}
const canEdit = computed(()=>{
  return actionType.value === 'add';
})

</script>

<template>
  <div class="parent">
    <el-card shadow="never">
      <template #header>
        <div class="w-full flex justify-between">
          <div class="flex justify-start content-center">
            <el-input v-model="detailData.processDefinitionKey" size="small" placeholder="请输入流程定义KEY" :disabled="!canEdit"/>
            <div style="width: 10px"/>
            <el-input v-model="detailData.processDefinitionName" size="small" placeholder="请输入流程定义名称" :disabled="!canEdit"/>
          </div>
          <el-button  size="small" type="primary"  @click="saveModel">保存</el-button>
        </div>
      </template>
      <template #default>
        <div id="flowableModel" ref="flowableModel" class="container"/>
      </template>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.parent {
  position: relative;
  width: 100%;
  height: 100%;
}
@media (min-width: 1024px) {
  .container {
    max-width: none;
  }
}
.container {
  position: relative;
  width: 100%;
  height: v-bind(height);
}
</style>
