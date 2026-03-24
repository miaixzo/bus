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
 * @since Java 21+
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
