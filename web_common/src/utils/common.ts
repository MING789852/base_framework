import {buildUUID, debounce, isBoolean, isNullOrUnDef, useCopyToClipboard} from "@pureadmin/utils";
import {addDialog} from "@/components/ReDialog/index";
import PreviewFile from "@/components/PreviewFile/PreviewFile.vue";
import DetailForm from "@/components/detailForm/detailForm.vue";
import {nextTick, type Ref, ref} from "vue";
import {message} from "@/utils/message";
import {getUserInfo} from "@/utils/auth";
import {ElLoading} from "element-plus";
import showDialog from "@/utils/ConfirmDialog";
import {addDays, endOfDay, format, parse, startOfDay, startOfYear} from 'date-fns';
import 'viewerjs/dist/viewer.css'
import {api as viewerApi} from "v-viewer"
import router from "@/router";
import FileHashWorker from '@/utils/fileHash?worker'
import fileApi from '@/api/fileApi'
import fileApplyApi from '@/api/fileApplyApi'
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {useTimeoutFn} from "@vueuse/core";
let env = import.meta.env
const {copied, update} = useCopyToClipboard();

// https://cn.vitejs.dev/guide/features.html#glob-import
//处理业务模块
const rootViewModulesRoutes = import.meta.glob("/src/views/**/*.{vue,tsx}");
const rootComponentModulesRoutes = import.meta.glob("/src/components/**/*.{vue,tsx}");
//处理公共模块
const unHandleCommonViewModulesRoutes = import.meta.glob("@/views/**/*.{vue,tsx}");
const unHandleCommonComponentModulesRoutes = import.meta.glob("@/components/**/*.{vue,tsx}");
const commonViewModulesRoutes: Record<string, () => Promise<unknown>> = {}
const commonComponentModulesRoutes: Record<string, () => Promise<unknown>> = {}
Object.keys(unHandleCommonViewModulesRoutes).forEach((value) => {
    commonViewModulesRoutes[value.replace('../web_common', '')] = unHandleCommonViewModulesRoutes[value]
})
Object.keys(unHandleCommonComponentModulesRoutes).forEach((value) => {
    commonComponentModulesRoutes[value.replace('../web_common', '')] = unHandleCommonComponentModulesRoutes[value]
})

//合并两个模块
const allComponentVueList = {...commonComponentModulesRoutes, ...rootComponentModulesRoutes}
const allViewVueList = {...commonViewModulesRoutes, ...rootViewModulesRoutes}
const allVueList = {...allComponentVueList, ...allViewVueList}

class Common {

    public static instance: Common;

    private loadingInstance;

    private isLoading = false;

    private routerActionMapping;

    private isNumberReg = /^([+-]?(0|([1-9]\d*)))(\.\d+)?$/

