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
package org.miaixz.bus.fabric;

/**
 * Protocol-neutral message filter applied by HTTP, socket, WebSocket, SSE, and STOMP exchanges.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Filter {

    /**
     * Applies this filter to a message and downstream chain.
     *
     * @param message message
     * @param chain   downstream chain
     * @return filtered message
     */
    Message apply(Message message, Chain chain);

    /**
     * Downstream filter chain contract.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    interface Chain {

        /**
         * Proceeds to the next filter.
         *
         * @param message message
         * @return filtered message
         */
        Message proceed(Message message);

    }

}
