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
package org.miaixz.bus.metrics.nimble;

/**
 * Timing handle for a single LLM call, returned by {@link LlmTimer#start}.
 * <p>
 * Calling {@link #stop} records duration, available streaming latencies, token usage, and known-model cost. Calling
 * {@link #error} additionally records the error family. Only the first terminal call records metrics.
 *
 * @author Kimi Liu
 */
public interface LlmSample {

    /**
     * Call this when the first token arrives. Repeated calls do not replace the first timestamp.
     */
    void recordFirstToken();

    /**
     * Finalise the call and record all metrics.
     *
     * @param inputTokens  non-negative number of prompt tokens consumed
     * @param outputTokens non-negative number of completion tokens generated
     * @param finishReason "stop" / "length" / "tool_calls" / "error"
     */
    void stop(int inputTokens, int outputTokens, String finishReason);

    /**
     * Records a call-level error, sets the finish reason to {@code error}, and terminates the sample.
     *
     * @param t the throwable
     */
    void error(Throwable t);

}
