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

import org.miaixz.bus.metrics.metric.Meter;
import org.miaixz.bus.metrics.metric.RatePair;

/**
 * RatePair backed by two NativeMeter instances (total + errors).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeRatePair implements RatePair {

    /**
     * Meter tracking all events (successes + errors).
     */
    private final NativeMeter totalMeter = new NativeMeter();
    /**
     * Meter tracking error events only.
     */
    private final NativeMeter errorMeter = new NativeMeter();
    /**
     * Meter tracking success events only.
     */
    private final NativeMeter successMeter = new NativeMeter();

    /** Record one successful event into total and success meters. */
    @Override
    public void recordSuccess() {
        totalMeter.increment();
        successMeter.increment();
    }

    /** Record one failed event into total and error meters. */
    @Override
    public void recordError() {
        totalMeter.increment();
        errorMeter.increment();
    }

    /** Returns error.oneMinuteRate() / total.oneMinuteRate(); 0.0 if no events. */
    @Override
    public double errorRate() {
        double total = totalMeter.oneMinuteRate();
        return total <= 0 ? 0.0 : errorMeter.oneMinuteRate() / total;
    }

    /** Returns success.oneMinuteRate() / total.oneMinuteRate(); 1.0 if no events. */
    @Override
    public double successRate() {
        double total = totalMeter.oneMinuteRate();
        return total <= 0 ? 1.0 : successMeter.oneMinuteRate() / total;
    }

    /** Returns the combined total meter. */
    @Override
    public Meter total() {
        return totalMeter;
    }

    /** Returns the error-only meter. */
    @Override
    public Meter errors() {
        return errorMeter;
    }

    /** Returns the success-only meter. */
    @Override
    public Meter successes() {
        return successMeter;
    }

    /** Tick all three meters — called by the global 5s scheduler. */
    public void tick() {
        totalMeter.tick();
        errorMeter.tick();
        successMeter.tick();
    }

}
