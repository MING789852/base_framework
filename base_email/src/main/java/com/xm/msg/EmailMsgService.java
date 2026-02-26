package com.xm.msg;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.configuration.mail.MailProperty;
import com.xm.core.msg.MsgSaveService;
import com.xm.core.msg.MsgService;
import com.xm.core.msg.consts.MsgTypeConst;
import com.xm.core.msg.params.ErrorSendResult;
import com.xm.core.msg.params.Msg;
import com.xm.core.msg.params.MsgSendResult;
import com.xm.msg.params.EmailInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(MsgTypeConst.email)
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "msg.email",name = "enable", havingValue = "true")
public class EmailMsgService implements MsgService {

    private final MailProperty mailProperty;

    private final EmailMappingService emailMappingService;

    private final JavaMailSender javaMailSender;

    private final MsgSaveService msgSaveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MsgSendResult createOrUpdateMsg(List<Msg> msgList) {
        MsgSendResult result = new MsgSendResult();
        if (CollectionUtil.isEmpty(msgList)){
            return result;
        }
        List<MimeMessage> mimeMessages = new ArrayList<>();
        List<Msg> unknownResultMsgList = new ArrayList<>();
        for (Msg msg:msgList){
            if (StrUtil.isBlank(msg.getBusinessType())){
                throw new CommonException("【邮件消息】业务类型为空，无法创建或更新邮件消息");
            }
            if (StrUtil.isBlank(msg.getBusinessKey())){
                throw new CommonException("【邮件消息】业务主键为空，无法创建或更新邮件消息");
            }
            if (StrUtil.isBlank(msg.getTitle())){
                throw new CommonException("【邮件消息】邮件标题为空，无法创建或更新邮件消息");
            }
            if (StrUtil.isBlank(msg.getContent())){
                throw new CommonException("【邮件消息】邮件内容为空，无法创建或更新邮件消息");
            }
            List<String> toUserIdList = msg.getToUserIdList();
            List<String> ccUserIdList = msg.getCcUserIdList();
            if (CollectionUtil.isEmpty(toUserIdList)){
                throw new CommonException("【邮件消息】邮件接收人为空，无法创建或更新邮件消息");
            }
            List<String> toList = toUserIdList
                    .stream()
                    .map(item->{
                        //如果是邮箱的话就不用转换
                        if (Validator.isEmail(item)){
                            return item;
                        }else {
                            return emailMappingService.getEmailByUserId(item);
                        }
                    })
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
            List<String> ccList;
            if (CollectionUtil.isNotEmpty(ccUserIdList)){
                ccList = ccUserIdList.stream().map(item->{
                            //如果是邮箱的话就不用转换
                            if (Validator.isEmail(item)){
                                return item;
                            }else {
                                return emailMappingService.getEmailByUserId(item);
                            }
                        })
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
            }else {
                ccList = new ArrayList<>();
            }

            if (CollectionUtil.isEmpty(toList)){
                result.addFailSendMsg(msg,new ErrorSendResult(null,"转换之后，邮件接收人为空，无法创建或更新邮件消息",null));
                continue;
            }

            EmailInfo emailInfo=new EmailInfo();
            emailInfo.setFromEmail(mailProperty.getUsername());
            emailInfo.setSender(mailProperty.getSender());
            emailInfo.setTitle(msg.getTitle());
            emailInfo.setContent(msg.getContent());
            emailInfo.setToUserList(toList);
            emailInfo.setCcUserList(ccList);

            MimeMessage mimeMessage = generateMimeMessageWithFile(toList, ccList, msg.getTitle(), msg.getContent(), msg.getFileMap());
            mimeMessages.add(mimeMessage);
            msg.setType(getMsgType());
            msg.setInfo(JSONUtil.toJsonStr(emailInfo));
            msgSaveService.saveMsg(msg);

            unknownResultMsgList.add(msg);
        }
        if (CollectionUtils.isEmpty(mimeMessages)){
            return result;
        }
        try {
            MimeMessage[] msg=new MimeMessage[mimeMessages.size()];
            mimeMessages.toArray(msg);
            javaMailSender.send(msg);
            //存储发送结果
            result.addSuccessSendMsgList(unknownResultMsgList);
        }catch (Exception e){
            log.error("[JavaMailSender邮件发送错误]错误原因",e);
            //存储发送结果
            String errorReason=StrUtil.format("[JavaMailSender邮件发送错误]错误原因->{}",e.getMessage());
            result.addFailSendMsgList(unknownResultMsgList,new ErrorSendResult(null,errorReason,e));
        }
        return result;
    }

    @Override
    public boolean deleteMsgList(String businessType, String businessKey) {
        log.info("【邮件消息】businessType->{},businessKey->{},无法删除",businessType,businessKey);
        return false;
    }

    @Override
    public boolean deleteMsg(String businessType, String businessKey, String userId) {
        log.info("【邮件消息】businessType->{},businessKey->{},userId->{},无法删除",businessType,businessKey,userId);
        return false;
    }

    @Override
    public boolean finishMsgList(String businessType, String businessKey) {
        log.info("【邮件消息】businessType->{},businessKey->{},无法完结",businessType,businessKey);
        return false;
    }

    @Override
    public boolean finishMsg(String businessType, String businessKey, String userId) {
        log.info("【邮件消息】businessType->{},businessKey->{},userId->{},无法完结",businessType,businessKey,userId);
        return false;
    }

    @Override
    public String getMsgType() {
        return MsgTypeConst.email;
    }



    /**
     * @param toList  发送人
     * @param ccList 抄送人，无则填null
     * @param tittle 邮件标题
     * @param content  邮件主题
     * @param fileMap 附件
     * @return  MimeMessage
     */
    private  MimeMessage generateMimeMessageWithFile(List<String> toList, List<String> ccList, String tittle, String content, Map<String, byte[]> fileMap){
        if (CollectionUtils.isEmpty(toList)){
            log.error("邮件接收人为空");
            return null;
        }
        //抄送人为空
        if (ccList==null){
            ccList=new ArrayList<>();
        }

        //设置接收人和抄送人
        String [] tos= ArrayUtil.toArray(toList,String.class);
        String [] ccs=ArrayUtil.toArray(ccList,String.class);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message,true);
        } catch (MessagingException e) {
            log.error("MimeMessageHelper创建邮件失败",e);
            throw new CommonException("添加附件失败");
        }

        log.info("邮件接收人---->{}\n 邮件抄送人->{}\n 邮件标题--->{} \n 邮件内容--->{}",toList,ccList,tittle,content);

        try {
            //设置附件
            if (fileMap!=null&&!fileMap.isEmpty()){
                for (Map.Entry<String,byte[]> entry:fileMap.entrySet()){
                    try {
                        helper.addAttachment(entry.getKey(),new ByteArrayResource(entry.getValue()));
                    } catch (Exception e) {
                        log.error("添加附件失败",e);
                        throw new CommonException("添加附件失败");
                    }
                }
            }
            //设置发送人
            helper.setFrom(mailProperty.getUsername(),mailProperty.getSender());
            //设置接收人
            helper.setTo(tos);
            //设置抄送人
            if (CollectionUtil.isNotEmpty(ccList)) {
                helper.setCc(ccs);
            }
            //设置邮件标题
            helper.setSubject(tittle);
            //设置内容
            helper.setText(content,true);

            return helper.getMimeMessage();
        } catch (Exception e) {
            log.error("获取邮件错误",e);
            return null;
        }
    }
}
