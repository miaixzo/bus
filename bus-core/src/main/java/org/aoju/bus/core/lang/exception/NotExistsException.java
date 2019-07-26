package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 对象不存在
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NotExistsException extends UncheckedException {

    public NotExistsException() {
    }

    public NotExistsException(String message) {
        super(message);
    }

    public NotExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotExistsException(Throwable cause) {
        super(cause);
    }

    public NotExistsException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
