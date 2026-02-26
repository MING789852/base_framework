import type DesFormColumnTypeEnum from "@/enums/DesFormColumnTypeEnum";
import type {Ref} from "vue";
import FormFnClass from "@/class/FormFnClass";
import {ButtonProps} from "@/components/ReDialog/type";

declare global {
    interface DesFormContentDefine {
        // 字段属性名
        prop: string,
        // 标签
        label: string,
        // 类型
        type: DesFormColumnTypeEnum|string|any,
        //列的数量
        colSpan?: number,
        //单元格应该跨越的行数
        rowSpan?: number,
        //列的宽度，不同行相同列的宽度按最大值设定（如无 border ，宽度包含标签与内容）
        width?: string | number,
        //列的最小宽度，与 width 的区别是 width 是固定的，min-width 会把剩余宽度按比例分配给设置了 min-width 的列（如无 border，宽度包含标签与内容）
        minWidth?: string | number,
        // 标签宽度
        labelWidth?:string|number,
        //列的标签对齐方式，若不设置该项，则使用内容的对齐方式（如无 border，请使用 align 参数）
        labelAlign?: 'left' | 'center' | 'right',
        //列的内容对齐方式（如无 border，对标签和内容均生效）
        align?:'left' | 'center' | 'right'
    }

    interface DesFormDefine {
        title?: string,
        showTitle?: boolean,
        size?: '' | 'large' | 'default' | 'small',
        column?: number,
        direction?: 'vertical' | 'horizontal',
        labelWidth?: string | number,
        border?: boolean,
        content: DesFormContentDefine[],
        //其他属性
        [key:string]:any
    }

    interface SaveParams {
        closeDialogCallBack: ()=>void;
        dialogResult:InputDialogResult
    }

    interface OpenInputDialogDefine{
        columns: Array<DetailColumnDefine>,
        dictMapping?: Ref|any,
        defaultValue?: Ref|any,
        deleteFileIdList?: Ref<Array<string>>|Array<string>,
        uploadMap?: Ref<Map<String,File>>|Map<String,File>,
        callBack?: (result: InputDialogResult) => void,
        openDialogCallBack?: ()=>void,
        width?:string,
        footerButtons?: Array<ButtonProps>
        formFn?: FormFnClass
    }

    interface ButtonSelect {
        addButtonName?: (row: any, index: number, propName: string) => string;
        addFn?: (row: any, index: number, propName: string) => void;
        canAddFn?: (row: any, index: number, propName: string) => boolean;
        showEditFn?: (dataIndex: any, dataItem: any,row: any, index: number, propName: string) => boolean;
        editFn?: (dataIndex: any, dataItem: any,row: any, index: number, propName: string) => void;
        //dataIndex和dataItem只有多选时才能获取到数据
        removeFn?: (dataIndex: any, dataItem: any, row: any, index: number, propName: string) => void;
        canRemoveFn?: (dataIndex: any, dataItem: any, row: any, index: number, propName: string) => boolean;
        //dataIndex和dataItem只有多选时才能获取到数据
        showFn?: (dataIndex: any, dataItem: any, row: any, index: number, propName: string) => string;
        isJson?: boolean
    }

    interface ButtonAction {
        buttonName?: (row: any, propName: string,index: number) => string;
        actionFn?: (row: any, propName: string,index: number) => void;
    }

    interface EditAction {
        canEditFn?: (row:any,rowIndex:number,prop:string,other?:any)=>boolean
        textAreaSize?: (row:any,rowIndex:number,prop:string,other?:any)=>{ minRows?: number, maxRows?: number }
    }
}
