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

import java.util.concurrent.CompletableFuture;

import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Timeout;

/**
 * Protocol-neutral connector contract for opening network connections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Connector extends AutoCloseable {

    /**
     * Opens a network connection.
     *
     * @param address protocol address to connect
     * @param timeout timeout policy governing connection establishment
     * @return future completed with the opened connection or exceptionally when establishment fails
     */
    CompletableFuture<Connection> connect(Address address, Timeout timeout);

    /**
     * Returns whether this connector supports a transport.
     *
     * @param transport transport implementation proposed for connection establishment
     * @return true when this connector can open connections through that transport
     */
    boolean supports(Transport transport);

    /**
     * Closes this connector.
     */
    @Override
    void close();

}
