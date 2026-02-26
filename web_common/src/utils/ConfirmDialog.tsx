import {addDialog} from "@/components/ReDialog/index";
import {isNullOrUnDef} from "@pureadmin/utils";

function showDialog(msg: String,width?:string) {
  return new Promise((resolve)=>{
    addDialog({
      title: "提示",
      width: isNullOrUnDef(width) ? '50%' : width,
      contentRenderer: () => <p style={{whiteSpace: 'pre-wrap', marginBottom: '10px'}}>{msg}</p>, // jsx 语法 （注意在.vue文件启用jsx语法，需要在script开启lang="tsx"）
      closeCallBack: ({args}) => {
        if (args?.command === "cancel") {
          // reject()
        } else if (args?.command === "sure") {
          resolve(true)
        }
      }
    })
  })
}


export default showDialog
