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
package org.miaixz.bus.spring.jdbc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalApplicationListenerAdapter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import org.miaixz.bus.core.lang.annotation.AfterCommit;

/**
 * Discovers Core commit listeners and connects them to Spring's transactional event synchronization.
 * <p>
 * Listener methods retain payload matching, proxy invocation, ordering, and result publication. Events outside an
 * active transaction are ignored. AOT processing registers invocation hints while retaining this processor at runtime.
 *
 * @author Kimi Liu
 */
public class AfterCommitListenerProcessor
        implements SmartInitializingSingleton, ApplicationContextAware, BeanRegistrationAotProcessor {

    /**
     * Owning context used to resolve listener beans without eagerly instantiating lazy targets.
     */
    private ConfigurableApplicationContext applicationContext;

    /**
     * Creates the commit listener infrastructure without initializing application beans.
     */
    public AfterCommitListenerProcessor() {
        // No initialization required.
    }

    /**
     * Finds direct, inherited, and composed Core listener declarations on a target type.
     *
     * @param type bean target type
     * @return listener methods and their merged declarations
     */
    private static Map<Method, AfterCommit> methods(Class<?> type) {
        if (!AnnotationUtils.isCandidateClass(type, AfterCommit.class)) {
            return Map.of();
        }
        return MethodIntrospector.selectMethods(
                type,
                (MethodIntrospector.MetadataLookup<AfterCommit>) method -> AnnotatedElementUtils
                        .findMergedAnnotation(method, AfterCommit.class));
    }

    /**
     * Stores the context used for listener registration and deferred bean lookup.
     *
     * @param applicationContext owning Spring application context
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        Assert.isInstanceOf(ConfigurableApplicationContext.class, applicationContext);
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * Registers each listener once after proxy creation, including lazy and scoped listener targets.
     */
    @Override
    public void afterSingletonsInstantiated() {
        var factory = applicationContext.getBeanFactory();
        for (String name : factory.getBeanNamesForType(Object.class, true, false)) {
            if (ScopedProxyUtils.isScopedTarget(name)) {
                continue;
            }
            Class<?> type = AutoProxyUtils.determineTargetClass(factory, name);
            if (type != null && ScopedObject.class.isAssignableFrom(type)) {
                type = AutoProxyUtils.determineTargetClass(factory, ScopedProxyUtils.getTargetBeanName(name));
            }
            if (type == null) {
                continue;
            }
            for (Method method : methods(type).keySet()) {
                if (AnnotatedElementUtils.hasAnnotation(method, EventListener.class)) {
                    throw new BeanInitializationException(
                            "@AfterCommit must not be combined with Spring @EventListener: " + method);
                }
                Method invocable = AopUtils.selectInvocableMethod(method, applicationContext.getType(name));
                applicationContext.addApplicationListener(new Listener(name, type, invocable, applicationContext));
            }
        }
    }

    /**
     * Registers listener invocation metadata for the native image without instantiating listener beans.
     *
     * @param registeredBean bean definition being processed
     * @return invocation hint contribution, or null when the bean has no commit listeners
     */
    @Override
    public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
        var listeners = methods(registeredBean.getBeanClass());
        if (listeners.isEmpty()) {
            return null;
        }
        return (context, code) -> listeners.keySet().forEach(
                method -> context.getRuntimeHints().reflection().registerMethod(method, ExecutableMode.INVOKE));
    }

    /**
     * Keeps runtime discovery available when the application uses generated AOT bean registrations.
     *
     * @return false because this processor also registers runtime event listeners
     */
    @Override
    public boolean isBeanExcludedFromAotProcessing() {
        return false;
    }

    /**
     * Adapts one Core listener to Spring event matching and transaction completion callbacks.
     *
     * @author Kimi Liu
     */
    private static final class Listener extends ApplicationListenerMethodAdapter {

        /**
         * Deferred listener bean name, preserving lazy initialization and proxy advice.
         */
        private final String beanName;

        /**
         * Context used for target lookup and publication of returned events.
         */
        private final ApplicationContext context;

        /**
         * Spring synchronization adapter that schedules processing only after a successful commit.
         */
        private final TransactionalApplicationListenerAdapter<ApplicationEvent> transactional;

        /**
         * Creates an event adapter without resolving its listener bean.
         *
         * @param beanName   listener bean name
         * @param targetType ultimate bean target type
         * @param method     invocable listener method
         * @param context    owning application context
         */
        private Listener(String beanName, Class<?> targetType, Method method, ApplicationContext context) {
            super(beanName, targetType, method);
            this.beanName = beanName;
            this.context = context;
            this.transactional = new TransactionalApplicationListenerAdapter<>(this::processEvent);
            this.transactional.setTransactionPhase(TransactionPhase.AFTER_COMMIT);
            this.transactional.setOrder(getOrder());
            this.transactional.setListenerId(getListenerId());
        }

        /**
         * Keeps synchronization registration on the publishing transaction's thread.
         *
         * @return false so an asynchronous multicaster cannot lose the transaction context
         */
        @Override
        public boolean supportsAsyncExecution() {
            return false;
        }

        /**
         * Delegates registration and transaction outcome handling to Spring.
         *
         * @param event matching event published by the application
         */
        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            transactional.onApplicationEvent(event);
        }

        /**
         * Resolves the current proxy or scoped target only when the listener is invoked.
         *
         * @return listener bean from the owning context
         */
        @Override
        protected Object getTargetBean() {
            return context.getBean(beanName);
        }

        /**
         * Publishes returned events using collection and asynchronous completion conventions.
         *
         * @param result non-null listener return value
         */
        @Override
        protected void handleResult(Object result) {
            if (result instanceof CompletionStage<?> stage) {
                stage.whenComplete((value, failure) -> {
                    if (failure != null) {
                        handleAsyncError(failure);
                    } else {
                        publish(value);
                    }
                });
            } else {
                publish(result);
            }
        }

        /**
         * Publishes one return value or each returned array or collection element.
         *
         * @param result returned event, collection, array, or null
         */
        private void publish(Object result) {
            if (result == null) {
                return;
            }
            if (result.getClass().isArray()) {
                for (Object event : ObjectUtils.toObjectArray(result)) {
                    context.publishEvent(event);
                }
            } else if (result instanceof Collection<?> events) {
                events.forEach(context::publishEvent);
            } else {
                context.publishEvent(result);
            }
        }

    }

}
