import {VariableDataTypeDict} from "@/enums/TaskStatusEnum";

declare global{
    interface VariableDataProp{
        type?:keyof typeof VariableDataTypeDict,
        key?:string,
        value?:any
    }
}