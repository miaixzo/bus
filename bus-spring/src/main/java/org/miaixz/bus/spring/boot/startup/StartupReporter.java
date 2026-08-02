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
package org.miaixz.bus.spring.boot.startup;

import java.util.*;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.metrics.buffering.StartupTimeline;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;

import org.miaixz.bus.logger.Executor;

/**
 * A core component for collecting and reporting startup costs.
 * <p>
 * This class is responsible for gathering various performance metrics during the application startup process, including
 * JVM startup time, environment preparation time, context refresh time, etc. It provides functionalities for statistics
 * and reporting. It can extract information from Spring's startup events, convert them into structured statistical
 * models, and support customized processing of bean metrics.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class StartupReporter {

    /**
     * Collection of Spring Bean instantiation types.
     */
    private static final Collection<String> SPRING_BEAN_INSTANTIATE_TYPES = Set
            .of(StartupStages.SPRING_BEANS_INSTANTIATE, StartupStages.SPRING_BEANS_SMART_INSTANTIATE);

    /**
     * Collection of Spring context post-processor types.
     */
    private static final Collection<String> SPRING_CONTEXT_POST_PROCESSOR_TYPES = Set.of(
            StartupStages.SPRING_CONTEXT_BEANDEF_REGISTRY_POST_PROCESSOR,
            StartupStages.SPRING_CONTEXT_BEAN_FACTORY_POST_PROCESSOR);

    /**
     * Collection of Spring configuration classes enhancement types.
     */
    private static final Collection<String> SPRING_CONFIG_CLASSES_ENHANCE_TYPES = Set
            .of(StartupStages.SPRING_CONFIG_CLASSES_ENHANCE, StartupStages.SPRING_BEAN_POST_PROCESSOR);

    /**
     * Startup statistics data.
     */
    private final long applicationBootTime;

    /**
     * Mutable startup-stage metrics collected before the report is finalized.
     */
    private final List<BaseMetrics> stageStats = new ArrayList<>();

    /**
     * Application name shown in the startup report.
     */
    private String appName;

    /**
     * Total application startup duration in milliseconds.
     */
    private long applicationBootElapsedTime;

    /**
     * List of bean metrics customizers.
     */
    private final List<BeanMetricsCustomizer> beanMetricsCustomizers;

    /**
     * Cost threshold in milliseconds for filtering bean initialization statistics. Beans with initialization cost below
     * this threshold might be filtered out. Default is 50ms.
     */
    private int costThreshold = 50;

    /**
     * Initializes startup metric collection and loads {@link BeanMetricsCustomizer} implementations from Spring
     * factories.
     */
    public StartupReporter() {
        this(System.currentTimeMillis());
    }

    /**
     * Creates an isolated report for one application startup.
     *
     * @param applicationBootTime timestamp at which the current application startup began
     */
    public StartupReporter(long applicationBootTime) {
        this.applicationBootTime = applicationBootTime;
        List<BeanMetricsCustomizer> customizers = new ArrayList<>(SpringFactoriesLoader
                .loadFactories(BeanMetricsCustomizer.class, StartupReporter.class.getClassLoader()));
        AnnotationAwareOrderComparator.sort(customizers);
        this.beanMetricsCustomizers = List.copyOf(customizers);
    }

    /**
     * Returns the bean-cost threshold used by property binding.
     *
     * @return the cost threshold
     */
    public int getCostThreshold() {
        return costThreshold;
    }

    /**
     * Updates the bean-cost threshold used by property binding.
     *
     * @param costThreshold cost threshold
     */
    public void setCostThreshold(int costThreshold) {
        if (costThreshold < 0) {
            throw new IllegalArgumentException("costThreshold must not be negative");
        }
        this.costThreshold = costThreshold;
    }

    /**
     * Binds environment properties prefixed with "bus.startup" to this {@code StartupReporter} instance.
     *
     * @param environment The environment to bind from.
     * @throws IllegalStateException if an error occurs during binding.
     */
    public void bindToStartupReporter(ConfigurableEnvironment environment) {
        try {
            Binder.get(environment).bind("bus.startup", Bindable.ofInstance(this));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot bind to StartupReporter", ex);
        }
    }

    /**
     * Sets the application name in the startup statistics.
     *
     * @param appName The name of the application.
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Marks the application boot as finished.
     * <p>
     * Calculates the total application boot elapsed time and sorts all collected stage statistics by their start time.
     * </p>
     */
    public void applicationBootFinish() {
        applicationBootElapsedTime = Math.max(0, System.currentTimeMillis() - applicationBootTime);
        stageStats.sort((o1, o2) -> {
            if (o1.getStartTime() == o2.getStartTime()) {
                return 0;
            }
            return o1.getStartTime() > o2.getStartTime() ? 1 : -1;
        });
    }

    /**
     * Adds a common startup statistic to the collection.
     *
     * @param stat The {@link BaseMetrics} object representing a startup stage.
     */
    public void addCommonStartupStat(BaseMetrics stat) {
        if (stat != null) {
            stageStats.add(stat);
        }
    }

    /**
     * Finds a reported startup stage by its name.
     *
     * @param stageName The name of the stage to find.
     * @return The {@link BaseMetrics} object if found, otherwise {@code null}.
     */
    public BaseMetrics getStageByName(String stageName) {
        return stageStats.stream().filter(commonStartupStat -> commonStartupStat.getName().equals(stageName))
                .findFirst().orElse(null);
    }

    /**
     * Drains and returns all collected startup statistics.
     * <p>
     * Creates a new {@link StartupMetrics} instance, copies the current instance's data, and then clears the current
     * instance's stage statistics list.
     * </p>
     *
     * @return A new {@link StartupMetrics} instance containing all collected startup statistics.
     */
    public StartupMetrics drainStartupStatics() {
        List<BaseMetrics> stats = new ArrayList<>();
        Iterator<BaseMetrics> iterator = stageStats.iterator();
        while (iterator.hasNext()) {
            BaseMetrics metric = iterator.next();
            process(metric);
            stats.add(metric);
            iterator.remove();
        }
        return new StartupMetrics(appName, applicationBootElapsedTime, applicationBootTime, stats);
    }

    /**
     * Generates a list of {@link BeanMetrics} from the {@link BufferingApplicationStartup} data.
     * <p>
     * This method extracts startup events from the application context's {@link BufferingApplicationStartup}, converts
     * them into {@link BeanMetrics}, builds their hierarchical relationships, and filters statistics based on the
     * {@link #costThreshold}.
     * </p>
     *
     * @param context The configurable application context.
     * @return A list of {@link BeanMetrics} representing the bean initialization statistics.
     */
    public List<BeanMetrics> generateBeanStats(ConfigurableApplicationContext context) {
        ApplicationStartup applicationStartup = context.getApplicationStartup();
        if (!(applicationStartup instanceof BufferingApplicationStartup bufferingApplicationStartup)) {
            return List.of();
        }
        List<StartupTimeline.TimelineEvent> events = bufferingApplicationStartup.drainBufferedTimeline().getEvents();
        Set<Long> eventIds = new HashSet<>();
        Map<Long, List<StartupTimeline.TimelineEvent>> childrenByParent = new HashMap<>();
        for (StartupTimeline.TimelineEvent event : events) {
            eventIds.add(event.getStartupStep().getId());
            childrenByParent.computeIfAbsent(event.getStartupStep().getParentId(), key -> new ArrayList<>()).add(event);
        }
        List<BeanMetrics> roots = new ArrayList<>();
        for (StartupTimeline.TimelineEvent event : events) {
            Long parentId = event.getStartupStep().getParentId();
            if (parentId == null || !eventIds.contains(parentId)) {
                BeanMetrics root = buildBeanMetric(context, event, childrenByParent);
                if (root != null) {
                    roots.add(root);
                }
            }
        }
        return List.copyOf(roots);
    }

    /**
     * Filters bean initialization statistics based on a cost threshold.
     * <p>
     * For specific bean types (instantiation, post-processors, config class enhancements), a bean is retained only if
     * its cost exceeds the configured {@link #costThreshold}.
     * </p>
     *
     * @param bean The {@link BeanMetrics} to filter.
     * @return {@code true} if the bean should be retained, {@code false} otherwise.
     */
    private boolean filterBeanInitializeByCost(BeanMetrics bean) {
        String name = bean.getType();
        if (SPRING_BEAN_INSTANTIATE_TYPES.contains(name) || SPRING_CONTEXT_POST_PROCESSOR_TYPES.contains(name)
                || SPRING_CONFIG_CLASSES_ENHANCE_TYPES.contains(name)) {
            return bean.getCost() >= costThreshold;
        } else {
            return true;
        }
    }

    /**
     * Converts a {@link StartupTimeline.TimelineEvent} to a {@link BeanMetrics} object.
     * <p>
     * Extracts time information, type, name, and tags from the timeline event to construct a {@link BeanMetrics}
     * object. Special handling is applied for bean instantiation and context post-processor types to extract the
     * correct bean name.
     * </p>
     *
     * @param timelineEvent The startup timeline event.
     * @return The converted {@link BeanMetrics} object.
     * @param context          owning application context
     * @param childrenByParent children by parent
     */
    private BeanMetrics buildBeanMetric(
            ConfigurableApplicationContext context,
            StartupTimeline.TimelineEvent timelineEvent,
            Map<Long, List<StartupTimeline.TimelineEvent>> childrenByParent) {
        String type = timelineEvent.getStartupStep().getName();
        String name;
        if (SPRING_BEAN_INSTANTIATE_TYPES.contains(type)) {
            StartupStep.Tags tags = timelineEvent.getStartupStep().getTags();
            name = getValueFromTags(tags, "beanName");
        } else if (SPRING_CONTEXT_POST_PROCESSOR_TYPES.contains(type)) {
            StartupStep.Tags tags = timelineEvent.getStartupStep().getTags();
            name = getValueFromTags(tags, "postProcessor");
        } else {
            name = type;
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        timelineEvent.getStartupStep().getTags()
                .forEach(tag -> attributes.put(tag.getKey(), process(tag.getKey(), tag.getValue())));

        List<BeanMetrics> children = new ArrayList<>();
        for (StartupTimeline.TimelineEvent childEvent : childrenByParent
                .getOrDefault(timelineEvent.getStartupStep().getId(), List.of())) {
            BeanMetrics child = buildBeanMetric(context, childEvent, childrenByParent);
            if (child != null) {
                children.add(child);
            }
        }
        long realElapsedTime = Math.max(
                0,
                timelineEvent.getDuration().toMillis() - children.stream().mapToLong(BaseMetrics::getCost).sum());
        Object bean = null;
        if (SPRING_BEAN_INSTANTIATE_TYPES.contains(type) && context.isActive() && name != null
                && context.containsBean(name)) {
            bean = context.getBean(name);
            attributes.put("classType", AopProxyUtils.ultimateTargetClass(bean).getName());
        }
        BeanMetrics metric = new BeanMetrics(name, timelineEvent.getStartTime().toEpochMilli(),
                timelineEvent.getEndTime().toEpochMilli(), attributes, children, type, realElapsedTime);
        if (!filterBeanInitializeByCost(metric)) {
            return null;
        }
        if (bean == null) {
            return metric;
        }
        BeanMetrics customized = metric;
        for (BeanMetricsCustomizer customizer : beanMetricsCustomizers) {
            BeanMetrics current = customizer.customize(name, bean, customized);
            if (current == null) {
                break;
            }
            customized = current;
        }
        return customized;
    }

    /**
     * Processes named diagnostic attributes through the active logger extensions before a report is exposed.
     *
     * @param metric startup metric tree
     */
    private void process(BaseMetrics metric) {
        metric.mutableAttributes().replaceAll(this::process);
        if (metric instanceof ChildrenMetrics<?> childrenMetrics) {
            childrenMetrics.getChildren().forEach(this::process);
        }
    }

    /**
     * Processes one startup diagnostic value without depending on a concrete extension implementation.
     *
     * @param key   diagnostic field name
     * @param value diagnostic field value
     * @return processed string value
     */
    private String process(String key, String value) {
        Object processed = Executor.processValue(key, value);
        return processed == null ? null : processed.toString();
    }

    /**
     * Extracts the value associated with a given key from a collection of {@link StartupStep.Tags}.
     *
     * @param tags The collection of tags.
     * @param key  The key to search for.
     * @return The value of the tag if found, otherwise {@code null}.
     */
    private String getValueFromTags(StartupStep.Tags tags, String key) {
        for (StartupStep.Tag tag : tags) {
            if (Objects.equals(key, tag.getKey())) {
                return tag.getValue();
            }
        }
        return null;
    }

}
