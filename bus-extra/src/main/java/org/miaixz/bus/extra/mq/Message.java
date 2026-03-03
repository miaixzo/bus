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
package org.miaixz.bus.extra.mq;

import java.nio.charset.Charset;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Represents a generic message in a Message Queue (MQ) system. This interface defines the basic structure and
 * operations for a message, including retrieving its topic and content.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Message {

    /**
     * Retrieves the topic associated with this message. The topic is used to categorize messages and for routing in MQ
     * systems.
     *
     * @return The topic of the message as a {@link String}.
     */
    String topic();

    /**
     * Retrieves the raw content of the message as a byte array. This method provides the message payload in its
     * original binary format.
     *
     * @return The message content as a {@code byte[]}.
     */
    byte[] content();

    /**
     * Retrieves the message content as a string, decoding it using the specified character set. This is a convenience
     * method for converting the byte array content into a readable string.
     *
     * @param charset The {@link Charset} to use for decoding the message content.
     * @return The message content as a {@link String} decoded with the given charset.
     */
    default String content(final Charset charset) {
        return StringKit.toString(charset, charset);
    }

}
