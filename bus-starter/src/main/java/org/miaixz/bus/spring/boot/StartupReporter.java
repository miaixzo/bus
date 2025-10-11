/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.boot;

import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.metrics.BaseMetrics;
import org.miaixz.bus.spring.metrics.BeanMetrics;
import org.miaixz.bus.spring.metrics.BeanMetricsCustomizer;
import org.miaixz.bus.spring.metrics.StartupMetrics;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.metrics.buffering.StartupTimeline;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;

import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * A core component for collecting and reporting startup costs.
 * <p>
 * This class is responsible for gathering various performance metrics during the application startup process, including
 * JVM startup time, environment preparation time, context refresh time, etc. It provides functionalities for statistics
 * and reporting. It can extract information from Spring's startup events, convert them into structured statistical
 * models, and support customized processing of bean metrics.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StartupReporter {

    /**
     * Collection of Spring Bean instantiation types.
     */
    public static final Collection<String> SPRING_BEAN_INSTANTIATE_TYPES = Set
            .of(GeniusBuilder.SPRING_BEANS_INSTANTIATE, GeniusBuilder.SPRING_BEANS_SMART_INSTANTIATE);

    /**
     * Collection of Spring context post-processor types.
     */
    public static final Collection<String> SPRING_CONTEXT_POST_PROCESSOR_TYPES = Set.of(
            GeniusBuilder.SPRING_CONTEXT_BEANDEF_REGISTRY_POST_PROCESSOR,
            GeniusBuilder.SPRING_CONTEXT_BEAN_FACTORY_POST_PROCESSOR);

    /**
     * Collection of Spring configuration classes enhancement types.
     */
    public static final Collection<String> SPRING_CONFIG_CLASSES_ENHANCE_TYPES = Set
            .of(GeniusBuilder.SPRING_CONFIG_CLASSES_ENHANCE, GeniusBuilder.SPRING_BEAN_POST_PROCESSOR);

    /**
     * Startup statistics data.
     */
    public final StartupMetrics statics;

    /**
     * List of bean metrics customizers.
     */
    public final List<BeanMetricsCustomizer> beanMetricsCustomizers;

    /**
     * Buffer size for {@link BufferingApplicationStartup}. Default is 4096.
     */
    public int bufferSize = 4096;

    /**
     * Cost threshold in milliseconds for filtering bean initialization statistics. Beans with initialization cost below
     * this threshold might be filtered out. Default is 50ms.
     */
    public int costThreshold = 50;

    /**
     * Constructs a new {@code StartupReporter} instance. Initializes startup metrics and loads
     * {@link BeanMetricsCustomizer} implementations from Spring factories.
     */
    public StartupReporter() {
        this.statics = new StartupMetrics();
        this.statics.setApplicationBootTime(ManagementFactory.getRuntimeMXBean().getStartTime());
        this.beanMetricsCustomizers = SpringFactoriesLoader
                .loadFactories(BeanMetricsCustomizer.class, StartupReporter.class.getClassLoader());
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
        this.statics.setAppName(appName);
    }

    /**
     * Marks the application boot as finished.
     * <p>
     * Calculates the total application boot elapsed time and sorts all collected stage statistics by their start time.
     * </p>
     */
    public void applicationBootFinish() {
        statics.setApplicationBootElapsedTime(ManagementFactory.getRuntimeMXBean().getUptime());
        statics.getStageStats().sort((o1, o2) -> {
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
        statics.getStageStats().add(stat);
    }

    /**
     * Finds a reported startup stage by its name.
     *
     * @param stageName The name of the stage to find.
     * @return The {@link BaseMetrics} object if found, otherwise {@code null}.
     */
    public BaseMetrics getStageNyName(String stageName) {
        return statics.getStageStats().stream()
                .filter(commonStartupStat -> commonStartupStat.getName().equals(stageName)).findFirst().orElse(null);
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
        StartupMetrics startupReporterStatics = new StartupMetrics();
        startupReporterStatics.setAppName(this.statics.getAppName());
        startupReporterStatics.setApplicationBootElapsedTime(this.statics.getApplicationBootElapsedTime());
        startupReporterStatics.setApplicationBootTime(this.statics.getApplicationBootTime());
        List<BaseMetrics> stats = new ArrayList<>();
        Iterator<BaseMetrics> iterator = this.statics.getStageStats().iterator();
        while (iterator.hasNext()) {
            stats.add(iterator.next());
            iterator.remove();
        }
        startupReporterStatics.setStageStats(stats);
        return startupReporterStatics;
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
        List<BeanMetrics> rootBeanList = new ArrayList<>();
        ApplicationStartup applicationStartup = context.getApplicationStartup();
        if (applicationStartup instanceof BufferingApplicationStartup bufferingApplicationStartup) {
            Map<Long, BeanMetrics> beanStatIdMap = new HashMap<>();
            StartupTimeline startupTimeline = bufferingApplicationStartup.drainBufferedTimeline();
            // Get all startup events
            List<StartupTimeline.TimelineEvent> timelineEvents = startupTimeline.getEvents();
            // Convert startup events to Bean statistics
            timelineEvents.forEach(timelineEvent -> {
                BeanMetrics bean = eventToBeanStat(timelineEvent);
                rootBeanList.add(bean);
                beanStatIdMap.put(timelineEvent.getStartupStep().getId(), bean);
            });
            // Build the state tree
            timelineEvents.forEach(timelineEvent -> {
                BeanMetrics parentBean = beanStatIdMap.get(timelineEvent.getStartupStep().getParentId());
                BeanMetrics bean = beanStatIdMap.get(timelineEvent.getStartupStep().getId());
                if (parentBean != null) {
                    // Parent node's actual cost subtracts child node's cost
                    parentBean.setRealRefreshElapsedTime(parentBean.getRealRefreshElapsedTime() - bean.getCost());
                    // Remove child node from the root list
                    rootBeanList.remove(bean);
                    // If the child's cost is greater than the threshold, add it to the parent's child list
                    if (filterBeanInitializeByCost(bean)) {
                        parentBean.addChild(bean);
                        customBeanStat(context, bean);
                    }
                } else {
                    // If the root node's cost is less than the threshold, remove it from the root list
                    if (!filterBeanInitializeByCost(bean)) {
                        rootBeanList.remove(bean);
                    } else {
                        customBeanStat(context, bean);
                    }
                }
            });
        }
        return rootBeanList;
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
     */
    private BeanMetrics eventToBeanStat(StartupTimeline.TimelineEvent timelineEvent) {
        BeanMetrics bean = new BeanMetrics();
        bean.setStartTime(timelineEvent.getStartTime().toEpochMilli());
        bean.setEndTime(timelineEvent.getEndTime().toEpochMilli());
        bean.setCost(timelineEvent.getDuration().toMillis());
        bean.setRealRefreshElapsedTime(bean.getCost());
        String name = timelineEvent.getStartupStep().getName();
        bean.setType(name);
        if (SPRING_BEAN_INSTANTIATE_TYPES.contains(name)) {
            StartupStep.Tags tags = timelineEvent.getStartupStep().getTags();
            String beanName = getValueFromTags(tags, "beanName");
            bean.setName(beanName);
        } else if (SPRING_CONTEXT_POST_PROCESSOR_TYPES.contains(name)) {
            StartupStep.Tags tags = timelineEvent.getStartupStep().getTags();
            String beanName = getValueFromTags(tags, "postProcessor");
            bean.setName(beanName);
        } else {
            bean.setName(name);
        }
        timelineEvent.getStartupStep().getTags().forEach(tag -> bean.putAttribute(tag.getKey(), tag.getValue()));
        return bean;
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

    /**
     * Customizes a {@link BeanMetrics} object, particularly for bean instantiation types.
     * <p>
     * For bean instantiation metrics, it retrieves the actual bean instance from the context and applies all registered
     * {@link BeanMetricsCustomizer}s to further enrich the metrics.
     * </p>
     *
     * @param context  The configurable application context.
     * @param beanStat The {@link BeanMetrics} to customize.
     * @return The customized {@link BeanMetrics} object.
     */
    private BeanMetrics customBeanStat(ConfigurableApplicationContext context, BeanMetrics beanStat) {
        if (!context.isActive()) {
            return beanStat;
        }
        String type = beanStat.getType();
        if (SPRING_BEAN_INSTANTIATE_TYPES.contains(type)) {
            String beanName = beanStat.getName();
            Object bean = context.getBean(beanName);
            beanStat.putAttribute("classType", AopProxyUtils.ultimateTargetClass(bean).getName());
            BeanMetrics result = beanStat;
            for (BeanMetricsCustomizer customizer : beanMetricsCustomizers) {
                BeanMetrics current = customizer.customize(beanName, bean, result);
                if (current == null) {
                    return result;
                }
                result = current;
            }
            return result;
        } else {
            return beanStat;
        }
    }

}
