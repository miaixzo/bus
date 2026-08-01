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
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * Immutable snapshot of the framework request context.
 * <p>
 * Snapshot instances never retain servlet objects, request caches, or thread-local containers. The static carrier is
 * package-private so context lifecycle classes can share one source without exposing mutable state to callers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RuntimeContextSnapshot {

    private static final RuntimeContextSnapshot EMPTY = new RuntimeContextSnapshot(null, null);

    private static final ThreadLocal<RuntimeContextSnapshot> CURRENT = ThreadKit.newThreadLocal(false);

    private final String requestId;

    private final Authorize authorize;

    private RuntimeContextSnapshot(String requestId, Authorize authorize) {
        this.requestId = requestId;
        this.authorize = copy(authorize);
    }

    /**
     * Captures the context visible to the current thread.
     *
     * @return an immutable context snapshot
     */
    public static RuntimeContextSnapshot capture() {
        RuntimeContextSnapshot current = CURRENT.get();
        if (current != null) {
            return current.detachedCopy();
        }
        return of(ContextBuilder.getRequestId(), ContextBuilder.getAuthorize());
    }

    /**
     * Returns the empty snapshot.
     *
     * @return the empty snapshot
     */
    public static RuntimeContextSnapshot empty() {
        return EMPTY;
    }

    /**
     * Creates a snapshot from explicit immutable context values.
     *
     * @param requestId request correlation identifier
     * @param authorize authenticated authorization information
     * @return a detached snapshot
     */
    public static RuntimeContextSnapshot of(String requestId, Authorize authorize) {
        if (requestId == null && authorize == null) {
            return EMPTY;
        }
        return new RuntimeContextSnapshot(requestId, authorize);
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

    static RuntimeContextSnapshot replaceCurrent(RuntimeContextSnapshot snapshot) {
        RuntimeContextSnapshot previous = currentSnapshot();
        RuntimeContextSnapshot replacement = normalize(snapshot);
        if (replacement.isEmpty()) {
            CURRENT.remove();
        } else {
            CURRENT.set(replacement.detachedCopy());
        }
        return previous;
    }

    static RuntimeContextSnapshot currentSnapshot() {
        RuntimeContextSnapshot current = CURRENT.get();
        return current == null ? EMPTY : current.detachedCopy();
    }

    static String currentRequestId() {
        RuntimeContextSnapshot current = CURRENT.get();
        return current == null ? null : current.requestId;
    }

    static Authorize currentAuthorize() {
        RuntimeContextSnapshot current = CURRENT.get();
        return current == null ? null : copy(current.authorize);
    }

    static void setCurrentRequestId(String requestId) {
        RuntimeContextSnapshot current = CURRENT.get();
        setCurrent(of(requestId, current == null ? null : current.authorize));
    }

    static void setCurrentAuthorize(Authorize authorize) {
        RuntimeContextSnapshot current = CURRENT.get();
        setCurrent(of(current == null ? null : current.requestId, authorize));
    }

    static void clearCurrent() {
        CURRENT.remove();
    }

    private static RuntimeContextSnapshot normalize(RuntimeContextSnapshot snapshot) {
        return snapshot == null ? EMPTY : snapshot;
    }

    private static void setCurrent(RuntimeContextSnapshot snapshot) {
        if (snapshot.isEmpty()) {
            CURRENT.remove();
        } else {
            CURRENT.set(snapshot);
        }
    }

    private RuntimeContextSnapshot detachedCopy() {
        return isEmpty() ? EMPTY : new RuntimeContextSnapshot(requestId, authorize);
    }

    private static Authorize copy(Authorize authorize) {
        return authorize == null ? null : ObjectKit.clone(authorize);
    }

}
