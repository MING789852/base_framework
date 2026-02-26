import type {CSSProperties} from "vue";
import {Column} from "element-plus/es/components/table-v2/src/types";


class TableFnClass {
  //覆盖数据源获取方法
  cellClick: (row: any, column: any, cell: HTMLTableCellElement, event: Event)=>void;
  cellDblclick: (row: any, column: any, cell: HTMLTableCellElement, event: Event)=>void;
  rowDblclick: (row: any, column: any, event: Event) => void;
  rowClick: (row: any, column: any, event: Event) => void;
  getData:()=>void;
  handleSelectionChange:(newSelection: any[]) => void;
  handleCurrentChange:(value: number) => void;
  handleSizeChange:(value: number) => void;
  sortChange:(data: {column: any, prop: string, order: 'ascending' | 'descending' | null | undefined }) => void;
  //覆盖刷新方法
  refreshFn:()=>void;
  //覆盖删除方法
  deleteFn:(list?:any[])=>void;
  //覆盖初始化方法
  initFn:()=>void;
  //覆盖新增方法
  addFn:()=>void;
  //覆盖修改方法
  editFn: (cloneRow?:any,originRow?:any)=>void;
  //覆盖保存方法
  //callBack用于保存成功后的回调
  saveFn:(row?:any,saveParams?:SaveParams)=>void;
  //覆盖查询方法
  queryFn:()=>void;
  //覆盖查询弹窗前初始化方法
  beforeQueryDialogOpenFn:()=>Promise<any>;
  //查询条件初始化
  handleQuery: ()=>void;
  //初始化查询条件和清空查询条件
  initQuery: ()=>void;
  //导出excel
  exportExcelFn: ()=>void;
  //行类
  rowClassName: (data: { row: any, rowIndex: number,columns?:Column[] }) => string;
  //单元格类
  cellClassName: (data: { row: any, column: any, rowIndex: number, columnIndex: number }) => string;
  //单元格合并
  cellSpanMethod: (data: { row: any, column: any, rowIndex: number, columnIndex: number }) => number[] | { rowspan: number; colspan: number; }
  //行样式
  rowStyle: (data: { row: any, rowIndex: number }) => CSSProperties;
  //单元格样式
  headerCellStyle: (data: { row: any, column: any, rowIndex: number, columnIndex: number }) => CSSProperties;
  cellStyle: (data: { row: any, column: any, rowIndex: number, columnIndex: number }) => CSSProperties;
  //显示提示
  showToolTip:(data: {row: any, column: any,prop: string,position: 'header'|'cell'})=>boolean;
  getToolTipContent: (data: {row: any, column: any,prop: string,position: 'header'|'cell'})=>string;
  //行按钮操作
  buttonAction: ButtonAction;
  //按钮选择
  buttonSelect: ButtonSelect;
  //文件操作
  fileAction: FileAction;
  //图片属性
  imgProp: ImgProp;
  //编辑组件操作
  editAction: EditAction
}

export default TableFnClass
