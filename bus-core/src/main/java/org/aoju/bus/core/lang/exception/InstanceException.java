package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 实例化异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InstanceException extends UncheckedException {

    public InstanceException() {
        super();
    }

    public InstanceException(Throwable t) {
        super(t);
    }

    public InstanceException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
