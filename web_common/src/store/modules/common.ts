import { defineStore } from "pinia";
import { store } from "@/store";
import type {commonType} from "./types";

export const commonStore = defineStore({
  id: "common",
  state: (): commonType => ({
    loading: false,
  }),
  getters: {
    getLoading(state) {
      return state.loading;
    }
  },
  actions: {
    CHANGE_SETTING({ key, value }) {
      if (Reflect.has(this, key)) {
        this[key] = value;
      }
    },
    changeSetting(data) {
      this.CHANGE_SETTING(data);
    }
  }
});

export function useCommonStoreHook() {
  return commonStore(store);
}