    private isMobileReg = /(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i

    constructor() {
        console.log('common初始化')
    }

    public static getInstance() {
        if (Common.instance === undefined || Common.instance === null) {
            Common.instance = new Common()
        }
        return Common.instance
    }

    formatDate(date: Date | number, formatStr: string): string {
        return format(date, formatStr);
    }

    addDays(date: Date | number, amount: number) {
        return addDays(date, amount)
    }

    startOfYearStr(dateStr: string, formatStr: string) {
        return this.formatDate(startOfYear(parse(dateStr, formatStr, new Date())), formatStr)
    }

    startOfDay(date: Date | number) {
        return startOfDay(date)
    }

    endOfDay(date: Date | number) {
        return endOfDay(date)
    }

    getCurrentRouteQuery() {
        return router.currentRoute.value.query
    }

    getFileExtName(fileName: string) {
        const splitFileName = fileName.split('.')
        //获取末尾后缀
        return splitFileName[splitFileName.length - 1].toLowerCase()
    }

    getMonthStartAndEnd(date = new Date()) {
        const year = date.getFullYear();
        const month = date.getMonth(); // 从0开始的月份索引
        const day = 1; // 月初
        const start = new Date(year, month, day);

        const nextMonth = new Date(year, month + 1, 0); // 下个月的天数
        const end = new Date(year, month, nextMonth.getDate()); // 当前月份的月尾日期

        return {
            start,
            end
        };
    }

    isMobile() {
        return this.isMobileReg.test(navigator.userAgent)
    }

    showImg(index: number, imageList: any [], hidden?: (event: CustomEvent) => void) {
        viewerApi({
            options: {
                initialViewIndex: index,
                fullscreen: true,
                zIndex: 3999,
                hidden: isNullOrUnDef(hidden) ? () => {
                } : hidden
            },
            images: imageList
        })
    }

    getAllViewVue() {
        return allViewVueList
    }

    getAllVue() {
        return allVueList
    }

    getVue(key: string) {
        return allVueList[key]
    }

    selectIndex(list: Array<any>, item: any) {
        for (let i = 0; i < list.length; i++) {
            if (list[i] === item) {
                return i
            }
        }
        return -1
    }

    removeListItem(list: Array<any>, item: any) {
        let index = this.selectIndex(list, item)
        if (index !== -1) {
            list.splice(index, 1)
        }
    }

    reverseDict(dict: any) {
        let newDict = {}
        Object.keys(dict).forEach((value) => {
            newDict[dict[value]] = value
        })
        return newDict
    }

    isNumberStr(value: string) {
        if (isNullOrUnDef(value)) {
            return false
        }
        return this.isNumberReg.test(value)
    }

    isEmptyArr(value: any[]) {
        if (isNullOrUnDef(value) || value.length === 0) {
            return true
        } else {
            return false
        }
    }

    isStrBlank(value: string) {
        if (isNullOrUnDef(value)) {
            return true
        } else {
            return value.trim() === '';
        }
    }

    isNumber(value: any) {
        return typeof value === "number";
    }

    isString(value: any) {
        return typeof value === "string";
    }

    copyText(text: string) {
        update(text)
        return copied.value
    }

    showGlobalLoading() {
        if (this.isLoading) {
            return
        }
        this.loadingInstance = ElLoading.service({fullscreen: true, body: true, customClass: 'pure-loading'})
        this.isLoading = true
    }

    judgeIsLoading() {
        return this.isLoading
    }

    updateGlobalLoadingText(text: string) {
        if (this.judgeIsLoading()) {
            this.loadingInstance.setText(text)
        }
    }

    async closeGlobalLoading() {
        // this.loadingInstance.close()
        // this.isLoading = false
        await nextTick()
        this.loadingInstance.close()
        this.isLoading = false
    }

    handleResponsePage(tableRefValue: any, data: any) {
        tableRefValue.query.current = data.current
        tableRefValue.query.total = data.total
        tableRefValue.query.size = data.size
        tableRefValue.tableData = data.records
    }

    handleRequestApi(request: Promise<any>) {
        return new Promise<HttpResult>((resolve) => {
            this.showGlobalLoading()
            request.then((res: HttpResult) => {
                if (res.code === 200) {
                    resolve(res)
                } else {
                    message(res.msg, {type: 'error'})
                }
            }).finally(() => {
                this.closeGlobalLoading()
            })
        })
    }

    getCurrentUserInfo() {
        return getUserInfo()
    }

    initRouterActionMapping(routerActionMapping) {
        this.routerActionMapping = routerActionMapping
    }

    authActionCode(routerActionCode: string): boolean {
        var currentUserInfo = this.getCurrentUserInfo();
        if (isNullOrUnDef(currentUserInfo)) {
            return false
        }
        if (currentUserInfo.username === 'admin') {
            return true
        }
        let routerName = router.currentRoute.value.name
        if (isNullOrUnDef(this.routerActionMapping)) {
            return false
        }
        let routerAction = this.routerActionMapping[routerName]
        if (isNullOrUnDef(routerAction)) {
            return false
        }
        let action = routerAction[routerActionCode]
        return !isNullOrUnDef(action);
    }

    authUserButton(userNameList: Array<string>): boolean {
        let userInfo = this.getCurrentUserInfo()
        if (isNullOrUnDef(userInfo)) {
            return false
        }
        if (userInfo?.username === 'admin') {
            return true
        }
        if (!this.isStrBlank(userInfo.username) && !this.isEmptyArr(userNameList)) {
            if (userNameList.includes(userInfo.username)) {
                return true
            }
        }
        return false
    }

    authRoleButton(roleList: Array<string>): boolean {
        let userInfo = this.getCurrentUserInfo()
        if (isNullOrUnDef(userInfo)) {
            return false
        }
        if (userInfo?.username === 'admin') {
            return true
        }
        if (!this.isEmptyArr(userInfo.roles) && !this.isEmptyArr(roleList)) {
            return userInfo.roles.filter(item => roleList.includes(item)).length > 0;
        }
        return false
    }

    handleBlob(res: Blob, downloadName?: string) {
        if (res.type === 'application/json') {
            const file = new FileReader();
            file.readAsText(res, 'utf-8');
            file.onload = function () {
                const json = JSON.parse(file.result as string);
                message(json.msg, {type: 'error'})
                return json;
            }
        } else {
            try {
                let url = this.createObjectURLByFile(res)
                if (isNullOrUnDef(downloadName)) {
                    this.downLoadFileByUrl(url, '导出.xlsx')
                } else {
                    this.downLoadFileByUrl(url, downloadName)
                }
                this.revokeObjectURLByUrl(url)
            } catch (e) {
                message('Blob对象下载失败', {type: 'error'})
            }
        }
    }

    /**
     * @param object 修改的对象
     * @param path 属性路径的字符串，使用点（.）作为层级之间的分隔符,支持a.b[0]
     * @param value 要设置的值
     */
    setNestedProperty(object: Object, path: string, value: any) {
        // 若有 '[',']'，则替换成将 '[' 替换成 '.',去掉 ']'
        let pathList = path.replace(/\[/g, '.').replace(/]/g, '').split('.')
        if (typeof object !== 'object') return object;
        pathList.reduce((o, k, i, _) => {
            if (i === _.length - 1) { // 若遍历结束直接赋值
                o[k] = value
                return null
            } else if (k in o) { // 若存在对应路径，则返回找到的对象，进行下一次遍历
                return o[k]
            } else { // 若不存在对应路径，则创建对应对象，若下一路径是数字，新对象赋值为空数组，否则赋值为空对象
                o[k] = /^[0-9]{1,}$/.test(_[i + 1]) ? [] : {}
                return o[k]
            }
        }, object)
        // 返回object；
        return object;
    }

    /**
     * @param object 对象
     * @param path 属性路径的字符串，使用点（.）作为层级之间的分隔符
     * @param defaultValue
     */
    getNestedProperty(object: Object, path: string, defaultValue?: any) {
        // 若有 '[',']'，则替换成将 '[' 替换成 '.',去掉 ']'
        let pathList = path.replace(/\[/g, '.').replace(/]/g, '').split('.')
        // 判断 object 是否是数组或者对象，否则直接返回默认值 defaultValue
        if (typeof object !== 'object') return defaultValue
        // 沿着路径寻找到对应的值，未找到则返回默认值 defaultValue
        return pathList.reduce((o, k) => (o || {})[k], object) || defaultValue
    }

    openInputDialog(columns: Array<DetailColumnDefine>, dictList: any, defaultValue?: Ref<any>,
                    callBack?: (result: InputDialogResult) => void,
                    openDialogCallBack?: ()=>void) {
        let data: any
        if (isNullOrUnDef(defaultValue)) {
            data = ref({})
        } else {
            data = defaultValue
        }
        if (isNullOrUnDef(dictList)) {
            dictList = {}
        }
        let width
        if (this.isMobile()) {
            width = "90%"
        } else {
            width = "30%"
        }
        console.log(width)
        addDialog({
            width: width,
            title: "操作",
            props: {
                columns: columns,
                propData: data,
                dictList: dictList
            },
            contentRenderer: () => DetailForm,
            beforeSure(done, {options}) {
                let data: any = options.props.propData
                let result: InputDialogResult = {
                    data: data,
                    done: done
                }
                callBack(result)
            },
            open({ options, index }) {
                if (!isNullOrUnDef(openDialogCallBack)) {
                    openDialogCallBack()
                }
            }
        })
    }

    showMsgDialog(msg: String) {
        return showDialog(msg)
    }


    uploadFile(callBack: (e: Event) => void, multiple: boolean, accept?: string) {
        let inputObj = document.createElement('input')
        inputObj.type = 'file'
        if (!isNullOrUnDef(accept)) {
            inputObj.accept = accept
        }
        inputObj.multiple = multiple
        inputObj.onchange = (e: Event) => {
            callBack(e)
        }
        inputObj.click()
    }


    createObjectURLByFile(file: Blob | MediaSource) {
        return window.URL.createObjectURL(file)
    }


    revokeObjectURLByUrl(url: string) {
        return window.URL.revokeObjectURL(url)
    }

    readBlobText(blob:Blob,callBack:(text)=>void){
        if (isNullOrUnDef(blob)){
            callBack('')
            return
        }
        if (!(blob instanceof Blob)){
            callBack('')
            return
        }
        const reader = new FileReader();
        reader.onload = (e)=>{
            const text = e.target.result
            callBack(text)
        }
        reader.readAsText(blob,'UTF-8')
    }

    getTempFilePrefix() {
        return 'temp_'
    }

    downLoadByFetch(url: string, body?: any, downloadName?: string){
        const controller = new AbortController();
        return new Promise<any|boolean>((resolve)=>{
            let fetchRequest: Promise<Response>
            if (!isNullOrUnDef(body)){
                fetchRequest = fetch(url, {
                    method: 'POST',
                    //携带cookie
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json' // 设置头部内容类型为JSON
                    },
                    body: JSON.stringify(body),
                    signal: controller.signal
                })
            }else {
                fetchRequest = fetch(url,{
                    method: 'GET',
                    //携带cookie
                    credentials: 'include',
                    signal: controller.signal
                })
            }
            fetchRequest.then(response => {
                const contentType = response.headers.get("content-type")
                if (contentType === 'application/json') {
                    response.json().then(res=>{
                        resolve(res)
                    })
                } else {
                    if (isNullOrUnDef(body)){
                        //get请求使用中断请求方式再使用a标签下载
                        controller.abort('')
                        this.downLoadFileByUrl(url,downloadName)
                        resolve(true)
                    }else {
                        //使用blob下载，并显示进度
                        this.showGlobalLoading()
                        //进度条相关
                        //获取 Response 对象的 body 属性的读取器（reader）。body 属性是一个可读的流（ReadableStream），可以用来读取响应体的数据。
                        const reader = response.body.getReader();
                        //总大小
                        const contentLength = +response.headers.get('Content-Length');
                        //已下载
                        let receivedLength = 0;
                        let chunks = [];
                        // 读取一个数据块。这个方法返回一个 Promise，当一个数据块被读取时，这个 Promise 就会解析为一个包含 done 和 value 属性的对象。done 属性表示是否已经读取完所有数据，value 属性是一个 Uint8Array，包含了读取到的数据块。
                        const processChunk = ({ done, value }:{done:boolean,value:Uint8Array|undefined}) => {
                            if (done) {
                                this.updateGlobalLoadingText(`下载完成`)
                                // 将 chunks 数组中的所有数据块复制到一个新的 Uint8Array 中，然后使用这个 Uint8Array 创建一个 Blob 对象
                                let data = new Uint8Array(receivedLength);
                                let position = 0;
                                for(let chunk of chunks) {
                                    data.set(chunk, position);
                                    position += chunk.length;
                                }
                                return new Blob([data]);
                            }
                            // 将读取到的数据块添加到 chunks 数组中。
                            chunks.push(value);
                            // 更新已接收的数据长度。
                            receivedLength += value.length;
                            this.updateGlobalLoadingText(`已下载：${receivedLength}，总大小：${contentLength}`)
                            // 递归调用 reader.read()，直到读取完所有数据。
                            return reader.read().then(processChunk);
                        }
                        return reader.read().then(processChunk).then(res=>{
                            let url = this.createObjectURLByFile(res)
                            if (this.isStrBlank(downloadName)){
                                const contentDisposition = response.headers.get("content-disposition")
                                let fileName = contentDisposition.substring(contentDisposition.indexOf('filename')).split('=')[1]
                                fileName = fileName.trim()
                                fileName = decodeURIComponent(fileName)
                                this.downLoadFileByUrl(url,fileName)
                            }else {
                                this.downLoadFileByUrl(url,downloadName)
                            }
                            this.revokeObjectURLByUrl(url)
                            resolve(true)
                        })
                    }
                }
            })
        })
    }

