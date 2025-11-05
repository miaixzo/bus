/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.notify.magic;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents the notice or content of a message template.
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    /**
     * The URL associated with the notice, if any.
     */
    protected String url;
    /**
     * The sender of the message.
     */
    protected String sender;

    /**
     * The recipient(s) of the message, typically comma-separated.
     */
    protected String receive;

    /**
     * The subject of the message.
     */
    protected String subject;

    /**
     * The main content of the message. Limited to 28K characters.
     */
    protected String content;

    /**
     * The template or template ID to be used.
     */
    protected String template;

    /**
     * The signature or signature ID for the message.
     */
    protected String signature;

    /**
     * Parameters for the message template.
     */
    protected String params;

    /**
     * Extension fields or additional properties.
     */
    protected Map<String, Object> extend;

    /**
     * The type of the content.
     */
    protected Type type;

    /**
     * The sending mode of the message.
     */
    protected Mode mode;

    /**
     * Enumerates the types of content that can be sent.
     */
    public enum Type {
        /**
         * HTML content type.
         */
        HTML,
        /**
         * Plain text content type.
         */
        TEXT,
        /**
         * Voice message content type.
         */
        VOICE,
        /**
         * File content type.
         */
        FILE,
        /**
         * Other content type not explicitly defined.
         */
        OTHER
    }

    /**
     * Enumerates the sending modes for messages.
     */
    public enum Mode {
        /**
         * Single message sending mode.
         */
        SINGLE,
        /**
         * Batch message sending mode.
         */
        BATCH
    }

}
