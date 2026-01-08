/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.accord;

import org.miaixz.bus.http.Builder;

import java.io.IOException;

/**
 * An exception thrown to indicate a problem connecting via a single route. This may be an aggregate of multiple
 * connection attempts, none of which were successful.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RouteException extends RuntimeException {

    private IOException firstException;
    private IOException lastException;

    /**
     * Constructs a new {@code RouteException}.
     *
     * @param cause The initial cause of the exception.
     */
    RouteException(IOException cause) {
        super(cause);
        firstException = cause;
        lastException = cause;
    }

    /**
     * Returns the first exception that was encountered when attempting to connect.
     *
     * @return The first connection exception.
     */
    public IOException getFirstConnectException() {
        return firstException;
    }

    /**
     * Returns the last exception that was encountered when attempting to connect.
     *
     * @return The last connection exception.
     */
    public IOException getLastConnectException() {
        return lastException;
    }

    /**
     * Adds a new connection exception to this exception.
     *
     * @param e The new exception to add.
     */
    void addConnectException(IOException e) {
        Builder.addSuppressedIfPossible(firstException, e);
        lastException = e;
    }

}
