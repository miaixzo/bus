/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.mq;

/**
 * Defines the contract for a Message Queue (MQ) provider engine. Implementations of this interface are responsible for
 * initializing the MQ system and providing access to message producers and consumers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MQProvider {

    /**
     * Initializes the MQ provider with the given configuration. This method should be called before any producer or
     * consumer operations are performed.
     *
     * @param config The {@link MQConfig} containing the necessary configuration details for the MQ provider.
     * @return This {@code MQProvider} instance, initialized and ready for use.
     */
    MQProvider init(MQConfig config);

    /**
     * Retrieves a {@link Producer} instance associated with this MQ provider. The producer is used for sending messages
     * to the MQ system.
     *
     * @return A {@link Producer} instance for sending messages.
     */
    Producer getProducer();

    /**
     * Retrieves a {@link Consumer} instance associated with this MQ provider. The consumer is used for receiving
     * messages from the MQ system.
     *
     * @return A {@link Consumer} instance for receiving messages.
     */
    Consumer getConsumer();

}
