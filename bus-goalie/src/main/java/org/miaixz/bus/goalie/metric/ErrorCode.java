package org.miaixz.bus.goalie.metric;

/**
 * 自定义错误码
 */
public class ErrorCode extends org.miaixz.bus.base.normal.ErrorCode {

    /**
     * 没有API权限
     */
    public static String EM_80010001 = "80010001";
    /**
     * 没有角色
     */
    public static String EM_80010002 = "80010002";
    /**
     * 服务端超时
     */
    public static String EM_80010003 = "80010003";
    /**
     * 服务端未响应
     */
    public static String EM_80010004 = "80010004";

    static {
        register(EM_80010001, "没有API权限");
        register(EM_80010002, "没有角色");
        register(EM_80010003, "服务端超时");
        register(EM_80010004, "服务端未响应");
    }

}
