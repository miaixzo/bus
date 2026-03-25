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
 * A timer specialised for AI/LLM calls, recording TTFT (Time To First Token), ITL (Inter-Token Latency), token usage,
 * and estimated cost atomically.
 * <p>
 * Follows OTel GenAI SIG 2025 semantic conventions (gen_ai.*).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface LlmTimer {

    /**
     * Start timing an LLM call.
     *
     * @param model     model identifier, e.g. "claude-opus-4-6"
     * @param provider  provider name, e.g. "anthropic"
     * @param operation operation type, e.g. "chat" or "embeddings"
     * @return a timing handle
     */
    LlmSample start(String model, String provider, String operation);

}
