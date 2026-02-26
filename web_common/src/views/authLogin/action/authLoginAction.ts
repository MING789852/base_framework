import {getTopMenu, initRouter} from "@/router/utils";
import {isNullOrUnDef} from "@pureadmin/utils";
import {message} from "@/utils/message";
import common from "@/utils/common";
import {useMultiTagsStoreHook} from "@/store/modules/multiTags";
import {useModalHook} from "@/store/modules/modal";
import router from "@/router";

const authActionTypeEnum = {
    redirectUrl: 'redirectUrl',
    openDialog: 'openDialog'
}
interface DialogComponent {
    path: string,
    props: Object
}
const loginAfterAction = async ()=>{
    const modalHook =useModalHook()
    //清空全局弹窗遮罩
    modalHook.hideGlobalDialog()
    //初始化路由
    await initRouter()
    //获取路由参数
    let urlParams=common.getCurrentRouteQuery()
    const actionType = urlParams.actionType as string
    //获取首页路径
    let homePath = getTopMenu(true).path
    if (isNullOrUnDef(actionType)) {
        message("登录成功,开始跳转首页", {type: "success"})
        await router.push(homePath);
    }else {
        message("登录成功,开始跳转特定页面", {type: "success"});
        if (actionType === authActionTypeEnum.redirectUrl) {
            let redirectUrl = urlParams.redirectUrl as string
            if (!isNullOrUnDef(redirectUrl)) {
                redirectUrl = decodeURIComponent(redirectUrl);
                //去掉redirectUrl参数
                let redirectUrlWithoutParams=redirectUrl.split('?')[0]
                //找到路由数据
                const route = common.getRouterByPath(redirectUrlWithoutParams)
                if (isNullOrUnDef(route)){
                    return message('路由'+redirectUrlWithoutParams+'不存在', {type: 'error'})
                }
                //手动添加标签
                const { path, name, meta } = route;
                useMultiTagsStoreHook().handleTags("push", {
                    path,
                    name,
                    meta
                });
                //开始跳转路由
                await router.push(redirectUrl)
            }
        }
        if (actionType === authActionTypeEnum.openDialog) {
            let openDialog = urlParams.openDialog as string
            router.push(homePath).then(()=>{
                if (!isNullOrUnDef(openDialog)) {
                    let dialogComponent:DialogComponent = JSON.parse(openDialog)
                    modalHook.globalShowClose=false
                    modalHook.showGlobalDialog(dialogComponent.path, dialogComponent.props)
                }
            })
        }
    }
}

export default {
    loginAfterAction
}