declare interface IndexColumn {
  label: string,
  prop: string,
  show?: boolean,
  fixed?: boolean
}

declare interface CommonTableType {
  isAdd: boolean,
  selectTableData: any[],
  tableData: any[],
  loading: boolean,
  query: QueryDefine,
  addFn: () => void,
  deleteFn: () => void,
  saveFn: () => void,
  queryFn: () => void,
  getData: () => void
}

declare interface InputDisabled{
  [key:string]:(row:any,prop:string)=>Boolean
}

declare interface CommonTableButton{
  queryFn?: boolean,
  addFn?: boolean,
  saveFn?: boolean,
  deleteFn?: boolean,
  refreshFn?: boolean,
  initQueryFn?: boolean,
  exportExcelFn?: boolean
}
