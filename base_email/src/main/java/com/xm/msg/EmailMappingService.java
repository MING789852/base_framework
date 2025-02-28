package com.xm.msg;

import cn.hutool.core.util.StrUtil;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.service.TcUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailMappingService {

    private final TcUserService userService;

    public String getEmailByUserId(String userId) {
        if (StrUtil.isBlank(userId)){
            return null;
        }
        TcUser user = userService.selectById(userId);
        if (user==null){
            return null;
        }
        return user.getEmail();
    }
}
