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

import java.util.concurrent.atomic.LongAdder;

import org.miaixz.bus.metrics.metric.Counter;

/**
 * Lock-free counter backed by {@link LongAdder}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeCounter implements Counter {

    /**
     * Lock-free accumulator for the cumulative event count.
     */
    private final LongAdder adder = new LongAdder();

    /**
     * Increment by one.
     */
    @Override
    public void increment() {
        adder.increment();
    }

    /**
     * Increment by the given amount.
     *
     * @param amount number of events to add
     */
    @Override
    public void increment(long amount) {
        adder.add(amount);
    }

    /** Returns the cumulative count since creation. */
    @Override
    public long count() {
        return adder.sum();
    }

}
