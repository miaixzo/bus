package org.miaixz.bus.core.exception;

import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 加解密异常
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CryptoException extends UncheckedException {

    private static final long serialVersionUID = 1L;

    public CryptoException(Throwable e) {
        super(e);
    }

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String messageTemplate, Object... args) {
        super(StringKit.format(messageTemplate, args));
    }

    public CryptoException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CryptoException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
        super(message, throwable, enableSuppression, writableStackTrace);
    }

    public CryptoException(Throwable throwable, String messageTemplate, Object... params) {
        super(StringKit.format(messageTemplate, params), throwable);
    }
}
