declare type TcFile = {
    //下面字段是文件默认值
    id?: string,
    originalFileName?: string,
    fileName?: string,
    tempUrl?: string,
    extName?: string,
    createUser?: string,
    createDate?: Date
}

declare interface FileAuthStatusResult {
    status: number,
    //需要新申请
    newApplyFileIdList: string[],
    //已申请
    authFileIdList: string[],
    //申请中
    applyingFileIdList: string[]
}

declare type FileViewActionType = 'PreviewFileComponent' | 'Origin'


declare interface FileActionEvent {
    data: any,
    rowIndex: number,
    propName: string,
    fileIndex: number,
    fileTotalNum: number,
    fileName: string,
    file?: File,
    fileDataProp?: TcFile
}

declare interface ImgProp {
    width?: number | string;
    height?: number | string;
    border?: boolean;
    fit?:'' | 'fill' | 'contain' | 'cover' | 'none' | 'scale-down',
    onlyShow?: boolean;
    toolTip?: boolean;
    uploadColor?: string;
}

declare interface OtherProp{
    data?: any
    dataIndex?: number
    dataPropName?: string
}

declare interface FileAction {
    //修改上传文件名称
    renameUploadFileName?: (event: FileActionEvent) => string
    //上传文件前的校验
    beforeUploadFileFn?: (event: FileActionEvent) => (boolean | Promise<boolean>)
    //上传文件后的处理
    afterUploadFileFn?: (event: FileActionEvent) => void
    //是否允许上传文件
    canUploadFileFn?: (event: FileActionEvent) => boolean
    //是否允许下载文件
    canDownloadFileFn?: (event: FileActionEvent) => boolean
    //是否允许查看文件
    canViewFileFn?: (event: FileActionEvent) => boolean
    //是否允许删除文件
    canDeleteFileFn?: (event: FileActionEvent) => boolean
    //文件预览方式
    fileViewActionTypeFn?: (event: FileActionEvent) => FileViewActionType
    //显示文件名称
    showFileName?: (event: FileActionEvent) => string
}
