import {isNullOrUnDef} from "@pureadmin/utils";

class UploadFileClass{
  // 文件字段
  fileFields:Array<string>;
  // 已上传文件
  uploadMap:UploadMap;
  // 文件集合删除字段名称
  deleteFileFieldName:string;
  // 文件集合上传字段名称
  uploadFileFieldName:string;

  constructor(fileFields:Array<string>,uploadMap:UploadMap,deleteFileFieldName:string,uploadFileFieldName:string) {
    this.fileFields=fileFields
    this.uploadMap=uploadMap
    this.deleteFileFieldName=deleteFileFieldName
    this.uploadFileFieldName=uploadFileFieldName
  }

  removeAllFile = () =>{
    this.uploadMap.clear()
  }

  removeAllFileByRow = (row:any) => {
    for (let i = 0; i < this.fileFields.length; i++) {
      let tcFileList:Array<TcFile> = row[this.fileFields[i]]
      if (!isNullOrUnDef(tcFileList)){
        for (let j = 0; j < tcFileList.length; j++) {
          let uploadMapKey = tcFileList[j].uploadMapKey
          if (!isNullOrUnDef(uploadMapKey)) {
            this.uploadMap.delete(uploadMapKey)
          }
        }
      }
    }
  }

  uploadFn = (tcFileList:Array<TcFile>, scope:any, prop:string) => {
    //判断暂存上传文件key列表是否为空
    if (isNullOrUnDef(scope.row[this.uploadFileFieldName])) {
      scope.row[this.uploadFileFieldName] = []
    }
    //判断存储存储列是否为空
    if (isNullOrUnDef(scope.row[prop])){
      scope.row[prop] = []
    }
    for (let i = 0; i < tcFileList.length; i++) {
      let item = tcFileList[i]
      // 新上传文件建立关系
      scope.row[this.uploadFileFieldName].push(item.uploadMapKey)
      this.uploadMap.set(item.uploadMapKey,item.fileData)
      // 渲染页面
      scope.row[prop].push(item)
    }
  }

  deleteFile = (item:TcFile, scope, prop) => {
    //判断暂存删除文件ID列表是否为空
    if (isNullOrUnDef(scope.row[this.deleteFileFieldName])){
      scope.row[this.deleteFileFieldName] = []
    }
    //判断存储存储列是否为空
    if (isNullOrUnDef(scope.row[prop])) {
      scope.row[prop] = []
    }
    let list:Array<TcFile> = scope.row[prop]
    let deleteFileIdList:Array<string>=scope.row[this.deleteFileFieldName]
    //如果id为空则代表未上传，为临时文件
    if (isNullOrUnDef(item.id)){
      //删除全局上传文件
      this.uploadMap.delete(item.uploadMapKey)
      //渲染页面
      for (let i = 0; i < list.length; i++) {
        if (list[i].uploadMapKey === item.uploadMapKey) {
          list.splice(i,1)
          break
        }
      }
    }else {
      //存储已删除的id
      deleteFileIdList.push(item.id)
      //渲染页面
      for (let i = 0; i < list.length; i++) {
        if (list[i].id === item.id) {
          list.splice(i,1)
          break
        }
      }
    }
  }
}

export default UploadFileClass
