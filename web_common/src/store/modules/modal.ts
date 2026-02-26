import { store } from "@/store";
import { defineStore } from "pinia";
import {isNullOrUnDef} from "@pureadmin/utils";

interface Modal {
    isGlobalDialogVisible:boolean,
    globalDialogComponent:string,
    globalDialogProps:Record<string,any>,
    globalShowClose:boolean
}

export const useModalStore = defineStore({
    id: "pure-modal",
    state: (): Modal => ({
        globalDialogComponent: null,
        globalDialogProps: {},
        globalShowClose: true,
        isGlobalDialogVisible: false
    }),
    actions: {
        showGlobalDialog(componentPath, props = {}) {
            if (isNullOrUnDef(componentPath)){
                return
            }
            this.globalDialogComponent = componentPath;
            this.globalDialogProps = props;
            this.isGlobalDialogVisible = true;
        },
        hideGlobalDialog() {
            this.isGlobalDialogVisible = false;
            this.globalDialogComponent = null;
            this.globalDialogProps = {};
        }
    }
});

export function useModalHook() {
    return useModalStore(store);
}
