package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 资源不存在
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NotFoundException extends UncheckedException {

    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