    downLoadFileByUrl(url: string, downloadName?: string) {
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        //新标签中打开
        link.target = '_blank'
        if (!this.isStrBlank(downloadName)) {
            link.download = downloadName
        }
        link.click()
        link.remove()
    }

    downLoadFileByUrlWithFileAuth(url: string, body?: any, downloadName?: string){
        debounce(()=>{
            this.showGlobalLoading()
            this.downLoadByFetch(url,body,downloadName).then(res=>{
                if (!isBoolean(res)){
                    let httpResult:HttpResult = res
                    if (httpResult.code === 400){
                        message(httpResult.msg,{type:'error'})
                    }else {
                        this.applyFileAuth(httpResult.data)
                    }
                }
            }).finally(()=>{
                this.closeGlobalLoading()
            })
        },1000,true)()
    }

    applyFileAuth(result:FileAuthStatusResult) {
        //全部文件审批中
        if (result.status === 6001){
            return message('文件授权审批中',{type:'info'})
        }
        //6002.部分文件申请审批中
        //5001.全部文件未申请
        if (result.status === 6002||result.status === 6000){
            let newApplyFileIdList = result.newApplyFileIdList
            let applyingFileIdList = result.applyingFileIdList
            let authFileIdList = result.authFileIdList
            this.handleRequestApi(fileApi.getFileInfoByIdList([...newApplyFileIdList,...applyingFileIdList,...authFileIdList])).then(res=>{
                let fileList:TcFile[] = res.data
                let newApplyFileListStr = ''
                let applyingFileListStr = ''
                let authFileListStr = ''
                fileList.forEach((value)=>{
                    let fileId = value.id
                    let fileName = value.fileName + '.' + value.extName
                    if (newApplyFileIdList.includes(fileId)){
                        newApplyFileListStr = newApplyFileListStr + fileName + '\n'
                    }
                    if (applyingFileIdList.includes(fileId)){
                        applyingFileListStr = applyingFileListStr + fileName + '\n'
                    }
                    if (authFileIdList.includes(fileId)){
                        authFileListStr = authFileListStr + fileName + '\n'
                    }
                })
                let msg = ''
                if (!this.isEmptyArr(newApplyFileIdList)){
                    msg = msg + `\n【未申请授权文件】:\n\n${newApplyFileListStr}`
                }
                if (!this.isEmptyArr(applyingFileIdList)){
                    msg = msg + `\n【授权审批中文件】:\n\n${applyingFileListStr}`
                }
                if (!this.isEmptyArr(authFileIdList)){
                    msg = msg + `\n【已授权文件】:\n\n${authFileListStr}`
                }
                if (this.isEmptyArr(newApplyFileIdList)){
                    this.showMsgDialog(msg).then(()=>{})
                }else {
                    this.showMsgDialog(`${msg}\n是否继续申请授权`).then(()=>{
                        let columns: Array<DetailColumnDefine> = [
                            {prop: 'applyRemark', label: '申请备注',type: QueryTypeEnum.AREA_INPUT},
                        ]
                        useTimeoutFn(()=>{
                            this.openInputDialog(columns,null,null,(dialogResult)=>{
                                let data = {
                                    applyRemark: dialogResult.data.applyRemark,
                                    fileIdList: result.newApplyFileIdList
                                }
                                this.handleRequestApi(fileApplyApi.fileApply(data)).then(()=>{
                                    message('操作成功',{type:'success'})
                                    dialogResult.done()
                                })
                            })
                        },300)
                    })
                }
            })
        }
    }

