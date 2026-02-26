<script setup lang="ts">
import LogicFlow from "@logicflow/core";
import "@logicflow/core/lib/style/index.css";
import '@logicflow/extension/lib/style/index.css'
import './style.css'
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {BPMNElements ,Control,SelectionSelect,DndPanel,Menu} from "@logicflow/extension";
import Dagre from './Dagre'
import {ShapeItem} from "@logicflow/extension/src/components/dnd-panel";
import {MenuConfig} from "@logicflow/extension/src/components/menu";
import flowableApi from "@/api/flowableApi";
import {isNullOrUnDef} from "@pureadmin/utils";
import {message} from "@/utils/message";
import common from "@/utils/common";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {
  userTaskApprovalTypeDictEnum, userTaskApprovalTypeEnum, userConfigActionTypeDictEnum, userConfigActionTypeEnum,
  bpmnTypeEnum, serviceTaskApprovalTypeEnum, serviceTaskApprovalTypeDictEnum
} from "@/enums/TaskStatusEnum";

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
let distList = {}
const detailData=defineModel('detailData',{type:Object,required:true})
const detailFlag=defineModel('detailFlag',{type:Boolean,required:true})
const actionType = defineModel<'add'|'view'>('actionType')
const emits = defineEmits(['finish'])
interface UserTaskEditProp {
  singleUser?: string,
  candidateUsers?: string[],
  approvalCount?: number,
  category?: string,
  textValue?: string,
  userTaskApprovalType?: string,
  userConfigActionType?: string,
  customVar?: string
}
interface SequenceFlowEditProp {
  conditionalExpression?:string,
  conditionalName?:string
}

