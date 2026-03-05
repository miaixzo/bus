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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface for Large Language Model (LLM) providers.
 * <p>
 * This interface defines the contract for LLM providers that can handle chat completion requests in both streaming and
 * non-streaming modes. Implementations should support the OpenAIProvider Chat Completions API format.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface LlmProvider {

    /**
     * Gets the type of this provider (e.g., "vllm", "ollama", "openai").
     *
     * @return The provider type.
     */
    String getType();

    /**
     * Gets the base URL of the LLM service.
     *
     * @return The base URL.
     */
    String getUrl();

    /**
     * Sends a streaming chat completion request to the LLM service.
     * <p>
     * The response is returned as Server-Sent Events (SSE) in the format: {@code data:
     * {"choices":[{"delta":{"content":"token"}}]}}
     *
     * @param request The LLM request containing messages, model, and parameters.
     * @return A {@link Flux} emitting SSE-formatted response chunks.
     */
    Flux<String> stream(LlmRequest request);

    /**
     * Sends a non-streaming chat completion request to the LLM service.
     *
     * @param request The LLM request containing messages, model, and parameters.
     * @return A {@link Mono} emitting the complete response.
     */
    Mono<LlmResponse> chat(LlmRequest request);

}
