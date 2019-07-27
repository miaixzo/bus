package org.aoju.bus.http.internal.http.second;

import java.io.IOException;

/**
 * Thrown when an HTTP/2 stream is canceled without damage to the socket that carries it.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class StreamResetException extends IOException {

    public final ErrorCode errorCode;

    public StreamResetException(ErrorCode errorCode) {
        super("stream was reset: " + errorCode);
        this.errorCode = errorCode;
    }

}
