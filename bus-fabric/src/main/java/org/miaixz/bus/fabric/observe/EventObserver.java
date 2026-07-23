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
package org.miaixz.bus.fabric.observe;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.observe.event.FabricEvent;

/**
 * Observer contract for fabric lifecycle events.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface EventObserver {

    /**
     * Returns a no-operation observer.
     *
     * @return shared observer that ignores every event
     */
    static EventObserver noop() {
        return NoopEventObserver.instance();
    }

    /**
     * Returns an observer that suppresses runtime failures thrown by a delegate during valid event delivery.
     *
     * @param observer delegate observer, or {@code null} to use the no-operation observer
     * @return no-op observer for null/no-op input, the same instance when already safe, or a new safe wrapper
     */
    static EventObserver safe(final EventObserver observer) {
        if (observer == null || observer == noop()) {
            return noop();
        }
        if (observer instanceof SafeEventObserver) {
            return observer;
        }
        return new SafeEventObserver(observer);
    }

    /**
     * Emits an event object.
     *
     * @param event lifecycle event delivered to this observer
     */
    void emit(FabricEvent event);

    /**
     * Combines this observer with a second observer using ordered, best-effort runtime-exception isolation.
     *
     * @param next non-null observer invoked after this observer
     * @return ordered composite that attempts both observers for each valid event
     * @throws ValidateException if {@code next} is {@code null}
     */
    default EventObserver and(final EventObserver next) {
        return new ChainedEventObserver(this,
                Assert.notNull(next, () -> new ValidateException("Next observer must not be null")));
    }

}
