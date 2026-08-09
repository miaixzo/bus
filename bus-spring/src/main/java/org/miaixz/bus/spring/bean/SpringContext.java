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
package org.miaixz.bus.spring.bean;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Instance-owned access to one Spring application context and its event publisher.
 *
 * @author Kimi Liu
 */
public class SpringContext implements ApplicationListener<ContextClosedEvent>, AutoCloseable {

    /**
     * Context reference released when the owning context closes.
     */
    private final AtomicReference<ApplicationContext> context;

    /**
     * Creates a lifecycle wrapper for one application context.
     *
     * @param context owning application context
     */
    public SpringContext(ApplicationContext context) {
        this.context = new AtomicReference<>(Objects.requireNonNull(context, "context"));
    }

    /**
     * Returns the owned active context.
     *
     * @return the owned application context
     */
    public ApplicationContext get() {
        ApplicationContext current = this.context.get();
        if (current == null) {
            throw new IllegalStateException("Spring application context is closed");
        }
        if (current instanceof ConfigurableApplicationContext configurable && !configurable.isActive()) {
            throw new IllegalStateException("Spring application context is not active");
        }
        return current;
    }

    /**
     * Publishes an application event through the owned context.
     *
     * @param event published event
     */
    public void publishEvent(Object event) {
        Objects.requireNonNull(event, "event");
        if (event instanceof ApplicationEvent applicationEvent) {
            get().publishEvent(applicationEvent);
        } else {
            get().publishEvent(event);
        }
    }

    /**
     * Releases the reference only when the closing event belongs to the owned context.
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.context.compareAndSet(event.getApplicationContext(), null);
    }

    /**
     * Releases the owned reference without refreshing or closing the application context itself.
     */
    @Override
    public void close() {
        this.context.set(null);
    }

}
