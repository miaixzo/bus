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
 * @since Java 21+
 */
public final class RuntimeContextScope implements AutoCloseable {

    private final RuntimeContextSnapshot previous;

    private boolean closed;

    /**
     * Installs the supplied snapshot.
     *
     * @param snapshot snapshot to install; {@code null} installs the empty snapshot
     */
    public RuntimeContextScope(RuntimeContextSnapshot snapshot) {
        previous = RuntimeContextSnapshot.replaceCurrent(snapshot);
    }

    /**
     * Opens a scope for the supplied snapshot.
     *
     * @param snapshot snapshot to install
     * @return the opened scope
     */
    public static RuntimeContextScope open(RuntimeContextSnapshot snapshot) {
        return new RuntimeContextScope(snapshot);
    }

    /**
     * Restores the parent context. This operation is idempotent.
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            RuntimeContextSnapshot.replaceCurrent(previous);
        }
    }

}
