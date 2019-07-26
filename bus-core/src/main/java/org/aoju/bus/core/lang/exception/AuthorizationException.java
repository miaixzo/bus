package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 认证失败异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class AuthorizationException extends UnauthorizedException {

    public AuthorizationException() {
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    public AuthorizationException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