let lf:LogicFlow=null
onMounted( ()=>{
  common.handleRequestApi(flowableApi.getDictList()).then(res=>{
    if (res.code !== 200) {
      return message('获取数据字典失败',{type:'error'})
    }
    let userIdMapping = res.data['userIdMapping']
    distList = {...distList,...{
        singleUser: userIdMapping,
        candidateUsers: userIdMapping
      }}
  })
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
      type: bpmnTypeEnum.userTask,
      menu: [
        {
          callback(element: any): void {
            lf.deleteNode(element.id);
          }, className: "", icon: false, text: "删除"
        },
        {
          callback(element: any) {
            showNodeDetail(element)
          },className: "", icon: false,text: "节点配置"
        },
      ],
    });
    menu.setMenuByType({
      type: bpmnTypeEnum.serviceTask,
      menu: [
        {
          callback(element: any): void {
            lf.deleteNode(element.id);
          }, className: "", icon: false, text: "删除"
        },
        {
          callback(element: any) {
            showNodeDetail(element)
          },className: "", icon: false,text: "节点配置"
        },
      ],
    });
    menu.setMenuByType({
      type: bpmnTypeEnum.sequenceFlow,
      menu: [
        {
          callback(element: any): void {
            lf.deleteEdge(element.id);
          }, className: "", icon: false, text: "删除"
        },
        {
          callback(element: any): void {
            showEdgeDetail(element)
          }, className: "", icon: false, text: "设置条件"
        }
      ]
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
        type: bpmnTypeEnum.startEvent,
        text: '开始',
        label: '开始节点',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAAH6ji2bAAAABGdBTUEAALGPC/xhBQAAAnBJREFUOBGdVL1rU1EcPfdGBddmaZLiEhdx1MHZQXApraCzQ7GKLgoRBxMfcRELuihWKcXFRcEWF8HBf0DdDCKYRZpnl7p0svLe9Zzbd29eQhTbC8nv+9zf130AT63jvooOGS8Vf9Nt5zxba7sXQwODfkWpkbjTQfCGUd9gIp3uuPP8bZ946g56dYQvnBg+b1HB8VIQmMFrazKcKSvFW2dQTxJnJdQ77urmXWOMBCmXM2Rke4S7UAW+/8ywwFoewmBps2tu7mbTdp8VMOkIRAkKfrVawalJTtIliclFbaOBqa0M2xImHeVIfd/nKAfVq/LGnPss5Kh00VEdSzfwnBXPUpmykNss4lUI9C1ga+8PNrBD5YeqRY2Zz8PhjooIbfJXjowvQJBqkmEkVnktWhwu2SM7SMx7Cj0N9IC0oQXRo8xwAGzQms+xrB/nNSUWVveI48ayrFGyC2+E2C+aWrZHXvOuz+CiV6iycWe1Rd1Q6+QUG07nb5SbPrL4426d+9E1axKjY3AoRrlEeSQo2Eu0T6BWAAr6COhTcWjRaYfKG5csnvytvUr/WY4rrPMB53Uo7jZRjXaG6/CFfNMaXEu75nG47X+oepU7PKJvvzGDY1YLSKHJrK7vFUwXKkaxwhCW3u+sDFMVrIju54RYYbFKpALZAo7sB6wcKyyrd+aBMryMT2gPyD6GsQoRFkGHr14TthZni9ck0z+Pnmee460mHXbRAypKNy3nuMdrWgVKj8YVV8E7PSzp1BZ9SJnJAsXdryw/h5ctboUVi4AFiCd+lQaYMw5z3LGTBKjLQOeUF35k89f58Vv/tGh+l+PE/wG0rgfIUbZK5AAAAABJRU5ErkJggg==',
      },
      {
        type: bpmnTypeEnum.userTask,
        label: '用户任务',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAATCAYAAAEFVwZaAAAABGdBTUEAALGPC/xhBQAAAqlJREFUOBF9VM9rE0EUfrMJNUKLihGbpLGtaCOIR8VjQMGDePCgCCIiCNqzCAp2MyYUCXhUtF5E0D+g1t48qAd7CCLqQUQKEWkStcEfVGlLdp/fm3aW2QQdyLzf33zz5m2IsAZ9XhDpyaaIZkTS4ASzK41TFao88GuJ3hsr2pAbipHxuSYyKRugagICGANkfFnNh3HeE2N0b3nN2cgnpcictw5veJIzxmDamSlxxQZicq/mflxhbaH8BLRbuRwNtZp0JAhoplVRUdzmCe/vO27wFuuA3S5qXruGdboy5/PRGFsbFGKo/haRtQHIrM83bVeTrOgNhZReWaYGnE4aUQgTJNvijJFF4jQ8BxJE5xfKatZWmZcTQ+BVgh7s8SgPlCkcec4mGTmieTP4xd7PcpIEg1TX6gdeLW8rTVMVLVvb7ctXoH0Cydl2QOPJBG21STE5OsnbweVYzAnD3A7PVILuY0yiiyDwSm2g441r6rMSgp6iK42yqroI2QoXeJVeA+YeZSa47gZdXaZWQKTrG93rukk/l2Al6Kzh5AZEl7dDQy+JjgFahQjRopSxPbrbvK7GRe9ePWBo1wcU7sYrFZtavXALwGw/7Dnc50urrHJuTPSoO2IMV3gUQGNg87IbSOIY9BpiT9HV7FCZ94nPXb3MSnwHn/FFFE1vG6DTby+r31KAkUktB3Qf6ikUPWxW1BkXSPQeMHHiW0+HAd2GelJsZz1OJegCxqzl+CLVHa/IibuHeJ1HAKzhuDR+ymNaRFM+4jU6UWKXorRmbyqkq/D76FffevwdCp+jN3UAN/C9JRVTDuOxC/oh+EdMnqIOrlYteKSfadVRGLJFJPSB/ti/6K8f0CNymg/iH2gO/f0DwE0yjAFO6l8JaR5j0VPwPwfaYHqOqrCI319WzwhwzNW/aQAAAABJRU5ErkJggg==',
        className: 'important-node'
      },
      {
        type: bpmnTypeEnum.serviceTask,
        label: '服务任务',
        icon:'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCIgdmlld0JveD0iMCAwIDIwIDIwIj48ZyBmaWxsPSJjdXJyZW50Q29sb3IiIGZpbGwtcnVsZT0iZXZlbm9kZCIgY2xpcC1ydWxlPSJldmVub2RkIj48cGF0aCBkPSJNMTAgMTJhMiAyIDAgMSAwIDAtNGEyIDIgMCAwIDAgMCA0bTAgMmE0IDQgMCAxIDAgMC04YTQgNCAwIDAgMCAwIDhtMy41MDQtMTEuMjlhMiAyIDAgMCAxIDIuMzA5LjM3NWwxLjEwMiAxLjEwMmEyIDIgMCAwIDEgLjM3NSAyLjMwOWwtLjM4NS43N2ExIDEgMCAxIDEtMS43ODktLjg5NWwuMzg1LS43N0wxNC4zOTkgNC41bC0uNzcuMzg1YTEgMSAwIDEgMS0uODk0LTEuNzg5eiIvPjxwYXRoIGQ9Ik0xNS42NDQgNS44ODhhMSAxIDAgMCAxIDEuMjk3LjU2NGwuNDYgMS4xNjZhMSAxIDAgMCAxLTEuODYxLjczM2wtLjQ2LTEuMTY2YTEgMSAwIDAgMSAuNTY0LTEuMjk3Ii8+PHBhdGggZD0iTTE3LjYzMyA3LjMyM0EyIDIgMCAwIDEgMTkgOS4yMjF2MS41NThhMiAyIDAgMCAxLTEuMzY3IDEuODk4bC0uODE3LjI3MmExIDEgMCAxIDEtLjYzMi0xLjg5OEwxNyAxMC43OFY5LjIyMWwtLjgxNi0uMjcyYTEgMSAwIDAgMSAuNjMyLTEuODk4eiIvPjxwYXRoIGQ9Ik0xNi44OTkgMTEuMDgzYTEgMSAwIDAgMSAuNTE4IDEuMzE2bC0uNSAxLjE1YTEgMSAwIDEgMS0xLjgzNC0uNzk4bC41LTEuMTVhMSAxIDAgMCAxIDEuMzE2LS41MTgiLz48cGF0aCBkPSJNMTcuMjkgMTMuNTA0YTIgMiAwIDAgMS0uMzc1IDIuMzA5bC0xLjEwMiAxLjEwMmEyIDIgMCAwIDEtMi4zMDkuMzc1bC0uNzctLjM4NWExIDEgMCAxIDEgLjg5NS0xLjc4OWwuNzcuMzg1bDEuMTAyLTEuMTAybC0uMzg1LS43N2ExIDEgMCAwIDEgMS43ODktLjg5NHoiLz48cGF0aCBkPSJNMTQuMTEyIDE1LjY0NGExIDEgMCAwIDEtLjU2MyAxLjI5N2wtMS4xNjcuNDZhMSAxIDAgMSAxLS43MzMtMS44NjFsMS4xNjctLjQ2YTEgMSAwIDAgMSAxLjI5Ni41NjQiLz48cGF0aCBkPSJNMTIuNjc3IDE3LjYzM0EyIDIgMCAwIDEgMTAuNzc5IDE5SDkuMjIxYTIgMiAwIDAgMS0xLjg5OC0xLjM2N2wtLjI3Mi0uODE3YTEgMSAwIDAgMSAxLjg5OC0uNjMyTDkuMjIgMTdoMS41NThsLjI3Mi0uODE2YTEgMSAwIDAgMSAxLjg5OC42MzJ6Ii8+PHBhdGggZD0iTTguOTE3IDE2Ljg5OWExIDEgMCAwIDEtMS4zMTYuNTE4bC0xLjE1LS41YTEgMSAwIDEgMSAuNzk4LTEuODM0bDEuMTUuNWExIDEgMCAwIDEgLjUxOCAxLjMxNiIvPjxwYXRoIGQ9Ik02LjQ5NiAxNy4yOWEyIDIgMCAwIDEtMi4zMDktLjM3NWwtMS4xMDItMS4xMDJhMiAyIDAgMCAxLS4zNzUtMi4zMDlsLjM4NS0uNzdhMSAxIDAgMSAxIDEuNzg5Ljg5NWwtLjM4NS43N0w1LjYwMSAxNS41bC43Ny0uMzg1YTEgMSAwIDEgMSAuODk0IDEuNzg5eiIvPjxwYXRoIGQ9Ik00LjM1NiAxNC4xMTJhMSAxIDAgMCAxLTEuMjk3LS41NjNsLS40Ni0xLjE2N2ExIDEgMCAwIDEgMS44NjEtLjczM2wuNDYgMS4xNjZhMSAxIDAgMCAxLS41NjQgMS4yOTciLz48cGF0aCBkPSJNMi4zNjggMTIuNjc3QTIgMiAwIDAgMSAxIDEwLjc3OVY5LjIyMWEyIDIgMCAwIDEgMS4zNjgtMS44OThsLjgxNi0uMjcyYTEgMSAwIDEgMSAuNjMyIDEuODk4TDMgOS4yMnYxLjU1OGwuODE2LjI3MmExIDEgMCAxIDEtLjYzMiAxLjg5OHoiLz48cGF0aCBkPSJNMy4xMDEgOC45MTdhMSAxIDAgMCAxLS41MTgtMS4zMTZsLjUtMS4xNWExIDEgMCAwIDEgMS44MzQuNzk4bC0uNSAxLjE1YTEgMSAwIDAgMS0xLjMxNi41MTgiLz48cGF0aCBkPSJNMi43MSA2LjQ5NmEyIDIgMCAwIDEgLjM3NS0yLjMwOWwxLjEwMi0xLjEwMmEyIDIgMCAwIDEgMi4zMDktLjM3NWwuNzcuMzg1YTEgMSAwIDEgMS0uODk1IDEuNzg5bC0uNzctLjM4NUw0LjUgNS42MDFsLjM4NS43N2ExIDEgMCAxIDEtMS43ODkuODk0eiIvPjxwYXRoIGQ9Ik01Ljg4NyA0LjM1NmExIDEgMCAwIDEgLjU2NC0xLjI5N2wxLjE2Ny0uNDZhMSAxIDAgMSAxIC43MzMgMS44NjFsLTEuMTY3LjQ2YTEgMSAwIDAgMS0xLjI5Ny0uNTY0Ii8+PHBhdGggZD0iTTcuMzIzIDIuMzY4QTIgMiAwIDAgMSA5LjIyMSAxaDEuNTU4YTIgMiAwIDAgMSAxLjg5OCAxLjM2OGwuMjcyLjgxNmExIDEgMCAwIDEtMS44OTguNjMyTDEwLjc4IDNIOS4yMjFsLS4yNzIuODE2YTEgMSAwIDAgMS0xLjg5OC0uNjMyeiIvPjxwYXRoIGQ9Ik0xMS4wODMgMy4xMDFhMSAxIDAgMCAxIDEuMzE2LS41MThsMS4xNS41YTEgMSAwIDAgMS0uNzk4IDEuODM0bC0xLjE1LS41YTEgMSAwIDAgMS0uNTE4LTEuMzE2Ii8+PC9nPjwvc3ZnPg=='
      },
      {
        type: bpmnTypeEnum.parallelGateway,
        label: '并行网关',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAexJREFUWEfNl80qRVEUx3+3zCjckTJWRD5KwhMIeQAReQIDDKQoMTHyBD7LWDFSPjISyoyBKXMZ3AzEXbV3rXbHOWufq3vvGd2z7ur8f2vttdbeu0CNn0IF+nPACtAG7ANrQCn2e3kBVoGtQOwSmIyFyAMQin8AzQ4mGiIWYAnYUZHLMjwCt0Crs18DY8CXZTliADaAdffRH2AeOHTv3XkhrAAStUQvTyjuA80FYQGwiOeGyAKIEc8FkQaQJV4st90D0AAMA++q6MzL8RdAlrhoTQBnTnQaOAmq3gSRBGARF60p4NSJzgLHCW2XCRECWMWtAOKXCqEBYsRjAFIhPMAMcKRSuADsqffGcroHXMF586jaD7aBC+X/DTwBn8rWB8iUbHG2AxlmHuAZ6EwZMvflghu0jFbl8wp0ZBRmuwe4A4accxi9mPX/Vo4XoCtw7gFuAGlheYoeYBw4d8akUdsE9Fe4BGEx7gKLugg33aHCQydlQgdkaUPv3wtcqcjlt8yRUl21oae1tqMlA9GDKAZC182/jmIrhJyA5DQkm9EI8KYKJDNy71vX27E1E7ozzJFbMxADES0uH89aAh1dWnfkEo8FEH8NIe9VPZb7bIQTs6oXk6Sa8LaoC0lsESbtgHIxWa7V5dS6JWf6/QL7rLtMQZnv1wAAAABJRU5ErkJggg=='
      },
      {
        type:bpmnTypeEnum.exclusiveGateway,
        label: '排他网关',
        icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAglJREFUWEfNlztLXzEYhx8HcVG8TIKzxeKl4A39BmL7EQoVFxGXFlRQBAURBBEUxNVqP4Or10mwdRGrH8NJnNQXEgghOXkT4f/3bOckJ7/nvSZpoM5Pwzv0fwCLQCfwG1gBnnLXKwVYBjY8sRPgWy5ECYAv/gi0GphsiFyAeWDLsVzC8Be4BNrN9zNgAnjWhCMHYA1YNYu+AFPAkXnvLYXQAojVYr08vrg1tAhCA6ARL4ZIAeSIF0FUAZSIZ0PEAGLiw0ALcBrJcBlvBqQSVDkRAoiJdwMPgPyzBGx6ELPAvvk28taUrjUQPkCV210A0XEhXHEZGwL+aUrUBdDE3BcSCOmE1nIfLJkTFuA78Mdx6TRwEImzD+FOC4XGjn8xudFmPhxKM7MA/4Geiibjs4QgqsRjnuiyAFfAqJlVZb1daMbsCVIR8sg2LJvUTqL/9wHnQIeZ12EBJoHjRKu1a1eFQNr1dgTCL8td4KebhOvmUGH/D3lCk4S/Ap4YML3DWi595Kt4LqcMPwN3jnVuzOeAPWdsELjJLUP7f6wcPwH3ykbUD9yWNKIUxBjQZBIpFOrxt6NaI3ChEZcFPuxmlPJEVbWpNiG7QOo8IPM0LTrZcmPEGgAtRJblOR7QhKNIPJWEIa+54ZDxmh7LLZDfMWt6MQmFw37LupCU5IAfErmYLNTrcprYdfXDr1I/vkxj15XkAAAAAElFTkSuQmCC'
      },
      {
        type: bpmnTypeEnum.endEvent,
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
    //监听事件
    lf.on('node:click', ({ data }) => {
      console.log('节点被点击:', data);
      showNodeDetail(data as NodeElement)
    })
    lf.on('edge:click', ({ data }) => {
      console.log('边被点击:', data);
      showEdgeDetail(data)
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


//节点展示
interface NodeText {
  x?:number,
  y?:number,
  value?:string
}
interface ElementProp {
  assignee?: string,
  assigneeName?: string,
  userTaskApprovalType?: string,
  candidateUsers?: string[],
  approvalCount?: number,
  category?: string,
  conditionalExpression?: string,
  userConfigActionType?: string,
  customVar?: string
}
interface NodeElement {
  id?:string,
  x?:number,
  y?:number,
  type?: string,
  text?: NodeText,
  properties?:ElementProp
}
const formItemShowMapping = ref<Record<string, boolean>>({
  singleUser: false,
  candidateUsers: false,
  approvalCount: false,
  customVar: false
})
const formItemShow = (item:DetailColumnDefine) => {
  if (isNullOrUnDef(formItemShowMapping.value)){
    return true
  }else {
    let show=formItemShowMapping.value[item.prop]
    if (isNullOrUnDef(show)){
      return true
    }else {
      return show
    }
  }
}

const nodeDetailColumns = ref<Array<DetailColumnDefine>>([])
const nodeDetailData = ref<UserTaskEditProp>({})
const nodeDetailFlag = ref(false)
let saveNodeDetail:()=>void
let changeModelNode:()=>void

let currentNodeType = ''
const userTaskDetailColumns: Array<DetailColumnDefine> = [
  {prop: 'userTaskApprovalType', label: '审批类型',type: QueryTypeEnum.OPTION, disabled: false},
  {prop: 'userConfigActionType', label: '人员配置类型',type: QueryTypeEnum.OPTION, disabled: false},
  {prop: 'singleUser', label: '单人员选择',type: QueryTypeEnum.OPTION, disabled: false},
  {prop: 'candidateUsers', label: '多人员选择',type: QueryTypeEnum.MULTIPLE_OPTION, disabled: false},
  // {prop: 'textValue', label: '节点名称',type: QueryTypeEnum.INPUT, placeholder:'不填写时默认审批人员', disabled: false},
  {prop: 'customVar', label: '自定义变量',type: QueryTypeEnum.INPUT, disabled: false,placeholder:"必须使用${}包裹变量,例如${processVar}或${func.execute()};单签直接传人员id,会签和或签传人员id数组"},
  {prop: 'approvalCount', label: '审批通过数量',type: QueryTypeEnum.INPUT, disabled: false},
  {prop: 'category', label: '节点分类',type: QueryTypeEnum.INPUT, disabled: false},
]
const serviceTaskDetailColumns: Array<DetailColumnDefine> = [
  {prop: 'category', label: '节点分类',type: QueryTypeEnum.INPUT, disabled: false,placeholder:"区分多服务节点，建议填写唯一标识"},
  {prop: 'userTaskApprovalType', label: '类型',type: QueryTypeEnum.OPTION, disabled: false},
  {prop: 'candidateUsers', label: '人员选择',type: QueryTypeEnum.MULTIPLE_OPTION, disabled: false,placeholder:"可为空"},
  {prop: 'customVar', label: '自定义表达式',type: QueryTypeEnum.INPUT, disabled: false,placeholder:"必须使用${}包裹变量,例如${func.execute()}"},
]
watch(()=>[
  nodeDetailData.value['userTaskApprovalType'],
  nodeDetailData.value['userConfigActionType']
],(newValue,oldValue)=>{
  if (currentNodeType===bpmnTypeEnum.userTask){
    if (userTaskApprovalTypeEnum.all===newValue[0]){
      formItemShowMapping.value = {
        userConfigActionType:true,
        singleUser: false,
        candidateUsers: userConfigActionTypeEnum.fixedSelect === newValue[1],
        customVar: userConfigActionTypeEnum.customVar === newValue[1],
        approvalCount: false
      }
    }else if (userTaskApprovalTypeEnum.or===newValue[0]){
      formItemShowMapping.value = {
        userConfigActionType:true,
        singleUser: false,
        candidateUsers: userConfigActionTypeEnum.fixedSelect === newValue[1],
        customVar: userConfigActionTypeEnum.customVar === newValue[1],
        approvalCount: true
      }
    }else if (userTaskApprovalTypeEnum.single===newValue[0]){
      formItemShowMapping.value = {
        userConfigActionType:true,
        singleUser: userConfigActionTypeEnum.fixedSelect === newValue[1],
        customVar: userConfigActionTypeEnum.customVar === newValue[1],
        candidateUsers: false,
        approvalCount: false
      }
    }else {
      formItemShowMapping.value = {
        userConfigActionType:false,
        singleUser: false,
        candidateUsers: false,
        customVar: false,
        approvalCount: false
      }
    }
  }
  if (currentNodeType===bpmnTypeEnum.serviceTask){
    if (serviceTaskApprovalTypeEnum.cc===newValue[0]){
      formItemShowMapping.value = {
        candidateUsers: true,
        customVar: false
      }
    }else if (serviceTaskApprovalTypeEnum.customExpression===newValue[0]){
      formItemShowMapping.value = {
        candidateUsers: false,
        customVar: true
      }
    }else {
      formItemShowMapping.value = {
        candidateUsers: false,
        customVar: false
      }
    }
  }
})
const updateNodeElementProp = (element:NodeElement)=>{
  let data:UserTaskEditProp = nodeDetailData.value
  if (common.isStrBlank(data.userTaskApprovalType)){
    return message('请选择类型后操作',{type:'error'})
  }
  if (!isNullOrUnDef(data.category)){
    element.properties.category = data.category
  }
  element.properties.userTaskApprovalType = data.userTaskApprovalType
  element.properties.userConfigActionType = data.userConfigActionType
  if (element.type===bpmnTypeEnum.userTask){
    if (data.userTaskApprovalType===userTaskApprovalTypeEnum.single){
      //单签节点默认：人员名称
      if (userConfigActionTypeEnum.fixedSelect===data.userConfigActionType){
        element.properties.assignee = data.singleUser
        element.properties.assigneeName = distList['singleUser'][data.singleUser]
        element.text.value = element.properties.assigneeName
      }
      if (userConfigActionTypeEnum.customVar===data.userConfigActionType){
        element.properties.customVar = data.customVar
        element.text.value = `自定义\n[${element.properties.customVar}]`
      }
    }
    if (data.userTaskApprovalType===userTaskApprovalTypeEnum.or){
      //或签节点默认：或签(审批数量): user1、user2、user3
      if (userConfigActionTypeEnum.fixedSelect===data.userConfigActionType){
        element.properties.candidateUsers = data.candidateUsers
        element.properties.approvalCount = data.approvalCount
        element.text.value = `或签[n>=${element.properties.approvalCount}]`
      }
      if (userConfigActionTypeEnum.customVar===data.userConfigActionType){
        element.properties.customVar = data.customVar
        element.properties.approvalCount = data.approvalCount
        element.text.value = `或签自定义\n[n>=${element.properties.approvalCount}]\n[${element.properties.customVar}]`
      }
    }
    if (data.userTaskApprovalType===userTaskApprovalTypeEnum.all){
      //会签节点默认: 会签: user1、user2、user3
      if (userConfigActionTypeEnum.fixedSelect===data.userConfigActionType){
        element.properties.candidateUsers = data.candidateUsers
        element.text.value = `会签[n>=${element.properties.candidateUsers.length}]`
      }
      if (userConfigActionTypeEnum.customVar===data.userConfigActionType){
        element.properties.customVar = data.customVar
        element.text.value = `会签自定义\n[n>=n?]\n[${element.properties.customVar}]`
      }
    }
  }
  if (element.type===bpmnTypeEnum.serviceTask){
    if (data.userTaskApprovalType===serviceTaskApprovalTypeEnum.cc){
      element.properties.candidateUsers = data.candidateUsers
      element.text.value = `【服务】\n抄送(${element.properties.candidateUsers.length})`
    }
    if (data.userTaskApprovalType===serviceTaskApprovalTypeEnum.customExpression){
      element.properties.customVar = data.customVar
      element.text.value = `【服务】\n自定义\n[${element.properties.customVar}]`
    }
  }
}
const showNodeDetail = (element:NodeElement)=>{
  if (![bpmnTypeEnum.serviceTask,bpmnTypeEnum.userTask].includes(element.type)){
    return
  }
  currentNodeType = element.type
  if (element.type===bpmnTypeEnum.userTask){
    nodeDetailColumns.value = userTaskDetailColumns
    distList = {...distList,...{
        'userTaskApprovalType':userTaskApprovalTypeDictEnum,
        'userConfigActionType':userConfigActionTypeDictEnum
      }}
  }
  if (element.type===bpmnTypeEnum.serviceTask){
    nodeDetailColumns.value = serviceTaskDetailColumns
    distList = {...distList,...{
        'userTaskApprovalType':serviceTaskApprovalTypeDictEnum
      }}
  }
  //userTask操作
  common.showGlobalLoading(()=>{
    if (isNullOrUnDef(element.properties)){
      element.properties={}
    }
    if (isNullOrUnDef(element.text)){
      element.text = {}
    }
    nodeDetailData.value = {
      singleUser:element.properties.assignee,
      category: element.properties.category,
      textValue: element.text.value,
      userTaskApprovalType: element.properties.userTaskApprovalType,
      candidateUsers: element.properties.candidateUsers,
      approvalCount: element.properties.approvalCount,
      userConfigActionType: element.properties.userConfigActionType,
      customVar: element.properties.customVar
    }
    saveNodeDetail=()=>{
      updateNodeElementProp(element)
      lf.setProperties(element.id,element.properties)
      lf.updateText(element.id,element.text.value)
      nodeDetailFlag.value = false
    }
    changeModelNode = ()=>{
      saveNodeDetail()
      common.showMsgDialog('是否修改节点定义').then(()=>{
        let data = {
          node:element,
          processDefinitionKey: detailData.value.processDefinitionKey
        }
        common.handleRequestApi(flowableApi.changeModelNode(data)).then(res=>{
          message('修改成功',{type: 'success'})
        })
      })
    }
    nodeDetailFlag.value = true
    nextTick(()=>{
      common.closeGlobalLoading()
    })
  })
}
//边展示
const edgeDetailColumns: Array<DetailColumnDefine> = [
  {prop: 'conditionalExpression', label: '条件表达式',type: QueryTypeEnum.INPUT,placeholder:"必须使用${}包裹条件"},
  {prop: 'conditionalName', label: '条件名称',type: QueryTypeEnum.INPUT}
]
const edgeDetailData = ref<SequenceFlowEditProp>({})
const edgeDetailFlag = ref(false)
let saveEdgeDetail:()=>void
let changeModelEdge:()=>void
const showEdgeDetail = (element:any)=>{
  common.showGlobalLoading(()=>{
    if (isNullOrUnDef(element.properties)){
      element.properties={}
    }
    if (isNullOrUnDef(element.text)){
      element.text = {}
    }
    edgeDetailData.value={
      conditionalExpression: element.properties.conditionalExpression,
      conditionalName: element.text.value,
    }
    saveEdgeDetail = ()=>{
      let data:SequenceFlowEditProp = edgeDetailData.value
      if (!isNullOrUnDef(data.conditionalName)){
        element.text.value = data.conditionalName
      }
      if (!isNullOrUnDef(data.conditionalExpression)){
        element.properties.conditionalExpression = data.conditionalExpression
      }
      lf.setProperties(element.id,element.properties)
      lf.updateText(element.id,element.text.value)
      edgeDetailFlag.value=false
    }
    changeModelEdge = ()=>{
      saveEdgeDetail()
      common.showMsgDialog('是否修改边定义').then(()=>{
        let data = {
          edge:element,
          processDefinitionKey: detailData.value.processDefinitionKey
        }
        common.handleRequestApi(flowableApi.changeModelEdge(data)).then(res=>{
          message('修改成功',{type: 'success'})
        })
      })
    }
    edgeDetailFlag.value = true
    nextTick(()=>{
      common.closeGlobalLoading()
    })
  })
}
//复制模型配置
const copyModel = () => {
  let graphRawData = lf.getGraphRawData()
  let flag = common.copyText(JSON.stringify(graphRawData));
  if (flag){
    message('复制成功',{type:'success'})
  }
}
//粘贴导入模型配置
const importModel = () => {
  let columns: Array<DetailColumnDefine> = [
    {prop: 'json', label: 'JSON配置',type: QueryTypeEnum.AREA_INPUT,placeholder:'此处粘贴'},
  ]
  let params:OpenInputDialogDefine = {
    columns: columns,
    callBack: (result) => {
      let json = result.data.json
      if (common.isStrBlank(json)){
        return message('请输入JSON配置', { type: 'error' })
      }
      lf.renderRawData(JSON.parse(json))
      message('操作成功',{type:'success'})
      result.done()
    }
  }
  common.openInputDialog(params)
}
//保存模型
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
          <div class="flex">
            <el-button  size="small" type="primary"  @click="copyModel">复制</el-button>
            <el-button  size="small" type="primary"  @click="importModel">导入</el-button>
            <el-button  size="small" type="primary"  @click="saveModel">保存</el-button>
          </div>
        </div>
      </template>
      <template #default>
        <div id="flowableModel" ref="flowableModel" class="container"/>
      </template>
    </el-card>
    <el-drawer v-model="nodeDetailFlag" size="30%" append-to-body>
      <template #header>
        <div class="flex justify-between items-center">
          <div style="font-size: 18px;font-weight: bolder;color: black">节点属性</div>
        </div>
      </template>
      <div>
        <el-button size="small" type="primary" @click="saveNodeDetail">保存</el-button>
        <el-button v-if="actionType==='view'" size="small" type="primary" @click="changeModelNode">修改</el-button>
      </div>
      <div class="h-[50px]"></div>
      <el-form label-position="right" label-width="auto">
        <template v-for="(item, index) in nodeDetailColumns" :key="index">
          <el-form-item :label="item.label" v-show="formItemShow(item)">
            <el-input
                v-if="item.type === QueryTypeEnum.INPUT"
                v-model="nodeDetailData[item.prop]"
                :placeholder="item.placeholder"
                :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
            />
            <el-select v-else-if="item.type === QueryTypeEnum.OPTION"
                       v-model="nodeDetailData[item.prop]"
                        clearable  filterable :placeholder="item.placeholder">
              <el-option
                  v-for="(value,key) in distList[item.prop]"
                  :key="key"
                  :label="value"
                  :value="key"
              />
            </el-select>
            <el-select v-else-if="item.type === QueryTypeEnum.MULTIPLE_OPTION"
                       v-model="nodeDetailData[item.prop]"
                       clearable multiple  filterable :placeholder="item.placeholder">
              <el-option
                  v-for="(value,key) in distList[item.prop]"
                  :key="key"
                  :label="value"
                  :value="key"
              />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
    </el-drawer>
    <el-drawer v-model="edgeDetailFlag" size="30%" append-to-body>
      <template #header>
        <div class="flex justify-between items-center">
          <div style="font-size: 18px;font-weight: bolder;color: black">边属性</div>
        </div>
      </template>
      <div>
        <el-button size="small" type="primary" @click="saveEdgeDetail">保存</el-button>
        <el-button v-if="actionType==='view'" size="small" type="primary" @click="changeModelEdge">修改</el-button>
      </div>
      <div class="h-[50px]"></div>
      <el-form label-position="right" label-width="auto">
        <template v-for="(item, index) in edgeDetailColumns" :key="index">
          <el-form-item :label="item.label">
            <el-input
                v-if="item.type === QueryTypeEnum.INPUT"
                v-model="edgeDetailData[item.prop]"
                :placeholder="item.placeholder"
                :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
            />
          </el-form-item>
        </template>
      </el-form>
    </el-drawer>
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
