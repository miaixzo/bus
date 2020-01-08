package org.aoju.bus.core.lang.exception;

public class SignatureException extends UncheckedException {

    public SignatureException() {
        super();
    }

    public SignatureException(Throwable t) {
        super(t);
    }

    public SignatureException(String format, Object... args) {
        super(format, args);
    }

    public SignatureException(String message) {
        super(message);
    }

    public SignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignatureException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
