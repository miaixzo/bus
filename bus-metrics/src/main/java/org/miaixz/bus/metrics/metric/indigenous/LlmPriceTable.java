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

import java.util.HashMap;
import java.util.Map;

/**
 * Built-in price table for estimating LLM call costs (USD per token). Values sourced from public provider pricing pages
 * (March 2026). Override or extend via {@link #register(String, double, double)}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LlmPriceTable {

    /** Per-million-token prices in USD keyed by model ID; value is {inputUsdPerM, outputUsdPerM}. */
    private static final Map<String, double[]> PRICES = new HashMap<>();

    static {
        PRICES.put("claude-opus-4-6", new double[] { 15.0, 75.0 });
        PRICES.put("claude-sonnet-4-6", new double[] { 3.0, 15.0 });
        PRICES.put("claude-haiku-4-5-20251001", new double[] { 0.8, 4.0 });
        PRICES.put("gpt-4o", new double[] { 5.0, 15.0 });
        PRICES.put("gpt-4o-mini", new double[] { 0.15, 0.6 });
        PRICES.put("gpt-4-turbo", new double[] { 10.0, 30.0 });
        PRICES.put("gemini-1.5-pro", new double[] { 3.5, 10.5 });
        PRICES.put("gemini-1.5-flash", new double[] { 0.075, 0.3 });
    }

    /** Private constructor; this is a static utility class. */
    private LlmPriceTable() {
    }

    /**
     * Register or override the per-token price for a model.
     *
     * @param model               model identifier, e.g. "gpt-4o"
     * @param inputUsdPerMillion  input token price in USD per million tokens
     * @param outputUsdPerMillion output token price in USD per million tokens
     */
    public static void register(String model, double inputUsdPerMillion, double outputUsdPerMillion) {
        PRICES.put(model, new double[] { inputUsdPerMillion, outputUsdPerMillion });
    }

    /**
     * Estimate cost in USD for the given model and token counts. Returns 0 if the model is not in the price table.
     */
    public static double estimateCost(String model, int inputTokens, int outputTokens) {
        double[] p = PRICES.get(model);
        if (p == null) {
            return 0.0;
        }
        return (inputTokens * p[0] + outputTokens * p[1]) / 1_000_000.0;
    }

}
