package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 参数验证失败
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ValidateException extends UncheckedException {

    public ValidateException() {
        super();
    }

    public ValidateException(String msg) {
        super(msg);
    }

    public ValidateException(Throwable e) {
        super(e);
    }

    public ValidateException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
