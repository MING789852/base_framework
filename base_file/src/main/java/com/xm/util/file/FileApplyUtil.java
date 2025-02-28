package com.xm.util.file;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.configuration.file.FileProperty;
import com.xm.fileAuth.domain.entity.TcFileApply;
import com.xm.fileAuth.domain.entity.TcFileApplyDetail;
import com.xm.fileAuth.domain.entity.TcFileAuthCertificate;
import com.xm.fileAuth.domain.result.FileAuthStatusResult;
import com.xm.fileAuth.enums.FileApplyStatusEnum;
import com.xm.fileAuth.enums.FileAuthStatusEnum;
import com.xm.fileAuth.mapper.TcFileApplyDetailMapper;
import com.xm.fileAuth.mapper.TcFileApplyMapper;
import com.xm.fileAuth.mapper.TcFileAuthCertificateMapper;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.bean.SpringBeanUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FileApplyUtil {

    private static final TcFileApplyMapper fileApplyMapper;
    private static final TcFileApplyDetailMapper fileApplyDetailMapper;
    private static final TcFileAuthCertificateMapper fileAuthCertificateMapper;
    private static final FileProperty fileProperty;

    static {
        fileApplyMapper = SpringBeanUtil.getBeanByClass(TcFileApplyMapper.class);
        fileApplyDetailMapper = SpringBeanUtil.getBeanByClass(TcFileApplyDetailMapper.class);
        fileAuthCertificateMapper = SpringBeanUtil.getBeanByClass(TcFileAuthCertificateMapper.class);
        fileProperty = SpringBeanUtil.getBeanByClass(FileProperty.class);
    }

    public static boolean disableCheckAuthCertificate(List<String> fileIdList) {
        Boolean auth = fileProperty.getAuth();
        //未开启文件授权则不检查文件授权状态
        return auth == null || !auth;
    }

    public static boolean allowRead(List<String> fileIdList){
        //未开启文件授权则允许读取文件
        if (disableCheckAuthCertificate(fileIdList)){
            return true;
        }else {
            FileAuthStatusResult fileAuthStatusResult = checkFileAuthStatus(fileIdList);
            if (FileAuthStatusEnum.APPLY_ALLOW.getValue().equals(fileAuthStatusResult.getStatus())){
                return true;
            }else {
                throw new CommonException(fileAuthStatusResult.getStatus(),"无权限读取文件",fileAuthStatusResult);
            }
        }
    }

    public static FileAuthStatusResult checkFileAuthStatus(List<String> fileIdList){

        FileAuthStatusResult result=new FileAuthStatusResult();
        if (CollectionUtil.isEmpty(fileIdList)){
            throw new CommonException("文件ID为空，获取文件授权状态失败");
        }
        TcUser user = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
        Date now=new Date();
        //先判断有没有授权
        LambdaQueryWrapper<TcFileAuthCertificate> certificateLambdaQueryWrapper=new LambdaQueryWrapper<>();
        certificateLambdaQueryWrapper
                .in(TcFileAuthCertificate::getFileId,fileIdList)
                .eq(TcFileAuthCertificate::getUserId,user.getId())
                .ge(TcFileAuthCertificate::getEffectiveTime,now);
        List<TcFileAuthCertificate> fileAuthCertificateList = fileAuthCertificateMapper.selectList(certificateLambdaQueryWrapper);

        List<String> authFileIdList = fileAuthCertificateList.stream().map(TcFileAuthCertificate::getFileId).collect(Collectors.toList());

        if (fileIdList.size()==fileAuthCertificateList.size()){
            result.setStatus(FileAuthStatusEnum.APPLY_ALLOW.getValue());
            result.setAuthFileIdList(authFileIdList);
            result.setNewApplyFileIdList(new ArrayList<>());
            result.setApplyingFileIdList(new ArrayList<>());
            return result;
        }else {
            LambdaQueryWrapper<TcFileApply> applyLambdaQueryWrapper=new LambdaQueryWrapper<>();
            applyLambdaQueryWrapper
                    .eq(TcFileApply::getUserId,user.getId())
                    .eq(TcFileApply::getStatus, FileApplyStatusEnum.approving.getValue());
            List<TcFileApply> fileApplies = fileApplyMapper.selectList(applyLambdaQueryWrapper);
            //去掉已授权，获取需要新增的审批的文件ID
            List<String> newApplyFileIdList = fileIdList.stream().filter(item -> !authFileIdList.contains(item)).collect(Collectors.toList());
            //如果已授权文件为空，，并且审批单为空,说明全部文件未申请
            if (CollectionUtil.isEmpty(authFileIdList)&&CollectionUtil.isEmpty(fileApplies)){
                result.setStatus(FileAuthStatusEnum.ALL_UN_APPLY.getValue());
                result.setAuthFileIdList(authFileIdList);
                result.setNewApplyFileIdList(newApplyFileIdList);
                result.setApplyingFileIdList(new ArrayList<>());
                return result;
            }

            List<String> applyIdList = fileApplies.stream().map(TcFileApply::getId).collect(Collectors.toList());
            LambdaQueryWrapper<TcFileApplyDetail> detailLambdaQueryWrapper=new LambdaQueryWrapper<>();
            detailLambdaQueryWrapper
                    .in(TcFileApplyDetail::getApplyId,applyIdList)
                    .in(TcFileApplyDetail::getFileId,fileIdList);
            List<TcFileApplyDetail> fileApplyDetailList = fileApplyDetailMapper.selectList(detailLambdaQueryWrapper);
            List<String> applyingFileIdList = fileApplyDetailList.stream().map(TcFileApplyDetail::getFileId).distinct().collect(Collectors.toList());

            //去掉申请中，获取需要新增的审批的文件ID
            newApplyFileIdList = newApplyFileIdList.stream()
                    .filter(item -> !applyingFileIdList.contains(item))
                    .collect(Collectors.toList());

            //如果已授权文件为空，并且新申请文件数为空,说明全部文件已在申请中
            if (CollectionUtil.isEmpty(authFileIdList)&&CollectionUtil.isEmpty(newApplyFileIdList)){
                result.setStatus(FileAuthStatusEnum.ALL_APPLYING.getValue());
            }else {
                result.setStatus(FileAuthStatusEnum.PART_APPLYING.getValue());
            }
            result.setAuthFileIdList(authFileIdList);
            result.setNewApplyFileIdList(newApplyFileIdList);
            result.setApplyingFileIdList(applyingFileIdList);
            return result;
        }
    }
}
