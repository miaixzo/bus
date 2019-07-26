package org.aoju.bus.core.lang.exception;

/**
 * 无效的参数异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InvalidParamsException extends UncheckedException {

    public InvalidParamsException() {
    }

    public InvalidParamsException(String message) {
        super(message);
    }

    public InvalidParamsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidParamsException(Throwable cause) {
        super(cause);
    }

    public InvalidParamsException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
