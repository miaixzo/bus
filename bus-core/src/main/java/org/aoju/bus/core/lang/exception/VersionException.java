package org.aoju.bus.core.lang.exception;

/**
 * 自定义异常: 版本异常
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class VersionException extends UncheckedException {

    private static final long serialVersionUID = 1L;

    public VersionException() {
        super();
    }

    public VersionException(Throwable cause) {
        super(cause);
    }

    public VersionException(String format, Object... args) {
        super(format, args);
    }

    public VersionException(String message) {
        super(message);
    }

    public VersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public VersionException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
