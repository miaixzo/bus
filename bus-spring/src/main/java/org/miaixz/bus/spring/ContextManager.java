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
package org.miaixz.bus.spring;

import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * Owns the runtime context state for one Spring application context.
 * <p>
 * Every manager has an independent thread-local carrier. Installing an empty state removes the carrier value instead of
 * retaining an empty object on pooled threads.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ContextManager {

    /**
     * Creates an independent context state manager.
     */
    public ContextManager() {
        // No initialization required.
    }

    /**
     * Thread-local context stack owned exclusively by this manager instance.
     */
    private final ThreadLocal<ContextState> current = ThreadKit.newThreadLocal(false);

    /**
     * Captures the current immutable state.
     *
     * @return current state, or the shared empty value when none is installed
     */
    public ContextState capture() {
        ContextState state = this.current.get();
        return state == null ? ContextState.empty() : state;
    }

    /**
     * Installs a state and returns the state previously visible to this manager.
     *
     * @param state state to install; {@code null} installs the empty state
     * @return previous state
     */
    public ContextState install(ContextState state) {
        ContextState previous = capture();
        restore(state);
        return previous;
    }

    /**
     * Restores an exact state, removing the thread-local value for an empty state.
     *
     * @param state state to restore; {@code null} clears the state
     */
    public void restore(ContextState state) {
        if (state == null || state.isEmpty()) {
            this.current.remove();
        } else {
            this.current.set(state);
        }
    }

    /**
     * Removes the current thread state owned by this manager.
     */
    public void clear() {
        this.current.remove();
    }

}
