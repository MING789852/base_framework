<script setup lang="ts">
import TableColumnValue from "@/components/table/tableColumnValue/tableColumnValue.vue";
import TableFnClass from "@/class/TableFnClass";
import {CSSProperties, ref} from "vue";

interface Prop {
  tableHeight?: number,
  tableWidth?: number,
  columns?: ColumnDefine[],
  rowHeight?: number,
  rowIndex?: number,
  rowData?: any,
  dictList?: any,
  border?: boolean,
  tableFn?: TableFnClass,
  rowColumnSpanMap?: any
}

const props = withDefaults(defineProps<Prop>(), {
  tableHeight: () => 0,
  tableWidth: () => 0,
  columns: () => [],
  rowIndex: () => 0,
  rowData: () => ({}),
  border: () => false,
  rowHeight: () => 0,
  dictList: () => ({}),
  rowColumnSpanMap: () => ({}),
  tableFn: () => new TableFnClass(),
})
const deleteFileIdList = defineModel<string[]>('deleteFileIdList', {required: false, default: []})
const uploadMap = defineModel<Map<String, File>>('uploadMap', {required: false, default: new Map<String, File>()})

interface MergeZeroMap{
  rowSpanMap:any,
  colSpanMap:any
}
const mergeZeroMap = defineModel<MergeZeroMap>('mergeZeroMap', {required: false, default: () => ({rowSpanMap:{},colSpanMap:{}})})
const borderStyle  = ref('1px solid #ebeef5')
const getStyle = (rowIndex: number,colIndex:number) => {
  const spanKey = `${rowIndex}_${colIndex}`;
  let spanData = props.rowColumnSpanMap[spanKey];
  let style: CSSProperties;
  let colWidth = props.columns[colIndex]?.width as number
  const { colspan:colSpan, rowspan:rowSpan } = spanData??{colspan:0, rowspan:0};
  if (colSpan!=0&&rowSpan!=0) {
    // 计算宽度（列合并）
    let width = colWidth;
    if (colSpan > 1) {
      for (let i = 1; i < colSpan; i++) {
        let spanColIndex=colIndex + i
        const column = props.columns[spanColIndex];
        width += (column?.width as number) ?? 0;
        //记录被合并的列
        mergeZeroMap.value.colSpanMap[rowIndex+'_'+spanColIndex] = 0
      }
    }
    let height = props.rowHeight;
    if (rowSpan > 1){
      for (let i = 1; i < rowSpan; i++){
        let spanRowIndex = rowIndex + i
        height += props.rowHeight
        //记录被合并的行
        mergeZeroMap.value.rowSpanMap[spanRowIndex + '_' + colIndex] = 0
      }
    }
    style = {
      width: `${width}px`,
      minHeight: `${height}px`,
      ...(rowSpan > 1 && {
        alignSelf: 'flex-start',
        zIndex: 20
      }),
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
    };
  } else {
    let display = 'flex';
    let visible = true;
    let zeroWidth = mergeZeroMap.value.colSpanMap[spanKey]
    if (zeroWidth===0){
      display = 'none'
    }
    let height = mergeZeroMap.value.rowSpanMap[spanKey]
    if (height===0){
      visible = false
    }
    style = {
      display: display,
      visibility: visible?'visible':'hidden',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: `${props.rowHeight}px`,
      width: `${colWidth}px`
    };
  }
  return style
};
</script>

<template>
  <div class="h-full flex flex-row">
    <table-column-value v-for="(item,index) in columns"
                        :item="item"
                        :dict-list="dictList"
                        :table-fn="tableFn"
                        :style="getStyle(rowIndex,index)"
                        :class="border?'border_right':''"
                        v-model:delete-file-id-list="deleteFileIdList"
                        v-model:upload-map="uploadMap"
                        :scope="{
                          row: rowData,
                          $index: rowIndex
                        }"
    />
  </div>
</template>

<style scoped lang="scss">
.border_right{
  width: 100%;
  height: 100%;
  border-right: v-bind(borderStyle);
  border-bottom: v-bind(borderStyle);
  &:last-child{
    border-right: none;
  }
}
</style>