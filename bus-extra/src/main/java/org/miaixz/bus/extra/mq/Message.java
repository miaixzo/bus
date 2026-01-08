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
