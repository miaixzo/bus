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

import java.lang.ref.WeakReference;
import java.util.function.ToDoubleFunction;

import org.miaixz.bus.metrics.metric.Gauge;

/**
 * Gauge backed by a weak reference to a state object plus a value function. The weak reference prevents memory leaks
 * when the state object is garbage collected.
 *
 * @param <T> state object type
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeGauge<T> implements Gauge {

    /**
     * Weak reference to the observed state object; prevents memory leaks on GC.
     */
    private final WeakReference<T> stateRef;
    /**
     * Function that extracts the gauge value from the state object.
     */
    private final ToDoubleFunction<T> fn;

    /**
     * Create a gauge backed by a weak reference to {@code stateObj}.
     *
     * @param stateObj the object whose state is observed; held via weak reference
     * @param fn       function that extracts the gauge value from {@code stateObj}
     */
    public NativeGauge(T stateObj, ToDoubleFunction<T> fn) {
        this.stateRef = new WeakReference<>(stateObj);
        this.fn = fn;
    }

    /** Returns the current gauge value, or {@link Double#NaN} if the state object has been GC'd. */
    @Override
    public double value() {
        T obj = stateRef.get();
        return obj == null ? Double.NaN : fn.applyAsDouble(obj);
    }

}
