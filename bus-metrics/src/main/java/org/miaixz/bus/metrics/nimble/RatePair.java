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

import java.util.Objects;

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Combines a success Meter and an error Meter, providing direct access to error rate and success rate. Useful for
 * circuit-breaker decisions without external PromQL.
 *
 * @author Kimi Liu
 */
public interface RatePair {

    /**
     * Creates a rate pair from provider-owned meters using the canonical plural suffixes.
     *
     * @param provider provider that owns the meters
     * @param name     metric name prefix
     * @param tags     optional tags
     * @return rate pair sharing the provider's meter state
     * @throws IllegalArgumentException if the metric name is blank
     */
    static RatePair create(Provider provider, String name, Tag... tags) {
        Objects.requireNonNull(provider, "Metrics provider must not be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rate-pair name must not be blank");
        }
        Meter total = provider.meter(name + ".total", tags);
        Meter errors = provider.meter(name + ".errors", tags);
        Meter successes = provider.meter(name + ".successes", tags);
        return of(total, errors, successes);
    }

    /**
     * Creates a rate pair from three existing meters.
     *
     * @param total     meter receiving every event
     * @param errors    meter receiving failed events
     * @param successes meter receiving successful events
     * @return delegating rate pair
     */
    static RatePair of(Meter total, Meter errors, Meter successes) {
        Meter checkedTotal = Objects.requireNonNull(total, "Total meter must not be null");
        Meter checkedErrors = Objects.requireNonNull(errors, "Error meter must not be null");
        Meter checkedSuccesses = Objects.requireNonNull(successes, "Success meter must not be null");
        return new RatePair() {

            @Override
            public void recordSuccess() {
                checkedTotal.increment();
                checkedSuccesses.increment();
            }

            @Override
            public void recordError() {
                checkedTotal.increment();
                checkedErrors.increment();
            }

            @Override
            public double errorRate() {
                double totalRate = checkedTotal.oneMinuteRate();
                return totalRate <= 0 ? 0.0 : checkedErrors.oneMinuteRate() / totalRate;
            }

            @Override
            public double successRate() {
                double totalRate = checkedTotal.oneMinuteRate();
                return totalRate <= 0 ? 1.0 : checkedSuccesses.oneMinuteRate() / totalRate;
            }

            @Override
            public Meter total() {
                return checkedTotal;
            }

            @Override
            public Meter errors() {
                return checkedErrors;
            }

            @Override
            public Meter successes() {
                return checkedSuccesses;
            }
        };
    }

    /**
     * Record one successful event into both the total and success meters.
     */
    void recordSuccess();

    /**
     * Record one failed event into both the total and error meters.
     */
    void recordError();

    /**
     * error.oneMinuteRate() / total.oneMinuteRate(); returns 0 if no events.
     *
     * @return error rate
     */
    double errorRate();

    /**
     * success.oneMinuteRate() / total.oneMinuteRate(); returns 1 if no events.
     *
     * @return success rate
     */
    double successRate();

    /**
     * Returns the combined total meter (successes + errors).
     *
     * @return total meter
     */
    Meter total();

    /**
     * Returns the error-only meter.
     *
     * @return error meter
     */
    Meter errors();

    /**
     * Returns the success-only meter.
     *
     * @return success meter
     */
    Meter successes();

}