    verifyFileAuth(fileIdList:string[],allowCallBack:()=>void){
        let tempFilePrefix = this.getTempFilePrefix()
        let filterFileIdList = fileIdList.filter(value => value!==tempFilePrefix);
        fileApplyApi.checkFileAuthStatus(filterFileIdList).then((res:HttpResult)=>{
            let result:FileAuthStatusResult = res.data
            if (result.status === 0){
                allowCallBack()
            }else {
                this.applyFileAuth(result)
            }
        })
    }

    downLoadFileById(fileId: string) {
        this.verifyFileAuth([fileId],()=>{
            this.openUrl(this.getDownLoadUrlByFileId(fileId))
        })
    }

    viewFileById(fileId: string, fileName: string) {
        this.verifyFileAuth([fileId],()=>{
            let url = this.getViewUrlByFileId(fileId);
            let tempFilePrefix = this.getTempFilePrefix()
            let fileExtName = this.getFileExtName(fileName)
            if (fileId.startsWith(tempFilePrefix)) {
                message('临时文件无法预览', {type: 'error'})
            } else if (["jpg", "jpeg", "png", "gif", "bmp"].includes(fileExtName)) {
                this.showImg(0, [url], (e) => {
                    this.revokeObjectURLByUrl(url)
                })
            } else {
                addDialog({
                    fullscreen: true,
                    appendToBody: true,
                    destroyOnClose: true,
                    hideFooter: true,
                    showClose: true,
                    title: "",
                    props: {
                        fileName: fileName,
                        url: url,
                        fileId: fileId
                    },
                    contentRenderer: () => PreviewFile
                })

            }
        })
    }

