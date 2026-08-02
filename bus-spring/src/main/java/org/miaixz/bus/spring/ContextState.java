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

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Immutable snapshot of the framework request context.
 * <p>
 * Snapshot instances copy authorization data and never retain servlet objects, request caches, or thread-local
 * containers, allowing the captured values to cross execution boundaries safely.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ContextState {

    /**
     * Shared empty immutable state.
     */
    private static final ContextState EMPTY = new ContextState(null, null);

    /**
     * Captured request correlation identifier.
     */
    private final String requestId;

    /**
     * Detached captured authorization information.
     */
    private final Authorize authorize;

    /**
     * Creates an immutable detached state.
     *
     * @param requestId request correlation identifier
     * @param authorize authorization information
     */
    private ContextState(String requestId, Authorize authorize) {
        this.requestId = requestId;
        this.authorize = copy(authorize);
    }

    /**
     * Returns the empty snapshot.
     *
     * @return the empty snapshot
     */
    public static ContextState empty() {
        return EMPTY;
    }

    /**
     * Creates a snapshot from explicit immutable context values.
     *
     * @param requestId request correlation identifier
     * @param authorize authenticated authorization information
     * @return a detached snapshot
     */
    public static ContextState of(String requestId, Authorize authorize) {
        if (requestId == null && authorize == null) {
            return EMPTY;
        }
        return new ContextState(requestId, authorize);
    }

    /**
     * Gets the captured request identifier.
     *
     * @return the request identifier, or {@code null}
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Gets a detached copy of the captured authorization information.
     *
     * @return authorization information, or {@code null}
     */
    public Authorize getAuthorize() {
        return copy(authorize);
    }

    /**
     * Tests whether this snapshot contains no context values.
     *
     * @return {@code true} when both values are absent
     */
    public boolean isEmpty() {
        return requestId == null && authorize == null;
    }

    /**
     * Creates a defensive copy of authorization information.
     *
     * @param authorize source authorization information
     * @return detached copy, or {@code null}
     */
    private static Authorize copy(Authorize authorize) {
        return authorize == null ? null : ObjectKit.clone(authorize);
    }

}
