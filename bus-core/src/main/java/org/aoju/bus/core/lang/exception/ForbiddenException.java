package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 权限不足异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ForbiddenException extends UncheckedException {

    public ForbiddenException() {
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenException(Throwable cause) {
        super(cause);
    }

    public ForbiddenException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