    urlWithAuth(url: string): string {
        let userInfo = this.getCurrentUserInfo();
        if (url.indexOf('?')!==-1){
            return url + '&accessToken=' + userInfo.accessToken
        }else {
            return url + '?accessToken=' + userInfo.accessToken
        }
    }

    getEnv(){
        return env
    }

    getDownLoadUrlByFileId(id: string) {
        let url = window.location.origin + this.getEnv().VITE_API_PATH + '/file/download?id=' + id
        return this.urlWithAuth(url)
    }

    getViewUrlByFileId(id: string) {
        let url = window.location.origin  + this.getEnv().VITE_API_PATH +  '/file/view?id=' + id
        return this.urlWithAuth(url)
    }

    openUrl(url: string) {
        window.open(url, '_blank')
    }


    generateUUID() {
        return buildUUID()
    }

    createFileChunks(file: File, chunkSize = 1024 * 1024 * 10) { // 默认分片大小为10MB
        let fileSize = file.size;
        let offset = 0;
        let chunks: Blob[] = [];

        while (offset < fileSize) {
            let chunk = file.slice(offset, Math.min(offset + chunkSize, fileSize));
            chunks.push(chunk);
            offset += chunkSize;
        }
        return chunks;
    }

    computeFileChunksMd5(chunkList: Blob[], percentageFn?: (percentage: number) => void) {
        const worker = new FileHashWorker()
        return new Promise<string>((resolve, reject) => {
            worker.onmessage = (e) => {
                let md5: string = e.data.md5
                let percentage: number = e.data.percentage
                if (!isNullOrUnDef(percentageFn)) {
                    percentageFn(percentage)
                }
                if (md5) {
                    if (!isNullOrUnDef(percentageFn)) {
                        percentageFn(100)
                    }
                    resolve(md5)
                }
            }
            worker.onerror = (e) => {
                console.log(e)
                reject(e.message)
            }
            worker.postMessage({chunkList: chunkList})
        })
    }

