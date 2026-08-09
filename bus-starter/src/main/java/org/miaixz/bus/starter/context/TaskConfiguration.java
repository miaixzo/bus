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
package org.miaixz.bus.starter.context;

import java.util.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.miaixz.bus.spring.ContextDecorator;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures runtime context propagation for executors created by Spring Boot.
 *
 * <p>
 * This configuration is enabled by default and can be disabled with {@code bus.context.task.enabled=false}.
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ TaskDecorator.class, ThreadPoolTaskExecutorCustomizer.class })
@ConditionalOnProperty(prefix = GeniusBuilder.CONTEXT, name = "task.enabled", havingValue = "true", matchIfMissing = true)
public class TaskConfiguration {

    /**
     * Initializes the configuration activated for runtime context propagation across Spring-managed task executors.
     */
    public TaskConfiguration() {
        // No initialization required.
    }

    /**
     * Creates the bus context task executor customizer.
     *
     * @param taskDecorators ordered task decorators to compose
     * @return the task-executor customizer that installs context propagation
     */
    @Bean("busContextTaskExecutorCustomizer")
    @ConditionalOnMissingBean(name = "busContextTaskExecutorCustomizer")
    ThreadPoolTaskExecutorCustomizer busContextTaskExecutorCustomizer(List<TaskDecorator> taskDecorators) {
        List<TaskDecorator> ordered = new ArrayList<>(taskDecorators);
        AnnotationAwareOrderComparator.sort(ordered);

        Set<TaskDecorator> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<TaskDecorator> unique = new ArrayList<>(ordered.size());
        boolean contextDecoratorAdded = false;
        for (TaskDecorator decorator : ordered) {
            if (!seen.add(decorator)) {
                continue;
            }
            if (decorator instanceof ContextDecorator) {
                if (contextDecoratorAdded) {
                    continue;
                }
                contextDecoratorAdded = true;
            }
            unique.add(decorator);
        }
        return new OrderedTaskExecutorCustomizer(new CompositeTaskDecorator(unique));
    }

    /**
     * Implements the ordered task executor customizer contract.
     */
    private static final class OrderedTaskExecutorCustomizer implements ThreadPoolTaskExecutorCustomizer, Ordered {

        /**
         * Task decorator dependency used by this component.
         */
        private final TaskDecorator taskDecorator;

        /**
         * Creates the ordered task executor customizer.
         *
         * @param taskDecorator context-aware task decorator
         */
        private OrderedTaskExecutorCustomizer(TaskDecorator taskDecorator) {
            this.taskDecorator = taskDecorator;
        }

        /**
         * Installs the composed decorator on one Boot-managed task executor.
         *
         * @param taskExecutor task executor being customized
         */
        @Override
        public void customize(ThreadPoolTaskExecutor taskExecutor) {
            taskExecutor.setTaskDecorator(this.taskDecorator);
        }

        /**
         * Runs after application-provided task-executor customizers.
         *
         * @return customizer order
         */
        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }

    }

}
