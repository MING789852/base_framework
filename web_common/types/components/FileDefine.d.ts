declare type TcFile = {
  //UploadFileClass使用
  uploadMapKey?: string,
  fileData?:File,
  //下面字段是文件默认值
  id?:string,
  originalFileName?:string,
  fileName?:string,
  tempUrl?: string,
  extName?:string
}

declare interface FileAuthStatusResult {
  status:number,
  //需要新申请
  newApplyFileIdList:string[],
  //已申请
  authFileIdList:string[],
  //申请中
  applyingFileIdList: string[]
}

declare type UploadMap = Map<string,File>
