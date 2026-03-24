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
package org.miaixz.bus.metrics.metric;

/**
 * Timing handle for a single LLM call, returned by {@link LlmTimer#start}.
 * <p>
 * Calling {@link #stop} records six metrics atomically: llm.call.duration, llm.call.ttft, llm.call.itl, llm.tokens,
 * llm.cost, llm.errors.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface LlmSample {

    /**
     * Call this when the first token arrives (streaming responses). Records TTFT (Time To First Token).
     */
    void recordFirstToken();

    /**
     * Finalise the call and record all metrics.
     *
     * @param inputTokens  number of prompt tokens consumed
     * @param outputTokens number of completion tokens generated
     * @param finishReason "stop" / "length" / "tool_calls" / "error"
     */
    void stop(int inputTokens, int outputTokens, String finishReason);

    /**
     * Record a call-level error. Sets finishReason to "error" and increments llm.errors.
     *
     * @param t the throwable
     */
    void error(Throwable t);

}
