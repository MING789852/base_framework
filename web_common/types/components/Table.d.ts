import {Ref} from "vue";

declare global {
    interface TableModelField {
        tableData:Ref<any[]>,
        selectTableData:Ref<any[]>,
        query:Ref<QueryDefine>,
        uploadMap:Ref<Map<String, File>>,
        deleteFileIdList:Ref<string[]>,
        loading?:Ref<boolean>,
        isAdd?:Ref<boolean>
    }
}