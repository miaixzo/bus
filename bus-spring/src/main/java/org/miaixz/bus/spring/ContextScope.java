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

/**
 * Installs a runtime context snapshot for the lifetime of a lexical scope.
 * <p>
 * Scopes may be nested. Closing a scope restores the exact context that was visible before it was opened, and closing
 * the same scope more than once has no effect.
 *
 * @author Kimi Liu
 */
public class ContextScope implements AutoCloseable {

    /**
     * State visible before this scope was installed.
     */
    private final ContextState previous;

    /**
     * State manager owning the current thread carrier.
     */
    private final ContextManager manager;

    /**
     * Whether the parent state has already been restored.
     */
    private boolean closed;

    /**
     * Installs the supplied snapshot.
     *
     * @param manager  state manager owning the current application context
     * @param snapshot snapshot to install; {@code null} installs the empty snapshot
     */
    public ContextScope(ContextManager manager, ContextState snapshot) {
        this.manager = manager;
        this.previous = manager.install(snapshot);
    }

    /**
     * Restores the parent context. This operation is idempotent.
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            this.manager.restore(this.previous);
        }
    }

}
