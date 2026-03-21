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
package org.miaixz.bus.starter.tempus;

import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableTempus;
import org.miaixz.bus.tempus.temporal.Publisher;
import org.miaixz.bus.tempus.temporal.Subscriber;
import org.miaixz.bus.tempus.temporal.worker.CachingWorkflowClientProvider;
import org.miaixz.bus.tempus.temporal.worker.WorkflowClientProvider;
import org.miaixz.bus.tempus.temporal.worker.WorkflowServiceStubsProvider;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowIdGenerator;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowOptionsFactory;
import org.miaixz.bus.tempus.temporal.workflow.publisher.WorkflowPublisherBinding;
import org.miaixz.bus.tempus.temporal.workflow.publisher.WorkflowPublisherManager;
import org.miaixz.bus.tempus.temporal.workflow.publisher.WorkflowPublisherOptionsFactory;
import org.miaixz.bus.tempus.temporal.workflow.subscriber.WorkflowSubscriberBinding;
import org.miaixz.bus.tempus.temporal.workflow.subscriber.WorkflowSubscriberManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * Temporal framework-level auto-configuration.
 * <p>
 * Imported by {@link EnableTempus} via {@code @Import}. Registers the core Temporal beans: client, worker, publisher
 * and subscriber.
 * <p>
 * All beans are guarded by {@code @ConditionalOnMissingBean} so the application side can override any of them. The
 * subscriber worker only starts when {@code bus.tempus.enabled=true}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(TempusProperties.class)
public class TempusConfiguration {

    @Resource
    private TempusProperties properties;

    /**
     * Creates the {@link WorkflowClientProvider}.
     * <p>
     * Skipped when the application registers its own {@code WorkflowClientProvider} bean.
     *
     * @param provider the {@link WorkflowServiceStubsProvider} used to build the underlying gRPC stubs
     * @return a caching workflow-client provider
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowClientProvider workflowClientProvider(WorkflowServiceStubsProvider provider) {
        return new CachingWorkflowClientProvider(provider);
    }

    /**
     * Creates the default {@link WorkflowIdGenerator} that produces a random UUID for each workflow execution.
     * <p>
     * Skipped when the application registers its own {@code WorkflowIdGenerator} bean.
     *
     * @return a no-op workflow-id generator (UUID-based default)
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowIdGenerator workflowIdGenerator() {
        return new WorkflowIdGenerator() {
        };
    }

    /**
     * Creates the {@link WorkflowOptionsFactory}.
     * <p>
     * Skipped when the application registers its own {@code WorkflowOptionsFactory} bean.
     *
     * @param generator the {@link WorkflowIdGenerator} used to produce unique workflow IDs
     * @return a publisher-oriented workflow-options factory
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowOptionsFactory workflowOptionsFactory(WorkflowIdGenerator generator) {
        return new WorkflowPublisherOptionsFactory(generator, properties.getWorkflowExecutionTimeoutDays(),
                properties.getWorkflowRunTimeoutHours(), properties.getWorkflowTaskTimeoutMinutes());
    }

    /**
     * Creates the {@link Publisher} that starts workflow executions on the Temporal server.
     * <p>
     * Skipped when the application registers its own {@code Publisher} bean.
     *
     * @param provider the workflow-client provider
     * @param factory  the workflow-options factory
     * @param binding  the publisher binding that supplies task-queue and workflow-type metadata
     * @return a workflow publisher manager
     */
    @Bean
    @ConditionalOnMissingBean
    public Publisher publisherManager(
            WorkflowClientProvider provider,
            WorkflowOptionsFactory factory,
            WorkflowPublisherBinding binding) {
        return new WorkflowPublisherManager(provider, factory, binding);
    }

    /**
     * Creates the {@link Subscriber} worker that polls the Temporal task queue and dispatches workflow and activity
     * tasks.
     * <p>
     * Skipped when the application registers its own {@code Subscriber} bean.
     *
     * @param binding  the subscriber binding that registers workflow and activity implementations
     * @param provider the {@link WorkflowServiceStubsProvider} used to build the underlying gRPC stubs
     * @return a workflow subscriber manager
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = GeniusBuilder.TEMPUS, name = "enabled", havingValue = "true")
    public Subscriber subscriberManager(WorkflowSubscriberBinding binding, WorkflowServiceStubsProvider provider) {
        return new WorkflowSubscriberManager(binding, provider);
    }

}
