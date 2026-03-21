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
package org.miaixz.bus.metrics.metric.indigenous;

import java.util.concurrent.TimeUnit;

import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.metric.LlmSample;
import org.miaixz.bus.metrics.metric.LlmTimer;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Native LLM timer recording TTFT, ITL, token usage, and cost atomically. Follows OTel GenAI SIG 2025 semantic
 * conventions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeLlmTimer implements LlmTimer {

    /**
     * Base metric name; suffixes are appended for each derived instrument.
     */
    private final String name;
    /**
     * Tags applied to all derived metrics created by this timer.
     */
    private final Tag[] baseTags;
    /**
     * NativeProvider used to create sub-metrics (timers, counters).
     */
    private final NativeProvider provider;

    /**
     * Create a new NativeLlmTimer.
     *
     * @param name     base metric name
     * @param baseTags tags applied to all derived metrics
     * @param provider the NativeProvider used to create sub-metrics
     */
    public NativeLlmTimer(String name, Tag[] baseTags, NativeProvider provider) {
        this.name = name;
        this.baseTags = baseTags;
        this.provider = provider;
    }

    /**
     * Start timing an LLM call.
     *
     * @param model     model identifier, e.g. "claude-sonnet-4-6"
     * @param provider_ provider name, e.g. "anthropic"
     * @param operation operation type, e.g. "chat" or "embeddings"
     * @return a timing handle
     */
    @Override
    public LlmSample start(String model, String provider_, String operation) {
        long startNs = System.nanoTime();
        return new NativeLlmSample(startNs, model, provider_, operation);
    }

    private class NativeLlmSample implements LlmSample {

        /**
         * Nanosecond timestamp when this sample was started.
         */
        private final long startNs;
        /**
         * Model identifier, e.g. "claude-sonnet-4-6".
         */
        private final String model;
        /**
         * Provider name, e.g. "anthropic".
         */
        private final String providerName;
        /**
         * Operation type, e.g. "chat" or "embeddings".
         */
        private final String operation;
        /**
         * Nanosecond timestamp of the first token; -1 if not yet recorded.
         */
        private volatile long firstTokenNs = -1;

        /**
         * Creates a new NativeLlmSample.
         *
         * @param startNs      nanosecond timestamp when timing started
         * @param model        model identifier
         * @param providerName provider name
         * @param operation    operation type
         */
        NativeLlmSample(long startNs, String model, String providerName, String operation) {
            this.startNs = startNs;
            this.model = model;
            this.providerName = providerName;
            this.operation = operation;
        }

        /**
         * Records the nanosecond timestamp of the first token received.
         */
        @Override
        public void recordFirstToken() {
            firstTokenNs = System.nanoTime();
        }

        /**
         * Stops the sample and records duration, TTFT, ITL, token counts, and cost.
         *
         * @param inputTokens  number of input tokens consumed
         * @param outputTokens number of output tokens generated
         * @param finishReason reason the generation stopped (e.g. "stop", "length", "error")
         */
        @Override
        public void stop(int inputTokens, int outputTokens, String finishReason) {
            long endNs = System.nanoTime();
            long totalNs = endNs - startNs;

            // 1. Total duration
            provider.timer(
                    name + Builder.LLM_SUFFIX_DURATION,
                    Tag.of(Builder.TAG_MODEL, model),
                    Tag.of(Builder.TAG_PROVIDER, providerName),
                    Tag.of(Builder.TAG_OPERATION, operation),
                    Tag.of(Builder.TAG_FINISH_REASON, finishReason)).record(totalNs, TimeUnit.NANOSECONDS);

            // 2. TTFT
            if (firstTokenNs > 0) {
                long ttftNs = firstTokenNs - startNs;
                provider.timer(
                        name + Builder.LLM_SUFFIX_TTFT,
                        Tag.of(Builder.TAG_MODEL, model),
                        Tag.of(Builder.TAG_PROVIDER, providerName)).record(ttftNs, TimeUnit.NANOSECONDS);

                // 3. ITL = (total - ttft) / (outputTokens - 1)
                if (outputTokens > 1) {
                    long itlNs = (totalNs - ttftNs) / (outputTokens - 1);
                    provider.timer(
                            name + Builder.LLM_SUFFIX_ITL,
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, providerName)).record(itlNs, TimeUnit.NANOSECONDS);
                }
            }

            // 4. Token counts
            provider.counter(
                    name + Builder.LLM_SUFFIX_TOKENS,
                    Tag.of(Builder.TAG_MODEL, model),
                    Tag.of(Builder.TAG_PROVIDER, providerName),
                    Tag.of(Builder.TAG_TYPE, "input")).increment(inputTokens);
            provider.counter(
                    name + Builder.LLM_SUFFIX_TOKENS,
                    Tag.of(Builder.TAG_MODEL, model),
                    Tag.of(Builder.TAG_PROVIDER, providerName),
                    Tag.of(Builder.TAG_TYPE, "output")).increment(outputTokens);

            // 5. Cost estimation (USD * 1000 stored as long microdollars)
            double cost = LlmPriceTable.estimateCost(model, inputTokens, outputTokens);
            if (cost > 0) {
                provider.counter(
                        name + Builder.LLM_SUFFIX_COST,
                        Tag.of(Builder.TAG_MODEL, model),
                        Tag.of(Builder.TAG_PROVIDER, providerName)).increment((long) (cost * Builder.LLM_COST_SCALE));
            }
        }

        /**
         * Records an error counter and delegates to {@link #stop} with zero tokens.
         *
         * @param t the throwable that caused the error
         */
        @Override
        public void error(Throwable t) {
            provider.counter(
                    name + Builder.LLM_SUFFIX_ERRORS,
                    Tag.of(Builder.TAG_MODEL, model),
                    Tag.of(Builder.TAG_PROVIDER, providerName),
                    Tag.of(Builder.TAG_ERROR_TYPE, t.getClass().getSimpleName())).increment();
            stop(0, 0, "error");
        }
    }

}
