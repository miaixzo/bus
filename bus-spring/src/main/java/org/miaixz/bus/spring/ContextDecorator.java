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

import org.springframework.core.Ordered;
import org.springframework.core.task.TaskDecorator;

/**
 * Propagates the generic runtime context across task execution boundaries.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ContextDecorator implements TaskDecorator, Ordered {

    /**
     * Context facade used to capture and install state.
     */
    private final ContextBuilder contextBuilder;

    /**
     * Creates a stateless runtime context task decorator.
     *
     * @param contextBuilder context facade used for propagation
     */
    public ContextDecorator(ContextBuilder contextBuilder) {
        this.contextBuilder = contextBuilder;
    }

    /**
     * Captures the submitting thread context and restores the executing thread context after the task finishes.
     *
     * @param runnable task to decorate
     * @return context-aware task
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        ContextState snapshot = this.contextBuilder.capture();
        return () -> {
            try (ContextScope ignored = this.contextBuilder.install(snapshot)) {
                runnable.run();
            }
        };
    }

    /**
     * Runs context installation before unordered task decorators.
     *
     * @return highest precedence
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
