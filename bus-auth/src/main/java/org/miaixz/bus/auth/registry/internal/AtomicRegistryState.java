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
package org.miaixz.bus.auth.registry.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.auth.registry.spi.RegistryView;
import org.miaixz.bus.core.lang.Assert;

/**
 * Publishes complete immutable Registry views with one compare-and-set operation.
 * <p>
 * Readers observe either the entire previous view or the entire replacement view. Failed compilation never reaches this
 * state holder, and concurrent reloads can commit only when their expected view is still current.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AtomicRegistryState {

    /**
     * Atomic reference to the single currently committed immutable view.
     */
    private final AtomicReference<RegistryView> current;

    /**
     * Creates state with an already complete initial Registry view.
     *
     * @param initial complete initial view, normally revision zero
     * @throws IllegalArgumentException if the initial view is {@code null}
     */
    public AtomicRegistryState(final RegistryView initial) {
        this.current = new AtomicReference<>(Assert.notNull(initial, "Initial Registry view must not be null"));
    }

    /**
     * Returns the complete immutable view visible at this instant.
     *
     * @return current complete Registry view
     */
    public RegistryView current() {
        return current.get();
    }

    /**
     * Atomically publishes a replacement only if the expected view remains current.
     *
     * @param expected    exact view observed before compilation
     * @param replacement complete replacement view
     * @return {@code true} when the replacement was committed, otherwise {@code false}
     * @throws IllegalArgumentException if either view is {@code null}
     */
    public boolean replace(final RegistryView expected, final RegistryView replacement) {
        Assert.notNull(expected, "Expected Registry view must not be null");
        Assert.notNull(replacement, "Replacement Registry view must not be null");
        return current.compareAndSet(expected, replacement);
    }

}
