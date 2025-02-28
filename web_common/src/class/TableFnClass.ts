import type {CSSProperties} from "vue";

class TableFnClass {
  //覆盖数据源获取方法
  cellDblclick: (row: any, column: any, cell: HTMLTableCellElement, event: Event)=>void;
  rowDblclick: (row: any, column: any, event: Event) => void;
  getData:()=>void;
  handleSelectionChange:(newSelection: any[]) => void;
  handleCurrentChange:(value: number) => void;
  handleSizeChange:(value: number) => void;
  sortChange:(data: {column: any, prop: string, order: any }) => void;
  //覆盖刷新方法
  refreshFn:()=>void;
  //覆盖删除方法
  deleteFn:()=>void;
  //覆盖初始化方法
  initFn:()=>void;
  //覆盖新增方法
  addFn:()=>void;
  //覆盖保存方法
  saveFn:()=>void;
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
  rowClassName: (data: { row: any, rowIndex: number }) => string;
  //单元格类
  cellClassName: (data: { row: any, column: any, rowIndex: number, columnIndex: number }) => string;
  //单元格合并
  cellSpanMethod: (data: { row: any, column: any, rowIndex: number, columnIndex: number }) => number[] | { rowspan: number; colspan: number; }
  //行样式
  rowStyle: (data: { row: any, rowIndex: number }) => CSSProperties;
  //展示明细
  toolTipContent: (columnDefine:ColumnDefine,row:any) => string;
  //行按钮操作
  buttonAction: Record<string, (row:any)=>void>;
  //修改上传文件名称
  renameUploadFileName: (row:any,rowIndex:number,fileNum:number,propName:string,fileName:string) => string
  //是否运行上传文件
  allowUploadFile: (row:any,rowIndex:number,propName:string) => boolean
}

export default TableFnClass
