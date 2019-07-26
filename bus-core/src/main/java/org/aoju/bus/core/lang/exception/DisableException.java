package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 数据被禁用异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DisableException extends UncheckedException {

    public DisableException() {
    }

    public DisableException(String message) {
        super(message);
    }

    public DisableException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisableException(Throwable cause) {
        super(cause);
    }

    public DisableException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
