package com.xm.msg;

import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.cache.config.CustomCacheConfig;
import com.xm.core.msg.MsgUserIdMappingService;
import com.xm.util.dingding.DingdingUtil;
import com.xm.util.dingding.requestRes.DDUserDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DDUnionIdMappingService {

    private final MsgUserIdMappingService userIdMappingService;

    @Cacheable(value = "ddUnionId:getUnionIdByUserId",keyGenerator = CustomCacheConfig.keyGeneratorName)
    public String getUnionIdByUserId(String userId) {
        if (StrUtil.isBlank(userId)){
            return null;
        }
        String mappingType = "ddUnionId";
        String mapping = userIdMappingService.getMapping(mappingType,userId);
        if (StrUtil.isBlank(mapping)){
            DDUserDetail userDetail = DingdingUtil.getUserDetail(DingdingUtil.getAccessToken(), userId);
            if (userDetail==null){
                throw new CommonException("请求钉钉UserId获取UnionId接口失败");
            }
            userIdMappingService.saveMapping(mappingType,userId,userDetail.getUnionid());
            mapping=userDetail.getUnionid();
        }
        return mapping;
    }
}
