package org.aoju.bus.core.io.watch;

import org.aoju.bus.core.utils.StringUtils;

/**
 * 监听异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class WatchException extends RuntimeException {

    private static final long serialVersionUID = 8068509879445395353L;

    public WatchException(Throwable e) {
        super(e.getMessage(), e);
    }

    public WatchException(String message) {
        super(message);
    }

    public WatchException(String messageTemplate, Object... params) {
        super(StringUtils.format(messageTemplate, params));
    }

    public WatchException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public WatchException(Throwable throwable, String messageTemplate, Object... params) {
        super(StringUtils.format(messageTemplate, params), throwable);
    }

}
