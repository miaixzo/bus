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
 * Observer wrapper that prevents observation failures from escaping.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SafeEventObserver implements EventObserver {

    /**
     * Delegate observer.
     */
    private final EventObserver delegate;

    /**
     * Creates a wrapper.
     *
     * @param delegate delegate observer
     */
    SafeEventObserver(final EventObserver delegate) {
        this.delegate = Assert.notNull(delegate, () -> new ValidateException("Observer must not be null"));
    }

    @Override
    public void emit(final FabricEvent event) {
        final FabricEvent checkedEvent = Assert
                .notNull(event, () -> new ValidateException("Fabric event must not be null"));
        try {
            delegate.emit(checkedEvent);
        } catch (final RuntimeException ignored) {
            // Observation is intentionally best-effort on request critical paths.
        }
    }

}
