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
 * 收集和启动报告成本的基本组件。
 * <p>
 * 该类负责收集应用程序启动过程中的各项性能指标，包括JVM启动时间、环境准备时间、 上下文刷新时间等，并提供统计和报告功能。它能够从Spring的启动事件中提取信息， 转换为结构化的统计模型，并支持自定义Bean指标的定制化处理。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StartupReporter {

    /**
     * Spring Bean实例化类型集合
     */
    public static final Collection<String> SPRING_BEAN_INSTANTIATE_TYPES = Set
            .of(GeniusBuilder.SPRING_BEANS_INSTANTIATE, GeniusBuilder.SPRING_BEANS_SMART_INSTANTIATE);

    /**
     * Spring上下文后处理器类型集合
     */
    public static final Collection<String> SPRING_CONTEXT_POST_PROCESSOR_TYPES = Set.of(
            GeniusBuilder.SPRING_CONTEXT_BEANDEF_REGISTRY_POST_PROCESSOR,
            GeniusBuilder.SPRING_CONTEXT_BEAN_FACTORY_POST_PROCESSOR);

    /**
     * Spring配置类增强类型集合
     */
    public static final Collection<String> SPRING_CONFIG_CLASSES_ENHANCE_TYPES = Set
            .of(GeniusBuilder.SPRING_CONFIG_CLASSES_ENHANCE, GeniusBuilder.SPRING_BEAN_POST_PROCESSOR);

    /**
     * 启动统计信息
     */
    public final StartupMetrics statics;

    /**
     * Bean指标自定义器列表
     */
    public final List<BeanMetricsCustomizer> beanMetricsCustomizers;

    /**
     * 缓冲区大小，用于BufferingApplicationStartup
     */
    public int bufferSize = 4096;

    /**
     * 成本阈值，用于过滤Bean初始化统计
     */
    public int costThreshold = 50;

    /**
     * 构造函数，初始化StartupReporter
     */
    public StartupReporter() {
        this.statics = new StartupMetrics();
        this.statics.setApplicationBootTime(ManagementFactory.getRuntimeMXBean().getStartTime());
        this.beanMetricsCustomizers = SpringFactoriesLoader
                .loadFactories(BeanMetricsCustomizer.class, StartupReporter.class.getClassLoader());
    }

    /**
     * 将环境绑定到{@link StartupReporter}
     * <p>
     * 通过Spring的Binder机制，将环境配置中以"bus.startup"为前缀的属性绑定到当前实例
     * </p>
     *
     * @param environment 要绑定的环境
     * @throws IllegalStateException 如果绑定过程中发生异常
     */
    public void bindToStartupReporter(ConfigurableEnvironment environment) {
        try {
            Binder.get(environment).bind("bus.startup", Bindable.ofInstance(this));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot bind to StartupReporter", ex);
        }
    }

    /**
     * 设置应用程序名称
     *
     * @param appName 应用程序名称
     */
    public void setAppName(String appName) {
        this.statics.setAppName(appName);
    }

    /**
     * 结束应用程序启动
     * <p>
     * 设置应用程序启动耗时，并对所有阶段统计信息按开始时间进行排序
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
     * 添加要报告的普通启动状态
     *
     * @param stat 增加的启动状态
     */
    public void addCommonStartupStat(BaseMetrics stat) {
        statics.getStageStats().add(stat);
    }

    /**
     * 按名称查找启动统计中报告的阶段
     *
     * @param stageName 阶段名称
     * @return 报告的对象，当找不到对象时返回null
     */
    public BaseMetrics getStageNyName(String stageName) {
        return statics.getStageStats().stream()
                .filter(commonStartupStat -> commonStartupStat.getName().equals(stageName)).findFirst().orElse(null);
    }

    /**
     * 从模型中提取阶段并返回{@link StartupMetrics}
     * <p>
     * 创建一个新的StartupMetrics实例，复制当前实例的数据，并清空当前实例的阶段统计列表
     * </p>
     *
     * @return 包含所有启动统计信息的新实例
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
     * 转换 {@link BufferingApplicationStartup} 到 {@link BeanMetrics} 列表
     * <p>
     * 从应用程序上下文中获取BufferingApplicationStartup，并将其中的启动事件转换为BeanMetrics列表。 转换过程中会构建Bean的层级关系，并根据成本阈值过滤掉部分统计信息。
     * </p>
     *
     * @param context 可配置的应用程序上下文
     * @return Bean指标列表
     */
    public List<BeanMetrics> generateBeanStats(ConfigurableApplicationContext context) {
        List<BeanMetrics> rootBeanList = new ArrayList<>();
        ApplicationStartup applicationStartup = context.getApplicationStartup();
        if (applicationStartup instanceof BufferingApplicationStartup bufferingApplicationStartup) {
            Map<Long, BeanMetrics> beanStatIdMap = new HashMap<>();
            StartupTimeline startupTimeline = bufferingApplicationStartup.drainBufferedTimeline();
            // 获取所有启动事件
            List<StartupTimeline.TimelineEvent> timelineEvents = startupTimeline.getEvents();
            // 将启动事件转换为Bean统计
            timelineEvents.forEach(timelineEvent -> {
                BeanMetrics bean = eventToBeanStat(timelineEvent);
                rootBeanList.add(bean);
                beanStatIdMap.put(timelineEvent.getStartupStep().getId(), bean);
            });
            // 构建状态树
            timelineEvents.forEach(timelineEvent -> {
                BeanMetrics parentBean = beanStatIdMap.get(timelineEvent.getStartupStep().getParentId());
                BeanMetrics bean = beanStatIdMap.get(timelineEvent.getStartupStep().getId());
                if (parentBean != null) {
                    // 父节点实际成本减去子节点
                    parentBean.setRealRefreshElapsedTime(parentBean.getRealRefreshElapsedTime() - bean.getCost());
                    // 从根列表中移除子节点
                    rootBeanList.remove(bean);
                    // 如果子列表开销大于阈值，则将其放到父节点的子列表中
                    if (filterBeanInitializeByCost(bean)) {
                        parentBean.addChild(bean);
                        customBeanStat(context, bean);
                    }
                } else {
                    // 如果根节点小于阈值，则移除根节点
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
     * 根据成本阈值过滤Bean初始化统计
     * <p>
     * 对于特定类型的Bean（如实例化、后处理器、配置类增强），只有当其成本超过阈值时才保留
     * </p>
     *
     * @param bean Bean指标
     * @return 如果应该保留则返回true，否则返回false
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
     * 将启动时间线事件转换为Bean指标
     * <p>
     * 从时间线事件中提取时间信息、类型、名称和标签，构建BeanMetrics对象
     * </p>
     *
     * @param timelineEvent 启动时间线事件
     * @return 转换后的Bean指标
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
     * 从标签中获取指定键的值
     *
     * @param tags 标签集合
     * @param key  要查找的键
     * @return 找到的值，如果找不到则返回null
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
     * 自定义Bean指标
     * <p>
     * 对于Bean实例化类型的指标，获取对应的Bean实例，并应用所有注册的自定义器进行处理
     * </p>
     *
     * @param context  可配置的应用程序上下文
     * @param beanStat Bean指标
     * @return 自定义处理后的Bean指标
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
