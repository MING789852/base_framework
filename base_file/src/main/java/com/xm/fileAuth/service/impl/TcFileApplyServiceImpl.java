package com.xm.fileAuth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.file.domain.entity.TcFile;
import com.xm.fileAuth.domain.dto.ApplyFileDto;
import com.xm.fileAuth.domain.dto.AuthApplyFileDto;
import com.xm.fileAuth.domain.entity.TcFileApply;
import com.xm.fileAuth.domain.entity.TcFileApplyDetail;
import com.xm.fileAuth.domain.entity.TcFileAuthCertificate;
import com.xm.fileAuth.domain.result.FileAuthStatusResult;
import com.xm.fileAuth.domain.vo.TcFileApplyVo;
import com.xm.fileAuth.enums.FileApplyStatusEnum;
import com.xm.fileAuth.enums.FileAuthStatusEnum;
import com.xm.fileAuth.mapper.TcFileApplyDetailMapper;
import com.xm.fileAuth.mapper.TcFileApplyMapper;
import com.xm.fileAuth.mapper.TcFileAuthCertificateMapper;
import com.xm.fileAuth.service.TcFileApplyService;
import com.xm.interceptor.FileApplyInterceptor;
import com.xm.interceptor.FileAuthInterceptor;
import com.xm.interceptor.FileReadInterceptor;
import com.xm.module.core.params.QueryData;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.file.FileApplyUtil;
import com.xm.util.file.FileOptionUtil;
import com.xm.util.id.SnowIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TcFileApplyServiceImpl implements TcFileApplyService {

    private final TcFileApplyMapper fileApplyMapper;
    private final TcFileApplyDetailMapper fileApplyDetailMapper;
    private final TcFileAuthCertificateMapper fileAuthCertificateMapper;
    private final List<FileApplyInterceptor> fileApplyInterceptorList;
    private final List<FileAuthInterceptor> fileAuthInterceptorList;
    private final FileReadInterceptor fileReadInterceptor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String fileApply(ApplyFileDto applyFileDto) {
        if (CollectionUtil.isNotEmpty(fileApplyInterceptorList)){
            fileApplyInterceptorList.forEach(item->item.before(applyFileDto));
        }
        String applyRemark = applyFileDto.getApplyRemark();
        List<String> fileIdList = applyFileDto.getFileIdList();
        Date effectiveTime = applyFileDto.getEffectiveTime();
        String refId = applyFileDto.getRefId();
        String refType = applyFileDto.getRefType();
        if (CollectionUtil.isEmpty(fileIdList)){
            throw new CommonException("文件ID为空，无法申请授权");
        }
        TcUser user = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
        Date now=new Date();
        List<TcFile> fileInfoByIdList = FileOptionUtil.getFileInfoByIdList(fileIdList);
        if (CollectionUtil.isEmpty(fileInfoByIdList)){
            throw new CommonException("申请文件不存在");
        }
        TcFileApply apply=new TcFileApply();
        apply.setId(SnowIdUtil.getSnowId());
        apply.setApplyTime(now);
        apply.setUserName(user.getNickName());
        apply.setUserId(user.getId());
        apply.setEffectiveTime(effectiveTime);
        apply.setStatus(FileApplyStatusEnum.approving.getValue());
        apply.setApplyRemark(applyRemark);
        apply.setRefId(refId);
        apply.setRefType(refType);

        fileApplyMapper.insert(apply);

        List<TcFileApplyDetail> fileApplyDetailList=new ArrayList<>();
        for (TcFile file:fileInfoByIdList){
            TcFileApplyDetail detail=new TcFileApplyDetail();
            detail.setFileName(file.getFileName()+"."+file.getExtName());
            detail.setFileId(file.getId());
            detail.setApplyId(apply.getId());
            detail.setId(SnowIdUtil.getSnowId());
            fileApplyDetailMapper.insert(detail);
            fileApplyDetailList.add(detail);
        }

        if (CollectionUtil.isNotEmpty(fileApplyInterceptorList)){
            fileApplyInterceptorList.forEach(item->item.after(apply,fileApplyDetailList));
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String fileAuth(AuthApplyFileDto authApplyFileDto) {
        if (CollectionUtil.isNotEmpty(fileAuthInterceptorList)){
            fileAuthInterceptorList.forEach(item->item.before(authApplyFileDto));
        }
        TcUser user = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
        Date now=new Date();
        Integer applyStatus = authApplyFileDto.getApplyStatus();
        Date effectiveTime = authApplyFileDto.getEffectiveTime();
        List<TcFileApply> list = authApplyFileDto.getList();
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空，无法操作");
        }
        if (effectiveTime==null){
            throw new CommonException("有效期为空，无法操作");
        }
        List<Integer> applyStatusList = Arrays.stream(FileApplyStatusEnum.values()).map(FileApplyStatusEnum::getValue).collect(Collectors.toList());
        if (!applyStatusList.contains(applyStatus)){
            throw new CommonException("非法状态");
        }
        for (TcFileApply item:list){
            String id=item.getId();
            if (StrUtil.isBlank(id)){
                continue;
            }
            TcFileApply apply = fileApplyMapper.selectById(id);
            if (apply==null){
                String msg=StrUtil.format("申请id->{}不存在",id);
                throw new CommonException(msg);
            }
            if (!FileApplyStatusEnum.approving.getValue().equals(apply.getStatus())){
                String msg=StrUtil.format("申请id->{}非待审批状态",id);
                throw new CommonException(msg);
            }
            TcFileApply update=new TcFileApply();
            update.setId(id);
            update.setOperateUser(user.getNickName());
            update.setOperateUserId(user.getId());
            update.setOperateTime(now);
            update.setEffectiveTime(effectiveTime);
            update.setStatus(applyStatus);
            fileApplyMapper.updateById(update);

            //如果同意则生成凭证
            if (FileApplyStatusEnum.success.getValue().equals(applyStatus)){

                LambdaQueryWrapper<TcFileApplyDetail> detailLambdaQueryWrapper=new LambdaQueryWrapper<>();
                detailLambdaQueryWrapper.eq(TcFileApplyDetail::getApplyId,apply.getId());
                List<TcFileApplyDetail> fileApplyDetailList = fileApplyDetailMapper.selectList(detailLambdaQueryWrapper);

                if (CollectionUtil.isNotEmpty(fileApplyDetailList)){
                    for (TcFileApplyDetail detail:fileApplyDetailList){
                        LambdaQueryWrapper<TcFileAuthCertificate> certificateLambdaQueryWrapper=new LambdaQueryWrapper<>();
                        certificateLambdaQueryWrapper
                                .eq(TcFileAuthCertificate::getFileId,detail.getFileId())
                                .eq(TcFileAuthCertificate::getUserId,apply.getUserId());
                        TcFileAuthCertificate certificate = fileAuthCertificateMapper.selectOne(certificateLambdaQueryWrapper);
                        if (certificate==null){
                            certificate=new TcFileAuthCertificate();

                            certificate.setId(SnowIdUtil.getSnowId());
                            certificate.setEffectiveTime(effectiveTime);
                            certificate.setFileName(detail.getFileName());
                            certificate.setFileId(detail.getFileId());
                            certificate.setApplyTime(apply.getApplyTime());
                            certificate.setUserId(apply.getUserId());
                            certificate.setUserName(apply.getUserName());
                            certificate.setOperateTime(now);
                            certificate.setOperateUserId(user.getId());
                            certificate.setOperateUser(user.getNickName());

                            fileAuthCertificateMapper.insert(certificate);
                        }else {
                            TcFileAuthCertificate temp=new TcFileAuthCertificate();
                            temp.setId(certificate.getId());
                            temp.setApplyTime(apply.getApplyTime());
                            temp.setEffectiveTime(effectiveTime);
                            temp.setOperateTime(now);
                            temp.setOperateUserId(user.getId());
                            temp.setOperateUser(user.getNickName());
                            fileAuthCertificateMapper.updateById(temp);
                        }
                    }
                }
            }
        }

        if (CollectionUtil.isNotEmpty(fileAuthInterceptorList)){
            fileAuthInterceptorList.forEach(item->item.after(authApplyFileDto));
        }
        return "操作成功";
    }

    @Override
    public FileAuthStatusResult checkFileAuthStatus(List<String> fileIdList) {
        FileAuthStatusResult result=new FileAuthStatusResult();
        if (fileReadInterceptor!=null){
            if (fileReadInterceptor.disableCheckAuthCertificate(fileIdList)){
                result.setStatus(FileAuthStatusEnum.APPLY_ALLOW.getValue());
                result.setAuthFileIdList(fileIdList);
                result.setNewApplyFileIdList(new ArrayList<>());
                result.setApplyingFileIdList(new ArrayList<>());
                return result;
            }
        }
        return FileApplyUtil.checkFileAuthStatus(fileIdList);
    }

    @Override
    public Page<TcFileApply> selectByPage(QueryData<TcFileApply> queryData) {
        Page<TcFileApply> page=new Page<>(queryData.getCurrent(), queryData.getSize());
        QueryWrapper<TcFileApply> queryWrapper = queryData.generateQueryWrapper();
        page = fileApplyMapper.selectPage(page, queryWrapper);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcFileApplyVo> list) {
        return null;
    }

    @Override
    public TcFileApplyVo fillData(TcFileApply fileApply) {
        String id = fileApply.getId();
        TcFileApplyVo vo=new TcFileApplyVo();
        if (StrUtil.isBlank(id)){
            vo.setDetailList(new ArrayList<>());
            return vo;
        }else {
            TcFileApply database = fileApplyMapper.selectById(id);
            BeanUtil.copyProperties(database,vo);
            LambdaQueryWrapper<TcFileApplyDetail> detailLambdaQueryWrapper=new LambdaQueryWrapper<>();
            detailLambdaQueryWrapper.eq(TcFileApplyDetail::getApplyId,id);
            List<TcFileApplyDetail> fileApplyDetailList = fileApplyDetailMapper.selectList(detailLambdaQueryWrapper);
            vo.setDetailList(fileApplyDetailList);
            return vo;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcFileApply> list) {
        List<String> deleteIdList = list.stream().map(TcFileApply::getId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(deleteIdList)){
            //删除主表
            LambdaQueryWrapper<TcFileApply> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(TcFileApply::getId,deleteIdList);
            fileApplyMapper.delete(lambdaQueryWrapper);
            //删除明细表
            LambdaQueryWrapper<TcFileApplyDetail> detailLambdaQueryWrapper=new LambdaQueryWrapper<>();
            detailLambdaQueryWrapper.in(TcFileApplyDetail::getApplyId,deleteIdList);
            fileApplyDetailMapper.delete(detailLambdaQueryWrapper);
        }
        return "操作成功";
    }
}
