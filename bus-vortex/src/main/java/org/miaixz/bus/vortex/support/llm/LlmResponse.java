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
 * Response model for LLM chat completion responses.
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
public class LlmResponse {

    /**
     * Unique identifier for the completion.
     */
    private String id;

    /**
     * The object type (always "chat.completion").
     */
    private String object;

    /**
     * Unix timestamp of when the completion was created.
     */
    private Long created;

    /**
     * The model used for completion.
     */
    private String model;

    /**
     * The list of completion choices.
     */
    private List<Choice> choices;

    /**
     * Usage statistics for the request.
     */
    private Usage usage;

    /**
     * Represents a single completion choice.
     */
    @Getter
    @Setter
    public static class Choice {

        /**
         * The index of this choice.
         */
        private Integer index;

        /**
         * The generated message.
         */
        private Message message;

        /**
         * The reason the completion finished (stop, length, content_filter). JSON field: finish_reason
         */
        private String finishReason;

    }

    /**
     * Represents a message in the response.
     */
    @Getter
    @Setter
    public static class Message {

        /**
         * The role of the message author.
         */
        private String role;

        /**
         * The content of the message.
         */
        private String content;

    }

    /**
     * Token usage stastics.
     */
    @Getter
    @Setter
    public static class Usage {

        /**
         * Number of tokens in the prompt. JSON field: prompt_tokens
         */
        private Integer promptTokens;

        /**
         * Number of tokens in the completion. JSON field: completion_tokens
         */
        private Integer completionTokens;

        /**
         * Total number of tokens used. JSON field: total_tokens
         */
        private Integer totalTokens;

    }

}
