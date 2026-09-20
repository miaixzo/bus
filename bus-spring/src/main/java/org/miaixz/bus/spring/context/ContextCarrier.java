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

import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * Package-private carrier for nested execution-context frames.
 *
 * @author Kimi Liu
 */
final class ContextCarrier {

    /**
     * Thread-confined head of the nested context-frame stack.
     */
    private static final ThreadLocal<Frame> CURRENT = ThreadKit.newThreadLocal(false);

    /**
     * Prevents instantiation of the context carrier.
     */
    private ContextCarrier() {
        // No initialization required.
    }

    /**
     * Tests whether the current thread has an installed context frame.
     *
     * @return {@code true} when a context frame is installed
     */
    static boolean isPresent() {
        return CURRENT.get() != null;
    }

    /**
     * Returns the state at the head of the current thread's frame stack.
     *
     * @return current state, or the shared empty state when no frame is installed
     */
    static ContextState current() {
        Frame frame = CURRENT.get();
        return frame == null ? ContextState.empty() : frame.state;
    }

    /**
     * Pushes a state onto the current thread's context stack.
     *
     * @param state state to install; {@code null} installs the shared empty state
     * @return lexical scope used to restore the previous state
     */
    static ContextScope install(ContextState state) {
        Frame frame = new Frame(state == null ? ContextState.empty() : state, CURRENT.get(), Thread.currentThread());
        CURRENT.set(frame);
        return new ContextScope(frame);
    }

    /**
     * Removes the supplied frame after validating ownership and stack order.
     *
     * @param frame frame being closed
     * @throws IllegalStateException when another thread closes the frame or scopes are closed out of order
     */
    static void close(Frame frame) {
        if (frame.owner != Thread.currentThread()) {
            throw new IllegalStateException("Context scope must be closed by its owning thread");
        }
        if (CURRENT.get() != frame) {
            throw new IllegalStateException("Context scopes must be closed in reverse installation order");
        }
        if (frame.previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(frame.previous);
        }
    }

    /**
     * One immutable stack frame.
     */
    static final class Frame {

        /**
         * Immutable state installed by this frame.
         */
        private final ContextState state;

        /**
         * Previous frame restored when this frame closes.
         */
        private final Frame previous;

        /**
         * Thread that installed and therefore owns this frame.
         */
        private final Thread owner;

        /**
         * Creates one immutable frame in the current thread's context stack.
         *
         * @param state    state installed by this frame
         * @param previous previous frame in the stack, or {@code null}
         * @param owner    thread that owns the frame
         */
        private Frame(ContextState state, Frame previous, Thread owner) {
            this.state = state;
            this.previous = previous;
            this.owner = owner;
        }

    }

}
