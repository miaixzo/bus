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
package org.miaixz.bus.fabric.network;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.fabric.Lifecycle;

/**
 * Protocol-neutral network connection contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Connection extends AutoCloseable, Lifecycle {

    /**
     * Returns the connection destination.
     *
     * @return connection destination
     */
    Destination destination();

    /**
     * Returns the underlying conduit.
     *
     * @return network conduit
     */
    Conduit conduit();

    /**
     * Returns the protocol-layer read view.
     *
     * @return protocol-layer source
     */
    Source source();

    /**
     * Returns the protocol-layer write view.
     *
     * @return protocol-layer sink
     */
    Sink sink();

    /**
     * Returns whether the connection is healthy.
     *
     * @return true when healthy
     */
    boolean healthy();

    /**
     * Returns whether the connection is idle.
     *
     * @return true when idle
     */
    boolean idle();

    /**
     * Closes this connection.
     */
    @Override
    void close();

}
