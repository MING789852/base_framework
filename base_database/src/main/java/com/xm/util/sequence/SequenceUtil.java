package com.xm.util.sequence;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.module.sequence.service.TcSequenceService;
import com.xm.util.bean.SpringBeanUtil;

import java.util.Date;

public class SequenceUtil {
    public static Long getSequenceNo(String code){
        TcSequenceService tcSequenceService= SpringBeanUtil.getBeanByClass(TcSequenceService.class);
        return tcSequenceService.getSequenceByCode(code);
    }

    /**
     * 获取前缀 + yyyyMMdd + 序号位数
     * @param preCode
     * @param code
     * @param seqLength 序号长度
     * @return
     */
    public static String getPreDateSequence(String preCode,String code,int seqLength){
        Long sequenceNo=getSequenceNo(code);
        String dateStr= DateUtil.format(new Date(),"yyyyMMdd");
        String padSequenceNo= StrUtil.padPre(sequenceNo.toString(),seqLength,"0");
        return StrUtil.format("{}{}{}",preCode,dateStr,padSequenceNo);
    }
}
