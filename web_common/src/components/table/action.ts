import {computed, reactive, isReactive, ref,useTemplateRef, VNode} from "vue";
import {cloneDeep, debounce, isEmpty, isNullOrUnDef} from "@pureadmin/utils";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import common from '@/utils/common'
import {message} from "@/utils/message";
import {addDialog} from "@/components/ReDialog";
import SearchForm from "@/components/searchForm/searchForm.vue";
import Big from "big.js";
import {TableColumnCtx} from "element-plus";
import {useFullscreen} from "@vueuse/core";
import showDialog from "@/utils/ConfirmDialog";
import TableFnClass from "@/class/TableFnClass";
import FormFnClass from "@/class/FormFnClass";

export interface CommonTableProp {
    version?: 'v1' | 'v2'
    //新增修改类型 onRow、表格上操作  onDialog、弹窗操作
    saveOrUpdateType?: SaveOrUpdateType,
    //表格高度自适应内容
    tableHeightFitContent?: boolean,
    //分页数量
    showPageSizeChange?: boolean,
    pageSizeChangeList?: Array<number>,
    // 展示头部
    showHeader?: boolean,
    // action: 操作表  report: 查询报表
    tableType?: TableType,
    // 默认统计值
    summaryDefault?: Record<number, string>,
    // 忽略统字段
    ignoreSummaryPropList?: Array<string>,
    // 行尾统计方法
    showSummaryFn?: (data: { columns: any[], data: any[] }) => (VNode | string)[],
    // 展示行尾统计
    showSummary?: boolean,
    // 展示行项选择
    showSelection?: boolean,
    // 展示序号
    showSequence?: boolean,
    // 高亮选中行
    highlightCurrentRow?: boolean,
    // 展示分页
    showPage?: boolean,
    // 斑马表格
    stripe?: boolean,
    // 边框
    border?: boolean,
    // 表格高度
    height?: string|number,
    // 按钮是否展示
    tableButtonShow?: boolean,
    tableButton?: CommonTableButton,
    // 查询是否带条件  true 就是row[prop] false就是row[prop+'_$_'+queryCondition]
    // 查询条件在弹窗，默认true
    queryWithCondition?: boolean,
    // 查询条件在标头  默认false
    queryInHeaderWithCondition?: boolean,
    // 查询模板
    queryTemplate?: QueryDefine,
    //查询列定义
    queryColumns?: Array<ColumnDefine>,
    //查询组件是否显示在表头
    queryInHeader?: boolean,
    //列定义
    columns: Array<ColumnDefine>,
    //字典表
    dictList?: Object,
    //api方法
    api?: ApiBase,
    //表格方法const tableFn = new TableFnClass()
    tableFn?: TableFnClass,
    //表单方法
    formFn?: FormFnClass,
    //单元格样式配置
    cellClassNameMap?: Object,
    //行样式配置
    rowClassNameMap?: Object,
    //行列合并配置
    rowColumnSpanMap?: Object,
    //行列合并自动计算0,0
    rowColumnSpanAutoZero?: boolean,
    tableHeight?: number,
    tableWidth?: number,
}

export function useComputedModel(tableDataModel,queryModel){
    const tableData = computed<any[]>({
        get() {
            return tableDataModel.value
        },
        set(value) {
            tableDataModel.value = isReactive(value) ? value : reactive(value)
        }
    })
    const query = computed<QueryDefine>({
        get() {
            return queryModel.value
        },
        set(value) {
            queryModel.value = isReactive(value) ? value : reactive(value)
        }
    })
    return{
        tableData,
        query
    }
}

