package org.aoju.bus.core.lang.exception;

/**
 * HTTP请求异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class HttpUncheckException extends UncheckedException {

    public HttpUncheckException() {
    }

    public HttpUncheckException(String message) {
        super(message);
    }

    public HttpUncheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpUncheckException(Throwable cause) {
        super(cause);
    }

    public HttpUncheckException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
