package com.xm.core.msg;

public interface MsgUserIdMappingService {
    String getMapping(String type,String userId);
    void saveMapping(String type,String userId, String mapping);
}