    computeFileMd5(file: File) {
        let chunkList = this.createFileChunks(file);
        return this.computeFileChunksMd5(chunkList)
    }

    async uploadFileMap(uploadMap: Map<string, File>) {
        let tempIdAndFileIdMap = new Map<string, string>()
        if (uploadMap.size === 0) {
            return tempIdAndFileIdMap
        } else {
            //文件超过10m就分块
            let chunkSize = 1024 * 1024 * 10
            this.showGlobalLoading()
            let allFileSize = 0
            uploadMap.forEach((value, key) => {
                allFileSize = allFileSize + value.size
            })
            //已上传文件大小
            let uploadedFileSize = 0
            let uploadedFileNum = 0
            //进度
            let progress = 0
            let uploadFileProgress = (progress: number, tip: string) => {
                this.updateGlobalLoadingText(`[${progress}%]${tip}`)
            }
            let receiveResultFn = (tempId: string, fileId: string) => {
                tempIdAndFileIdMap.set(tempId, fileId)
                if (tempIdAndFileIdMap.size == uploadMap.size) {
                    uploadFileProgress(100, '批量上传文件完成')
                }
            }
            let calculateUploadProgress = (alreadyUploadSize: number, msg: string) => {
                uploadedFileSize = uploadedFileSize + alreadyUploadSize
                progress = Math.floor(uploadedFileSize / allFileSize * 100)
                uploadFileProgress(progress, msg)
            }
            uploadFileProgress(progress, '开始批量上传文件')
            for (let [tempId, value] of uploadMap.entries()) {
                let fileName = value.name
                let fileSize = value.size
                if (fileSize > chunkSize) {
                    //大于分块数就执行分块上传
                    let chunkList = this.createFileChunks(value, chunkSize)
                    let md5 = await this.computeFileChunksMd5(chunkList, (percentage) => {
                        uploadFileProgress(progress, `[分块上传]文件->${fileName}执行MD5校验->[${percentage}%]`)
                    })
                    let data = {
                        fileName: fileName,
                        fileSize: fileSize,
                        md5: md5
                    }
                    let createChunkUploadRes = await fileApi.createChunkUpload(data)
                    //文件是否存在
                    let fileExists: boolean = createChunkUploadRes.data.fileExists
                    let fileId: string = createChunkUploadRes.data.fileId
                    let uploadId: string = createChunkUploadRes.data.uploadId
                    let filePath: string = createChunkUploadRes.data.filePath
                    //断点续传
                    let alreadyChunkNumberList: Array<number> = createChunkUploadRes.data.alreadyChunkNumberList
                    if (fileExists) {
                        calculateUploadProgress(fileSize, `[分块上传]文件->${fileName}已存在,完成上传`)
                        receiveResultFn(tempId, fileId)
                    } else {
                        let chunkIdList = []
                        for (let chunkNumber = 0; chunkNumber < chunkList.length; chunkNumber++) {
                            let chunk = chunkList[chunkNumber]
                            let chunkSize = chunk.size
                            if (alreadyChunkNumberList.includes(chunkNumber)) {
                                calculateUploadProgress(chunkSize, `[分块上传]文件->${fileName}, [${chunkNumber}/${chunkList.length}]已存在,完成上传`)
                                continue
                            }
                            let chunkData = {
                                chunkNumber: chunkNumber,
                                chunkSize: chunkSize,
                                filePath: filePath,
                                fileName: fileName,
                                uploadId: uploadId,
                                md5: md5
                            }
                            const formData = new FormData()
                            formData.append("data", JSON.stringify(chunkData))
                            formData.append("file", chunk)
                            let uploadChunkRes = await fileApi.uploadChunk(formData)
                            chunkIdList.push(uploadChunkRes.data.chunkId)
                            //更新进度条
                            calculateUploadProgress(chunkSize, `[分块上传]文件->${fileName}, [${chunkNumber}/${chunkList.length}]已上传完成`)
                        }
                        //合并文件
                        let data = {
                            uploadId: uploadId,
                            fileName: fileName,
                            filePath: filePath,
                            md5: md5
                        }
                        let mergeChunkRes = await fileApi.mergeChunk(data)
                        uploadFileProgress(progress, `[分块上传]文件->${fileName}完成合并上传`)
                        receiveResultFn(tempId, mergeChunkRes.data.id)
                    }
                } else {
                    //小于分块数就执行直接上传
                    let data = {
                        fileName: fileName,
                        fileSize: fileSize
                    }
                    const formData = new FormData()
                    formData.append("data", JSON.stringify(data))
                    formData.append("file", value)
                    let uploadFileRes = await fileApi.uploadFile(formData)
                    calculateUploadProgress(fileSize, `[直接上传]文件->${fileName}完成直接上传`)
                    receiveResultFn(tempId, uploadFileRes.data.id)
                }
                uploadedFileNum = uploadedFileNum + 1
            }
            this.closeGlobalLoading()
            uploadFileProgress = null
            receiveResultFn = null
            calculateUploadProgress = null
            return tempIdAndFileIdMap
        }
    }
}

const instance = Common.getInstance()
export default instance
