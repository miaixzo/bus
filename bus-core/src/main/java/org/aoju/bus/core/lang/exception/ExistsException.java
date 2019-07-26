package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 对象已存在异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ExistsException extends UncheckedException {

    public ExistsException() {
    }

    public ExistsException(String message) {
        super(message);
    }

    public ExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExistsException(Throwable cause) {
        super(cause);
    }

    public ExistsException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
