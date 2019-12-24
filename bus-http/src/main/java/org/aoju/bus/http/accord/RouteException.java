package org.aoju.bus.http.accord;

import org.aoju.bus.http.Internal;

import java.io.IOException;

/**
 * 抛出异常，以指示通过单一路由连接的问题。
 * 可能已经用替代协议进行了多次尝试，但没有一次成功
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public final class RouteException extends RuntimeException {

    private IOException firstException;
    private IOException lastException;

    public RouteException(IOException cause) {
        super(cause);
        firstException = cause;
        lastException = cause;
    }

    public IOException getFirstConnectException() {
        return firstException;
    }

    public IOException getLastConnectException() {
        return lastException;
    }

    public void addConnectException(IOException e) {
        Internal.addSuppressedIfPossible(firstException, e);
        lastException = e;
    }

}
