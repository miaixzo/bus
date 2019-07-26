package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 业务异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BusinessException extends UncheckedException {

    public BusinessException() {
        super();
    }

    public BusinessException(String msg) {
        super(msg);
    }


    public BusinessException(String msg, Throwable e) {
        super(msg, e);
    }

    public BusinessException(Throwable e) {
        super(e);
    }

    public BusinessException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}