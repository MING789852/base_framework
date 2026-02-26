package com.xm.util.dingding.task;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.dingding.DingDingUtil;
import com.xm.util.dingding.task.requestParams.DDPersonalTaskCreateParam;
import com.xm.util.dingding.task.requestParams.DDTaskCreateParam;
import com.xm.util.dingding.task.requestParams.DDTaskUpdateParam;
import com.xm.util.dingding.task.requestRes.DDPersonalTaskCreateRes;
import com.xm.util.dingding.task.requestRes.DDTaskCreateRes;
import com.xm.util.dingding.task.requestRes.DDTaskDeleteRes;
import com.xm.util.dingding.task.requestRes.DDTaskUpdateRes;
import com.xm.util.http.HttpUtils;
import com.xm.util.http.enums.HttpMethodEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DingDingTaskUtil {
    /**
     * 待办更新
     */
    public static DDTaskUpdateRes taskUpdate(DDTaskUpdateParam param, String taskId){
        String url = StrUtil.format("https://api.dingtalk.com/v1.0/todo/users/MhC5jXZMkKwoVvxt9srSkwiEiE/tasks/{}",taskId);
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        String resStr= HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.PUT,header, JSONUtil.toJsonStr(param));
        return JSONUtil.toBean(resStr, DDTaskUpdateRes.class);
    }

    /**
     * 待办删除
     */
    public static DDTaskDeleteRes taskDelete(String taskId){
        String url = StrUtil.format("https://api.dingtalk.com/v1.0/todo/users/MhC5jXZMkKwoVvxt9srSkwiEiE/tasks/{}",taskId);
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url,HttpMethodEnum.DELETE,header, null);
        return JSONUtil.toBean(resStr, DDTaskDeleteRes.class);
    }

    /**
     * 待办创建
     * unionId：设计用于区分个人、企业场景资源归属地，目前开放接口仅支持企业场景，这个可以理解为创建者，直接传创建者ID即可。
     * creatorId：创建该待办的用户。
     * executorId：要执行待办任务的人。
     * participantId：待办任务的参与人，仅关注查看任务信息，无操作权限。
     * operatorId：本次请求的操作用户。
     * 特别注意:
     * 如果creatorId(创建者的unionid)和executorIds(执行者的unionid)相同时，则收不到DING通知。
     */
    public static DDTaskCreateRes taskCreate(DDTaskCreateParam param){
        //使用创建者
        String creatorId = param.getCreatorId();
        String url = StrUtil.format("https://api.dingtalk.com/v1.0/todo/users/{}/tasks?operatorId={}",creatorId,creatorId);
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(param));
        DDTaskCreateRes bean = JSONUtil.toBean(resStr, DDTaskCreateRes.class);
        bean.setResult(!StrUtil.isBlank(bean.getId()));
        return bean;
    }

    public static DDPersonalTaskCreateRes personalTaskCreate(DDPersonalTaskCreateParam param){
        //使用创建者
        String url = "https://api.dingtalk.com/v1.0/todo/users/me/personalTasks";
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(param));
        DDPersonalTaskCreateRes bean = JSONUtil.toBean(resStr, DDPersonalTaskCreateRes.class);
        if (StrUtil.isBlank(bean.getTaskId())){
            String msg=StrUtil.format("【钉钉待办消息】创建个人待办失败->{}",resStr);
            log.error(msg);
            throw new CommonException(msg);
        }else {
            return bean;
        }
    }
}
