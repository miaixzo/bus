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
