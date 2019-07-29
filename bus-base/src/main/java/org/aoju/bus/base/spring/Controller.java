package org.aoju.bus.base.spring;


import org.aoju.bus.base.consts.ResultCode;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import com.alibaba.fastjson.JSON;

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

    public static String write(ResultCode respCode) {
        return write(respCode, null);
    }

    public static String write(ResultCode respCode, Object data) {
        return write(respCode.getErrcode(), respCode.getErrmsg(), data);
    }

    public static String write(ResultCode respCode, String message) {
        return write(respCode.getErrcode(), StringUtils.isEmpty(message) ? respCode.getErrmsg() : message);
    }

    public static String write(Object data) {
        return write(ResultCode.EM_SUCCESS, data);
    }

    public static String write(String errcode) {
        return write(ResultCode.of(errcode), null);
    }

    public static String write(String errcode, String errmsg) {
        return write(errcode, errmsg, null);
    }

    public static String write(String errcode, String errmsg, Object data) {
        ResultCode resultCode = ResultCode.of(errcode);
        if (ObjectUtils.isNotEmpty(resultCode)) {
            errmsg = StringUtils.isEmpty(errmsg) ? resultCode.getErrmsg() : errmsg;
            return JSON.toJSON(new Message(resultCode.getErrcode(), errmsg, data)).toString();
        }
        return JSON.toJSON(new Message(ResultCode.EM_FAILURE.getErrcode(), ResultCode.EM_FAILURE.errmsg)).toString();
    }

}
