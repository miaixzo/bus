/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
