import {addDialog} from "@/components/ReDialog/index";

function showDialog(msg: String) {
  return new Promise((resolve)=>{
    addDialog({
      title: "提示",
      contentRenderer: () => <p style={{whiteSpace: 'pre-wrap'}}>{msg}</p>, // jsx 语法 （注意在.vue文件启用jsx语法，需要在script开启lang="tsx"）
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
