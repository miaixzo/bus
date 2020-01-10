package org.aoju.bus.core.lang.exception;


public class InvalidClaimException extends UncheckedException {

    public InvalidClaimException() {
        super();
    }

    public InvalidClaimException(Throwable t) {
        super(t);
    }

    public InvalidClaimException(String format, Object... args) {
        super(format, args);
    }

    public InvalidClaimException(String message) {
        super(message);
    }

    public InvalidClaimException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidClaimException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
