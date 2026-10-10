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
package org.miaixz.bus.metrics.nimble.indigenous;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.nimble.LlmSample;
import org.miaixz.bus.metrics.nimble.LlmTimer;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Native LLM timer recording TTFT, ITL, token usage, and cost atomically. Follows OTel GenAI SIG 2025 semantic
 * conventions.
 *
 * @author Kimi Liu
 */
public class NativeLlmTimer implements LlmTimer {

    /**
     * Dynamic semantic keys that callers may not duplicate in base tags.
     */
    private static final Set<String> RESERVED_TAG_KEYS = Set.of(
            Builder.TAG_MODEL,
            Builder.TAG_PROVIDER,
            Builder.TAG_OPERATION,
            Builder.TAG_FINISH_REASON,
            Builder.TAG_TYPE,
            Builder.TAG_ERROR_TYPE);

    /**
     * Base metric name; suffixes are appended for each derived instrument.
     */
    private final String name;

    /**
     * Tags applied to all derived metrics created by this timer.
     */
    private final Tag[] baseTags;

    /**
     * Provider used to create sub-metrics.
     */
    private final Provider provider;

    /**
     * Create a new NativeLlmTimer.
     *
     * @param name     base metric name
     * @param baseTags tags applied to all derived metrics
     * @param provider the NativeProvider used to create sub-metrics
     */
    public NativeLlmTimer(String name, Tag[] baseTags, NativeProvider provider) {
        this(name, baseTags, (Provider) provider);
    }

    /**
     * Creates a backend-neutral LLM timer.
     *
     * @param name     base metric name
     * @param baseTags tags applied to every derived metric
     * @param provider provider used to create derived instruments
     * @throws IllegalArgumentException if the name is blank or a base tag uses a reserved dynamic key
     */
    public NativeLlmTimer(String name, Tag[] baseTags, Provider provider) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("LLM timer name must not be blank");
        }
        this.name = name;
        this.baseTags = baseTags == null ? new Tag[0] : baseTags.clone();
        for (Tag tag : this.baseTags) {
            Objects.requireNonNull(tag, "LLM base tag must not be null");
            if (RESERVED_TAG_KEYS.contains(tag.key())) {
                throw new IllegalArgumentException("LLM base tag uses reserved key: " + tag.key());
            }
        }
        this.provider = Objects.requireNonNull(provider, "Metrics provider must not be null");
    }

    /**
     * Combines immutable base tags with per-call semantic tags.
     *
     * @param dynamicTags semantic tags for one derived metric
     * @return combined tag array
     */
    private Tag[] tags(Tag... dynamicTags) {
        Tag[] combined = Arrays.copyOf(baseTags, baseTags.length + dynamicTags.length);
        System.arraycopy(dynamicTags, 0, combined, baseTags.length, dynamicTags.length);
        return combined;
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
        if (model == null || model.isBlank() || provider_ == null || provider_.isBlank() || operation == null
                || operation.isBlank()) {
            throw new IllegalArgumentException("LLM model, provider, and operation must not be blank");
        }
        long startNs = System.nanoTime();
        return new NativeLlmSample(startNs, model, provider_, operation);
    }

    /**
     * The native llm sample class.
     *
     * @author Kimi Liu
     */
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
        private final AtomicLong firstTokenNs = new AtomicLong(-1);

        /**
         * Ensures that a sample records exactly one terminal outcome.
         */
        private final AtomicBoolean terminated = new AtomicBoolean();

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
            firstTokenNs.compareAndSet(-1, System.nanoTime());
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
            if (inputTokens < 0 || outputTokens < 0) {
                throw new IllegalArgumentException("LLM token counts must be non-negative");
            }
            if (finishReason == null || finishReason.isBlank()) {
                throw new IllegalArgumentException("LLM finish reason must not be blank");
            }
            if (!terminated.compareAndSet(false, true)) {
                return;
            }
            finish(inputTokens, outputTokens, finishReason);
        }

        /**
         * Records one terminal outcome after the atomic termination transition succeeds.
         *
         * @param inputTokens  non-negative input token count
         * @param outputTokens non-negative output token count
         * @param finishReason terminal reason
         */
        private void finish(int inputTokens, int outputTokens, String finishReason) {
            long endNs = System.nanoTime();
            long totalNs = endNs - startNs;

            provider.timer(
                    name + Builder.LLM_SUFFIX_DURATION,
                    tags(
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, providerName),
                            Tag.of(Builder.TAG_OPERATION, operation),
                            Tag.of(Builder.TAG_FINISH_REASON, finishReason)))
                    .record(totalNs, TimeUnit.NANOSECONDS);

            long observedFirstToken = firstTokenNs.get();
            if (observedFirstToken > 0) {
                long ttftNs = observedFirstToken - startNs;
                provider.timer(
                        name + Builder.LLM_SUFFIX_TTFT,
                        tags(
                                Tag.of(Builder.TAG_MODEL, model),
                                Tag.of(Builder.TAG_PROVIDER, providerName),
                                Tag.of(Builder.TAG_OPERATION, operation)))
                        .record(ttftNs, TimeUnit.NANOSECONDS);

                if (outputTokens > 1) {
                    long itlNs = (totalNs - ttftNs) / (outputTokens - 1);
                    provider.timer(
                            name + Builder.LLM_SUFFIX_ITL,
                            tags(
                                    Tag.of(Builder.TAG_MODEL, model),
                                    Tag.of(Builder.TAG_PROVIDER, providerName),
                                    Tag.of(Builder.TAG_OPERATION, operation)))
                            .record(itlNs, TimeUnit.NANOSECONDS);
                }
            }

            provider.counter(
                    name + Builder.LLM_SUFFIX_TOKENS,
                    tags(
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, providerName),
                            Tag.of(Builder.TAG_OPERATION, operation),
                            Tag.of(Builder.TAG_TYPE, "input")))
                    .increment(inputTokens);
            provider.counter(
                    name + Builder.LLM_SUFFIX_TOKENS,
                    tags(
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, providerName),
                            Tag.of(Builder.TAG_OPERATION, operation),
                            Tag.of(Builder.TAG_TYPE, "output")))
                    .increment(outputTokens);

            double cost = LlmPriceTable.estimateCost(model, inputTokens, outputTokens);
            if (cost > 0) {
                long scaledCost = Math.round(cost * Builder.LLM_COST_SCALE);
                provider.counter(
                        name + Builder.LLM_SUFFIX_COST,
                        tags(
                                Tag.of(Builder.TAG_MODEL, model),
                                Tag.of(Builder.TAG_PROVIDER, providerName),
                                Tag.of(Builder.TAG_OPERATION, operation)))
                        .increment(scaledCost);
            }
        }

        /**
         * Records an error counter and delegates to {@link #stop} with zero tokens.
         *
         * @param t the throwable that caused the error
         */
        @Override
        public void error(Throwable t) {
            Throwable checked = Objects.requireNonNull(t, "LLM error must not be null");
            if (!terminated.compareAndSet(false, true)) {
                return;
            }
            provider.counter(
                    name + Builder.LLM_SUFFIX_ERRORS,
                    tags(
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, providerName),
                            Tag.of(Builder.TAG_OPERATION, operation),
                            Tag.of(Builder.TAG_ERROR_TYPE, checked.getClass().getSimpleName())))
                    .increment();
            finish(0, 0, "error");
        }

    }

}
