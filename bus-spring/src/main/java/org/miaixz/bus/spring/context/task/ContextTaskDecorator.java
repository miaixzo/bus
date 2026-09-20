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
package org.miaixz.bus.spring.context.task;

import org.springframework.core.Ordered;
import org.springframework.core.task.TaskDecorator;

import org.miaixz.bus.spring.context.ContextTransfer;

/**
 * Captures context at task submission and installs it for task execution.
 *
 * @author Kimi Liu
 */
public final class ContextTaskDecorator implements TaskDecorator, Ordered {

    /**
     * Creates the stateless task decorator.
     */
    public ContextTaskDecorator() {
        // No initialization required.
    }

    /**
     * Captures the submitting thread's complete context and wraps the task with a restoring scope.
     *
     * @param runnable task submitted to a Spring-managed executor
     * @return task that installs the captured context only for its execution
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        return ContextTransfer.wrap(runnable);
    }

    /**
     * Gives context capture the highest decorator precedence.
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE}
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
