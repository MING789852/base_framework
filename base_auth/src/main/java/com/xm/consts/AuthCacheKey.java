package com.xm.consts;

public class AuthCacheKey {
    private static final String requestActionPrefix ="requestAction:";

    private static final String userAndSessionPrefix="loginMapping:";

    public static String getRequestAction(String suffix){
        return requestActionPrefix+suffix;
    }

    public static String getUserAndSession(String suffix){
        return userAndSessionPrefix+suffix;
    }
}
