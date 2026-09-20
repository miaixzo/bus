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
package org.miaixz.bus.spring.context;

/**
 * Lexical lifetime of one installed context snapshot.
 * <p>
 * Scopes are thread-confined, nestable, idempotently closeable, and must be closed in reverse installation order.
 *
 * @author Kimi Liu
 */
public final class ContextScope implements AutoCloseable {

    /**
     * Context frame restored when this scope closes.
     */
    private final ContextCarrier.Frame frame;

    /**
     * Whether this scope has already restored its previous context.
     */
    private boolean closed;

    /**
     * Creates a scope for an installed carrier frame.
     *
     * @param frame installed frame owned by the current thread
     */
    ContextScope(ContextCarrier.Frame frame) {
        this.frame = frame;
    }

    /**
     * Restores the previous context exactly once.
     *
     * @throws IllegalStateException when called by a non-owning thread or out of installation order
     */
    @Override
    public void close() {
        if (!closed) {
            ContextCarrier.close(frame);
            closed = true;
        }
    }

}
