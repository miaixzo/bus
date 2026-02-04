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
