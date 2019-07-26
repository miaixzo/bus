package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 处理异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InstrumentException extends UncheckedException {

    public InstrumentException() {
        super();
    }

    public InstrumentException(Throwable t) {
        super(t);
    }

    public InstrumentException(String message) {
        super(message);
    }

    public InstrumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstrumentException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
