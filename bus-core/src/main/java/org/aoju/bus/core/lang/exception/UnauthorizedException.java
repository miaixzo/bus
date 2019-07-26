package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 未认证异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class UnauthorizedException extends UncheckedException {

    public UnauthorizedException() {
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    public UnauthorizedException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
