package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 无法找到校验器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NoSuchException extends UncheckedException {

    public NoSuchException() {
    }

    public NoSuchException(String message) {
        super(message);
    }

    public NoSuchException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchException(Throwable cause) {
        super(cause);
    }

    public NoSuchException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
