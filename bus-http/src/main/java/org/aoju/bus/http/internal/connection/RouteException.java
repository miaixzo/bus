package org.aoju.bus.http.internal.connection;

import org.aoju.bus.http.internal.Internal;

import java.io.IOException;

/**
 * An exception thrown to indicate a problem connecting via a single Route. Multiple attempts may
 * have been made with alternative protocols, none of which were successful.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
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
