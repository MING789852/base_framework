type TableType = 'action'|'report'
type SaveOrUpdateType = 'onRow'|'onDialog'

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


declare interface CommonTableButton{
  queryFn?: boolean,
  addFn?: boolean,
  editFn?: boolean,
  saveFn?: boolean,
  deleteFn?: boolean,
  refreshFn?: boolean,
  initQueryFn?: boolean,
  exportExcelFn?: boolean,
  fullscreenFn?: boolean
}