export function useTableData(props:any,modelFiled:TableModelField) {
    const selectTableData = modelFiled.selectTableData
    const tableData = modelFiled.tableData
    const query = modelFiled.query
    const uploadMap = modelFiled.uploadMap
    const deleteFileIdList = modelFiled.deleteFileIdList
    const isAdd = modelFiled.isAdd??ref(false)
    const loading = modelFiled.loading??ref(false)
    //数据字典计算
    const dictMapping = computed(() => {
        if (isNullOrUnDef(props.dictList)) {
            return {}
        } else {
            return props.dictList
        }
    })
    //表格头按钮展示计算
    const tableButtonComputed = computed(() => {
        let tableButton = cloneDeep(props.tableButton)
        if (!props.queryInHeader) {
            tableButton.initQueryFn = false
        }
        return tableButton
    })
    //表格列计算
    const columnsDisableEdit = (list: ColumnDefine[]) => {
        let newColumns = []
        list.forEach(value => {
            let newValue: ColumnDefine = cloneDeep(value)
            let newType
            if ([ColumnTypeEnum.EDIT_INPUT, ColumnTypeEnum.INPUT].map(item => item.toString()).includes(value.type)) {
                newType = ColumnTypeEnum.COMMON
            }
            if ([ColumnTypeEnum.EDIT_INPUT_AREA, ColumnTypeEnum.AREA_INPUT].map(item => item.toString()).includes(value.type)) {
                newType = ColumnTypeEnum.COMMON_OVER
            }
            if ([ColumnTypeEnum.EDIT_OPTION, ColumnTypeEnum.OPTION].map(item => item.toString()).includes(value.type)) {
                newType = ColumnTypeEnum.DICT_COMMON
            }
            if ([ColumnTypeEnum.EDIT_DATE, ColumnTypeEnum.DATE].map(item => item.toString()).includes(value.type)) {
                newType = ColumnTypeEnum.DATE_COMMON
            }
            if ([ColumnTypeEnum.LEVEL].map(item => item.toString()).includes(value.type)) {
                newValue.children = columnsDisableEdit(value.children)
            }
            if (!isNullOrUnDef(newType)) {
                newValue.type = newType
            }
            newColumns.push(newValue)
        })
        return newColumns
    }
    const columnsComputed = computed(() => {
        if (props.saveOrUpdateType === 'onRow') {
            //修改在表格，则无需处理
            return props.columns
        } else {
            //修改在弹窗，则需要禁用表格修改
            return columnsDisableEdit(props.columns)
        }
    })
    //默认转换columns为弹窗详情DetailColumnDefine
    const saveOrUpdateDialogColumnsComputed = computed(() => {
        let saveOrUpdateDialogDetailColumns: Array<DetailColumnDefine> = []
        if (props.saveOrUpdateType === 'onDialog') {
            saveOrUpdateDialogDetailColumns = common.convertColumnTypeToDialogType(props.columns)
        }
        return saveOrUpdateDialogDetailColumns
    })
    //分页布局计算
    const pageLayoutComputed = computed(() => {
        if (props.showPageSizeChange) {
            return 'total,sizes,prev,pager,next'
        } else {
            return 'total,prev,pager,next'
        }
    })

    const exportExcelFn = () => {
        if (isNullOrUnDef(props.tableFn.exportExcelFn)) {
            let data = {
                columnPropsList: props.columns,
                conditions: query.value
            }
            if (props.api?.exportExcel) {
                common.showGlobalLoading()
                props.api.exportExcel(data).then((res: Blob) => {
                    common.handleBlob(res)
                }).catch(() => {
                    message('请求失败', {type: 'error'})
                }).finally(() => {
                    common.closeGlobalLoading()
                })
            }
        } else {
            props.tableFn.exportExcelFn()
        }
    }
    const cellStyleFn = (data: { row: any, column: any, rowIndex: number, columnIndex: number })=>{
        return isNullOrUnDef(props.tableFn.cellStyle)?{}:props.tableFn.cellStyle(data)

    }
    const headerCellStyleFn = (data: { row: any, column: any, rowIndex: number, columnIndex: number })=>{
        return isNullOrUnDef(props.tableFn.headerCellStyle)?{}:props.tableFn.headerCellStyle(data)
    }
    const rowStyleFn = (data: { row: any, rowIndex: number }) => {
       return  isNullOrUnDef(props.tableFn.rowStyle)?{}:props.tableFn.rowStyle(data)
    }
    const rowClassNameFn = (data: { row: any, rowIndex: number }) => {
        if (isNullOrUnDef(props.tableFn.rowClassName)) {
            if (isNullOrUnDef(props.rowClassNameMap)) {
                return ''
            } else {
                let rowIndex = data.rowIndex
                let tableRowClassName = props.rowClassNameMap[rowIndex]
                if (isNullOrUnDef(tableRowClassName)) {
                    return ''
                } else {
                    return tableRowClassName
                }
            }
        } else {
            return props.tableFn.rowClassName(data)
        }
    }
    const cellClassNameFn: (data: {
        row: any,
        column: any,
        rowIndex: number,
        columnIndex: number
    }) => string = (data) => {
        if (isNullOrUnDef(props.tableFn.cellClassName)) {
            //如果样式列表为空，直接返回
            let cellClassNameMap: Object = props.cellClassNameMap
            if (Object.keys(cellClassNameMap).length === 0) {
                return ''
            }
            //如果带有选择框则需要列定位需要再-1
            let columnIndex = data.columnIndex
            if (props.showSelection) {
                columnIndex = data.columnIndex - 1
            }
            let key = data.rowIndex + '_' + columnIndex
            let className: string = cellClassNameMap[key]
            if (!isNullOrUnDef(className)) {
                return className
            } else {
                return ''
            }
        } else {
            return props.tableFn.cellClassName(data)
        }
    }
    let rowColumnSpanAutoZero = {}
    const spanMethod = (data: { row: any, column: any, rowIndex: number, columnIndex: number }) => {
        if (isNullOrUnDef(props.tableFn.cellSpanMethod)) {
            if (isNullOrUnDef(props.rowColumnSpanMap)) {
                return [1, 1]
            } else {
                let rowIndex = data.rowIndex
                let columnIndex = data.columnIndex
                if (props.rowColumnSpanAutoZero) {
                    if (rowIndex === 0 && columnIndex === 0) {
                        // 初始化自动行列合并[0,0]映射
                        rowColumnSpanAutoZero = {}
                    }
                    let zeroSpan = rowColumnSpanAutoZero[rowIndex + "_" + columnIndex]
                    if (!isNullOrUnDef(zeroSpan)) {
                        return zeroSpan
                    } else {
                        let rowColumnSpan = props.rowColumnSpanMap[rowIndex + "_" + columnIndex]
                        if (isNullOrUnDef(rowColumnSpan)) {
                            return {
                                rowspan: 1,
                                colspan: 1,
                            }
                        } else {
                            let rowspan = rowColumnSpan.rowspan
                            let colspan = rowColumnSpan.colspan
                            //自动处理[0,0]
                            if (rowspan > 1 || colspan > 1) {
                                for (let i = rowIndex; i < rowIndex + rowspan; i++) {
                                    for (let j = columnIndex; j < columnIndex + colspan; j++) {
                                        if (i === rowIndex && j === columnIndex) {
                                            continue
                                        }
                                        rowColumnSpanAutoZero[i + "_" + j] = {
                                            rowspan: 0,
                                            colspan: 0,
                                        }
                                    }
                                }
                            }
                            return rowColumnSpan
                        }
                    }
                } else {
                    let rowColumnSpan = props.rowColumnSpanMap[rowIndex + "_" + columnIndex]
                    if (isNullOrUnDef(rowColumnSpan)) {
                        return {
                            rowspan: 1,
                            colspan: 1,
                        }
                    } else {
                        return rowColumnSpan
                    }
                }
            }
        } else {
            return props.tableFn.cellSpanMethod(data)
        }
    }
    const summaryMethod = (data: { columns: any[], data: any[] }) => {
        if (isNullOrUnDef(props.showSummaryFn)) {
            let summaryArr = []
            //无数据或者无列配置时，默认不处理
            if (Object.keys(data.data).length === 0 || Object.keys(data.columns).length === 0) {
                return summaryArr
            }
            //初始化数组
            props.columns.forEach(() => {
                summaryArr.push('')
            })
            //数据统计的列映射
            let indexMapping = new Map<string, number>()
            //把level类型都拍成一级
            let indexColumns = common.columnsToIndexColumn(props.columns)
            //过滤掉不展示的数据
            indexColumns = indexColumns.filter((value) => {
                if (isNullOrUnDef(value.show)) {
                    return true
                }
                return value.show
            })
            //处理固定列（处于最前面）
            let columnIndex = 0
            indexColumns.forEach((value) => {
                if (value.fixed) {
                    indexMapping.set(value.prop, columnIndex)
                    columnIndex++
                }
            })
            //处理普通列 (在固定列后面)
            indexColumns.forEach((value) => {
                if (!value.fixed) {
                    indexMapping.set(value.prop, columnIndex)
                    columnIndex++
                }
            })
            //执行数据统计
            indexMapping.forEach((index, prop) => {
                //不需要统计列的数据
                if (props.ignoreSummaryPropList.includes(prop)) {
                    summaryArr[index] = ''
                    return
                }
                //需要统计列的数据
                let sum = new Big(0);
                let isNumber = true
                for (let i = 0; i < data.data.length; i++) {
                    let item = data.data[i]
                    if (!isNullOrUnDef(item[prop])) {
                        if (common.isString(item[prop]) && common.isStrBlank(item[prop])) {
                        } else {
                            if (common.isNumber(item[prop])) {
                                sum = sum.add(new Big(item[prop]))
                            } else if (common.isString(item[prop]) && common.isNumberStr(item[prop])) {
                                try {
                                    sum = sum.add(new Big(item[prop]))
                                } catch (e) {
                                    console.log("【summaryMethod】字符串转换数字错误：" + item[prop])
                                    sum = sum.add(new Big(0))
                                }
                            } else {
                                isNumber = false
                            }
                        }
                    }
                }
                if (!isNumber) {
                    summaryArr[index] = ''
                } else {
                    summaryArr[index] = sum.toString()
                }
            })
            Object.keys(props.summaryDefault).forEach((value) => {
                summaryArr[value] = props.summaryDefault[value]
            })
            return summaryArr
        } else {
            return props.showSummaryFn(data)
        }
    }
    const rowDblclick = (row: any, column: any, event: Event) => {
        // 行点击事件处理
        props?.tableFn?.rowDblclick?.(row, column, event)
    }
    const rowClick = (row: any, column: any, event: Event) => {
        // 行点击事件处理
        props?.tableFn?.rowClick?.(row, column, event)
    }
    const cellDblclick = (row: any, column: any, cell: HTMLTableCellElement, event: Event) => {
        // 单元格点击事件处理
        props?.tableFn?.cellDblclick?.(row, column, cell, event)
    }
    const cellClick = (row: any, column: any, cell: HTMLTableCellElement, event: Event) => {
        props?.tableFn?.cellClick?.(row, column, cell, event)
    }

    const sortChange = (data: { column: any, prop: string, order: any }) => {
        // 排序事件处理
        if (!isNullOrUnDef(props.tableFn.sortChange)) {
            props.tableFn.sortChange(data)
        } else {
            //初始化
            query.value.orderByParams = {}
            //判断是否需要排序
            if (isNullOrUnDef(data.order)) {
                delete query.value.orderByParams[data.column.property]
            } else {
                query.value.orderByParams[data.column.property] = data.order
            }
            getData()
        }
    }
    const handleSelectionChange = (val: Array<Object>) => {
        if (!isNullOrUnDef(props.tableFn.handleSelectionChange)) {
            props.tableFn.handleSelectionChange(val)
        } else {
            selectTableData.value = val
        }
    }
    const initQuery = () => {
        if (!isNullOrUnDef(props.tableFn.initQuery)) {
            props.tableFn.initQuery()
        } else {
            let template: QueryDefine = cloneDeep(props.queryTemplate)
            query.value.queryParams = template.queryParams
            query.value.current = template.current
            query.value.size = template.size
        }
    }
    const getData = () => {
        if (!isNullOrUnDef(props.tableFn.getData)) {
            props.tableFn.getData()
        } else {
            if (isNullOrUnDef(props.api)) {
                return
            }
            loading.value = true
            if (!isNullOrUnDef(props.tableFn.handleQuery)) {
                props.tableFn.handleQuery()
            }
            let promise
            if (props.showPage) {
                promise = props.api.selectByPage(query.value)
            } else {
                promise = props.api.selectByList(query.value)
            }
            promise.then((res: any) => {
                if (props.showPage) {
                    if (res.code === 200) {
                        query.value.current = res.data.current
                        query.value.total = res.data.total
                        query.value.size = res.data.size
                        tableData.value = res.data.records
                    } else {
                        message(res.msg, {type: 'error'})
                    }
                } else {
                    if (res.code === 200) {
                        tableData.value = res.data
                    } else {
                        message(res.msg, {type: 'error'})
                    }
                }
            }).catch(res => {
                console.log(res)
                message('请求失败', {type: 'error'})
            }).finally(() => {
                loading.value = false
                isAdd.value = false
            })
        }
    }
    const openQueryFn = () => {
        addDialog({
            top: '5vh',
            width: "60%",
            title: "查询",
            props: {
                columns: props.queryColumns,
                query: query,
                dictList: dictMapping,
                queryWithCondition: props.queryWithCondition
            },
            contentRenderer: () => SearchForm,
            footerButtons: [
                {
                    label: "清空",
                    text: true,
                    bg: true,
                    type: "info",
                    btnClick: ({dialog: {options, index}, button}) => {
                        initQuery()
                    }
                }
            ],
            closeCallBack: ({options, args}) => {
                query.value = options.props.query as QueryDefine
                if (args?.command === "cancel") {
                    // 您点击了取消按钮
                    message(`取消查询`);
                } else if (args?.command === "sure") {
                    // 查询重置第一页
                    query.value.current = 1
                    getData()
                } else {
                    message(
                        `关闭查询窗口`
                    );
                }
            }
        });
    }
    const queryFn = () => {
        if (!isNullOrUnDef(props.tableFn.queryFn)) {
            props.tableFn.queryFn()
        } else {
            if (props.queryInHeader) {
                // 查询重置第一页
                query.value.current = 1
                getData()
            } else {
                if (!isNullOrUnDef(props.tableFn.beforeQueryDialogOpenFn)) {
                    props.tableFn.beforeQueryDialogOpenFn().then(() => {
                        openQueryFn()
                    })
                } else {
                    openQueryFn()
                }
            }
        }
    }
    //callBack函数用于关闭弹窗
    const saveFn = (row?: any, saveParams?: SaveParams) => {
        if (!isNullOrUnDef(props.tableFn.saveFn)) {
            if (props.saveOrUpdateType === 'onRow') {
                props.tableFn.saveFn()
            }
            if (props.saveOrUpdateType === 'onDialog') {
                props.tableFn.saveFn(row, saveParams)
            }
        } else {
            loading.value = true
            let data
            if (props.saveOrUpdateType === 'onRow') {
                data = tableData.value
            }
            if (props.saveOrUpdateType === 'onDialog') {
                data = [row]
            }
            props.api.saveOrUpdateData(data).then((res: any) => {
                let code = res.code
                let msg = res.msg
                if (code === 200) {
                    message('操作成功', {type: 'success'})
                    if (!isNullOrUnDef(saveParams)) {
                        saveParams.closeDialogCallBack?.()
                    }
                    getData()
                } else {
                    message(msg, {type: 'error'})
                }
            }).catch(res => {
                console.log(res)
            }).finally(() => {
                loading.value = false
            })
        }
    }
    const openEditDialog = (row) => {
        let dialogColumns = saveOrUpdateDialogColumnsComputed.value
        if (props.formFn.handleDialogColumns) {
            dialogColumns = props.formFn.handleDialogColumns(cloneDeep(saveOrUpdateDialogColumnsComputed.value))
        }
        let params: OpenInputDialogDefine = {
            columns: dialogColumns,
            defaultValue: row,
            dictMapping: dictMapping,
            formFn: props.formFn,
            uploadMap: uploadMap,
            deleteFileIdList: deleteFileIdList,
            callBack: (result: InputDialogResult) => {
                let saveParams: SaveParams = {
                    closeDialogCallBack: () => {
                        result.done()
                    },
                    dialogResult: result
                }
                saveFn(result.data, saveParams)
            }
        }
        common.openInputDialog(params)
    }
    const addFn = () => {
        if (!isNullOrUnDef(props.tableFn.addFn)) {
            props.tableFn.addFn()
        } else {
            if (props.saveOrUpdateType === 'onRow') {
                let item = {
                    add: true
                }
                isAdd.value = true
                tableData.value.unshift(item);
            }
            if (props.saveOrUpdateType === 'onDialog') {
                openEditDialog({})
            }
        }
    }
    const editFn = () => {
        if (selectTableData.value.length !== 1) {
            return message('请选择一条数据进行编辑', {type: 'warning'})
        }
        let originRow = selectTableData.value[0]
        let cloneRow = cloneDeep(originRow)
        if (props.tableFn.editFn) {
            props.tableFn.editFn(cloneRow, originRow)
        } else {
            openEditDialog(cloneRow)
        }
    }
    const deleteFn = () => {
        if (!isNullOrUnDef(props.tableFn.deleteFn)) {
            props.tableFn.deleteFn(selectTableData.value)
        } else {
            if (isNullOrUnDef(selectTableData.value) || isEmpty(selectTableData.value)) {
                message('请选择需要删除的数据后操作', {type: 'warning'})
            } else {
                showDialog('是否确认删除').then(() => {
                    loading.value = true
                    props.api.deleteData(selectTableData.value).then((res: any) => {
                        let code = res.code
                        let msg = res.msg
                        if (code === 200) {
                            if (isAdd.value) {
                                //前端界面删除
                                for (let i = 0; i < selectTableData.value.length; i++) {
                                    //删除数据
                                    common.removeListItem(tableData.value, selectTableData.value[i])
                                }
                            } else {
                                getData()
                            }
                            message('操作成功', {type: 'success'})
                        } else {
                            message(msg, {type: 'error'})
                        }
                    }).catch(res => {
                        console.log(res)
                    }).finally(() => {
                        loading.value = false
                    })
                })
            }
        }
    }
    const refreshFn = () => {
        props.tableFn?.refreshFn?.() || getData()
    }


    // 当前 Tooltip 显示的内容
    const currentTooltipContent = ref('')
    // 虚拟触发器的引用配置
    // const triggerRef = ref<HTMLElement>(null)
    const cellMouseEnter = (row: any, column: TableColumnCtx<any>, cell: HTMLTableCellElement, event: Event) => {
        let data = {row: row, column: column, prop: column.property, position: "cell" as any}
        if (props.tableFn?.showToolTip?.(data) ?? false) {
            debounce(() => {
                currentTooltipContent.value = props.tableFn?.getToolTipContent?.(data) ?? ''
                // triggerRef.value = cell
                // message(currentTooltipContent.value, { type: 'info', duration: 5000 })
            }, 2000, true)()
        }
    }
    const containerRef = useTemplateRef<HTMLElement>('containerRef')
    const {isFullscreen, enter, exit, toggle} = useFullscreen(containerRef)
    const fullscreenFn = async () => {
        if (isFullscreen.value) {
            await exit()
        } else {
            await toggle()
        }
    }

    return {
        props,
        uploadMap,
        deleteFileIdList,
        tableData,
        query,
        selectTableData,
        isAdd,
        loading,
        dictMapping,
        tableButtonComputed,
        columnsComputed,
        exportExcelFn,
        cellStyleFn,
        headerCellStyleFn,
        rowStyleFn,
        rowClassNameFn,
        cellClassNameFn,
        spanMethod,
        summaryMethod,
        rowDblclick,
        rowClick,
        cellDblclick,
        cellClick,
        sortChange,
        handleSelectionChange,
        initQuery,
        getData,
        openQueryFn,
        queryFn,
        saveFn,
        openEditDialog,
        cellMouseEnter,
        pageLayoutComputed,
        isFullscreen,
        addFn,
        editFn,
        deleteFn,
        refreshFn,
        fullscreenFn
    }
}