package org.aoju.bus.core.lang.exception;

import lombok.Data;

/**
 * 自定义异常: 未受检异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class UncheckedException extends RuntimeException {

    /**
     * 错误码
     */
    protected String errcode;
    /**
     * 错误信息
     */
    protected String errmsg;

    public UncheckedException() {
    }

    public UncheckedException(String message) {
        super(message);
    }

    public UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UncheckedException(Throwable cause) {
        super(cause);
    }

    public UncheckedException(String errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

}
