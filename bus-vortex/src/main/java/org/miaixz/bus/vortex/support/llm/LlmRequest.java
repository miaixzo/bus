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
package org.miaixz.bus.vortex.support.llm;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Request model for LLM chat completion requests.
 * <p>
 * This model follows the OpenAIProvider Chat Completions API format. JsonKit will automatically handle snake_case to
 * camelCase conversion.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {

    /**
     * The model to use for completion (e.g., "gpt-4", "llama3-70b").
     */
    private String model;

    /**
     * The list of messages in the conversation.
     */
    private List<Message> messages;

    /**
     * Whether to stream the response (default: false).
     */
    private boolean stream = false;

    /**
     * Sampling temperature (0.0 to 2.0). Higher values make output more random.
     */
    private Double temperature;

    /**
     * Maximum number of tokens to generate. JSON field: max_tokens
     */
    private Integer maxTokens;

    /**
     * Nucleus sampling parameter (0.0 to 1.0). JSON field: top_p
     */
    private Double topP;

    /**
     * Frequency penalty (-2.0 to 2.0). JSON field: frequency_penalty
     */
    private Double frequencyPenalty;

    /**
     * Presence penalty (-2.0 to 2.0). JSON field: presence_penalty
     */
    private Double presencePenalty;

    /**
     * Stop sequences to end generation.
     */
    private List<String> stop;

    /**
     * Unique identifier for the end-user.
     */
    private String user;

    /**
     * Represents a single message in the conversation.
     */
    @Getter
    @Setter
    public static class Message {

        /**
         * The role of the message author (system, user, assistant).
         */
        private String role;

        /**
         * The content of the message.
         */
        private String content;

        /**
         * Optional name of the author.
         */
        private String name;
    }

}
