package org.aoju.bus.core.lang.exception;

public class TokenExpiredException extends UncheckedException {

    public TokenExpiredException() {
        super();
    }

    public TokenExpiredException(Throwable t) {
        super(t);
    }

    public TokenExpiredException(String format, Object... args) {
        super(format, args);
    }

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenExpiredException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
