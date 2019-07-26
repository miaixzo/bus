package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 定时任务执行异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CrontabException extends UnauthorizedException {

    public CrontabException() {
    }

    public CrontabException(String message) {
        super(message);
    }

    public CrontabException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrontabException(Throwable cause) {
        super(cause);
    }

    public CrontabException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
