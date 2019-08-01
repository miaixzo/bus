package org.aoju.bus.base.spring;


import com.alibaba.fastjson.JSON;
import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;

/**
 * <p>
 * 基础输出封装
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Controller {

    public static String write(ErrorCode respCode) {
        return write(respCode, null);
    }

    public static String write(ErrorCode respCode, Object data) {
        return write(respCode.getErrcode(), respCode.getErrmsg(), data);
    }

    public static String write(ErrorCode respCode, String message) {
        return write(respCode.getErrcode(), StringUtils.isEmpty(message) ? respCode.getErrmsg() : message);
    }

    public static String write(Object data) {
        return write(ErrorCode.EM_SUCCESS, data);
    }

    public static String write(String errcode) {
        return write(ErrorCode.of(errcode), null);
    }

    public static String write(String errcode, String errmsg) {
        return write(errcode, errmsg, null);
    }

    public static String write(String errcode, String errmsg, Object data) {
        ErrorCode resultCode = ErrorCode.of(errcode);
        if (ObjectUtils.isNotEmpty(resultCode)) {
            errmsg = StringUtils.isEmpty(errmsg) ? resultCode.getErrmsg() : errmsg;
            return JSON.toJSON(new Message(resultCode.getErrcode(), errmsg, data)).toString();
        }
        return JSON.toJSON(new Message(ErrorCode.EM_FAILURE.getErrcode(), ErrorCode.EM_FAILURE.errmsg)).toString();
    }

}
