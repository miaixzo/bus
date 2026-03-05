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
