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
package org.miaixz.bus.starter.sensitive;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.logger.Executor;
import org.miaixz.bus.sensitive.Sanitizer;

/**
 * Binds one application-context-scoped sanitizer to the logger executor for the context lifecycle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SensitiveBinding implements AutoCloseable {

    /**
     * Sanitizer bound to the logger executor.
     */
    private final Sanitizer sanitizer;

    /**
     * Guards idempotent binding cleanup.
     */
    private final AtomicBoolean active = new AtomicBoolean(true);

    /**
     * Binds the supplied sanitizer immediately.
     *
     * @param sanitizer sanitizer owned by the current application context
     */
    public SensitiveBinding(Sanitizer sanitizer) {
        this.sanitizer = Objects.requireNonNull(sanitizer, "sanitizer");
        Executor.register(sanitizer);
    }

    /**
     * Unbinds the sanitizer when the owning application context closes.
     */
    @Override
    public void close() {
        if (active.compareAndSet(true, false)) {
            Executor.unregister(sanitizer);
        }
    }

}
