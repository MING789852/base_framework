package com.xm.module.sequence.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.module.sequence.domain.entity.TcSequence;
import com.xm.module.sequence.mapper.TcSequenceMapper;
import com.xm.module.sequence.service.TcSequenceService;
import com.xm.util.lock.LockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class TcSequenceServiceImpl implements TcSequenceService {

    private final TcSequenceMapper sequenceMapper;

    @Override
    public Long getSequenceByCode(String code) {
        return LockUtil.lock(code,()->{
            Date now = new Date();
            LambdaQueryWrapper<TcSequence> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(TcSequence::getCode,code);
            TcSequence tcSequence = sequenceMapper.selectOne(queryWrapper);
            Long value;
            if (tcSequence!=null){
                //判断是否跨天了,跨天则重置序号
                String nowStr=DateUtil.format(now,"yyyyMMdd");
                String updateDateStr=DateUtil.format(tcSequence.getUpdateDate(),"yyyyMMdd");
                if (nowStr.equals(updateDateStr)){
                    value=tcSequence.getValue();
                } else {
                    value=1L;
                }

                tcSequence.setValue(value+1);
                tcSequence.setUpdateDate(now);
                sequenceMapper.updateById(tcSequence);
            }else {
                value=1L;
                tcSequence=new TcSequence();
                tcSequence.setCode(code);
                tcSequence.setCreateDate(now);
                tcSequence.setUpdateDate(now);
                tcSequence.setValue(value+1);
                sequenceMapper.insert(tcSequence);
            }
            return value;
        },(e)->{
            String msg=StrUtil.format("获取序号失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        });
    }
}
