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

import jakarta.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableTempus;
import org.miaixz.bus.tempus.temporal.Publisher;
import org.miaixz.bus.tempus.temporal.Subscriber;
import org.miaixz.bus.tempus.temporal.worker.CachingWorkflowConnector;
import org.miaixz.bus.tempus.temporal.worker.DefaultWorkflowTransport;
import org.miaixz.bus.tempus.temporal.worker.WorkflowConnector;
import org.miaixz.bus.tempus.temporal.worker.WorkflowTransport;
import org.miaixz.bus.tempus.temporal.workflow.DefaultRetryOptionsFactory;
import org.miaixz.bus.tempus.temporal.workflow.DefaultWorkflowOptionsFactory;
import org.miaixz.bus.tempus.temporal.workflow.RetryOptionsFactory;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowIdGenerator;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowOptionsFactory;
import org.miaixz.bus.tempus.temporal.workflow.publisher.WorkflowPublisherBinding;
import org.miaixz.bus.tempus.temporal.workflow.publisher.WorkflowPublisherManager;
import org.miaixz.bus.tempus.temporal.workflow.subscriber.WorkflowSubscriberBinding;
import org.miaixz.bus.tempus.temporal.workflow.subscriber.WorkflowSubscriberManager;

/**
 * Temporal framework auto-configuration.
 * <p>
 * Imported through {@link EnableTempus}; registers core Temporal beans including clients, workers, publishers, and
 * subscribers.
 * <p>
 * All beans are protected by {@code @ConditionalOnMissingBean}, allowing applications to override default
 * implementations. The subscriber worker starts only when {@code bus.tempus.enabled=true}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(TempusProperties.class)
@ConditionalOnProperty(prefix = GeniusBuilder.TEMPUS, name = "enabled", havingValue = "true", matchIfMissing = true)
public class TempusConfiguration {

    /**
     * Creates Temporal auto-configuration.
     */
    public TempusConfiguration() {
        // No initialization required.
    }

    /**
     * Temporal configuration properties used to build default framework beans.
     */
    @Resource
    private TempusProperties properties;

    /**
     * Creates a {@link WorkflowTransport}.
     * <p>
     * Skipped when the application registers a custom {@code WorkflowTransport} bean.
     *
     * @return default Temporal workflow transport
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowTransport workflowTransport() {
        return new DefaultWorkflowTransport();
    }

    /**
     * Creates a {@link WorkflowConnector}.
     * <p>
     * Skipped when the application registers a custom {@code WorkflowConnector} bean.
     *
     * @param transport workflow transport used to create underlying service stubs
     * @return caching workflow connector
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(WorkflowTransport.class)
    public WorkflowConnector workflowConnector(WorkflowTransport transport) {
        return new CachingWorkflowConnector(transport);
    }

    /**
     * Creates the default {@link WorkflowIdGenerator}.
     * <p>
     * Skipped when the application registers a custom {@code WorkflowIdGenerator} bean.
     *
     * @return workflow ID generator based on the default UUID strategy
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowIdGenerator workflowIdGenerator() {
        return new WorkflowIdGenerator() {
        };
    }

    /**
     * Creates a {@link RetryOptionsFactory}.
     * <p>
     * Skipped when the application registers a custom {@code RetryOptionsFactory} bean.
     *
     * @return default retry options factory
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryOptionsFactory retryOptionsFactory() {
        return new DefaultRetryOptionsFactory();
    }

    /**
     * Creates a {@link WorkflowOptionsFactory}.
     * <p>
     * Skipped when the application registers a custom {@code WorkflowOptionsFactory} bean.
     *
     * @param generator           generator used to create unique workflow IDs
     * @param retryOptionsFactory retry options factory
     * @return default workflow options factory
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowOptionsFactory workflowOptionsFactory(
            WorkflowIdGenerator generator,
            RetryOptionsFactory retryOptionsFactory) {
        return new DefaultWorkflowOptionsFactory(generator, retryOptionsFactory);
    }

    /**
     * Creates a {@link Publisher} that starts workflow executions on the Temporal service.
     * <p>
     * Skipped when the application registers a custom {@code Publisher} bean.
     *
     * @param connector workflow connector
     * @param factory   workflow options factory
     * @param binding   publisher binding configuration
     * @return workflow publisher manager
     */
    @Bean
    @ConditionalOnBean({ WorkflowConnector.class, WorkflowOptionsFactory.class, WorkflowPublisherBinding.class })
    @ConditionalOnMissingBean
    public Publisher publisherManager(
            WorkflowConnector connector,
            WorkflowOptionsFactory factory,
            WorkflowPublisherBinding binding) {
        return new WorkflowPublisherManager(connector, factory, binding, properties);
    }

    /**
     * Creates a {@link Subscriber} worker that polls Temporal task queues and dispatches workflow and activity tasks.
     * <p>
     * Skipped when the application registers a custom {@code Subscriber} bean.
     * <p>
     * The worker is started by {@link TempusLifecycle} after the Spring context is ready.
     *
     * @param binding   subscriber binding used to register workflow and activity implementations
     * @param transport workflow transport used to create underlying service stubs
     * @param factory   Temporal options factory
     * @return workflow subscriber manager
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnBean({ WorkflowSubscriberBinding.class, WorkflowTransport.class, WorkflowOptionsFactory.class })
    @ConditionalOnProperty(prefix = GeniusBuilder.TEMPUS, name = "enabled", havingValue = "true")
    public Subscriber subscriberManager(
            WorkflowSubscriberBinding binding,
            WorkflowTransport transport,
            WorkflowOptionsFactory factory) {
        return new WorkflowSubscriberManager(binding, transport, factory, properties);
    }

    /**
     * Creates a lifecycle component that starts and stops the Temporal subscriber.
     *
     * @param subscriber Temporal subscriber
     * @return Temporal lifecycle component
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(Subscriber.class)
    @ConditionalOnProperty(prefix = GeniusBuilder.TEMPUS, name = "enabled", havingValue = "true")
    public TempusLifecycle tempusLifecycle(Subscriber subscriber) {
        return new TempusLifecycle(subscriber);
    }

}
