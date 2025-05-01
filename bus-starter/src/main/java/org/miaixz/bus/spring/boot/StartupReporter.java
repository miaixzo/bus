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
import org.miaixz.bus.spring.boot.statics.BaseStatics;
import org.miaixz.bus.spring.boot.statics.BeanStatics;
import org.miaixz.bus.spring.boot.statics.BeanStaticsCustomizer;
import org.miaixz.bus.spring.boot.statics.StartupStatics;
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
 * 收集和启动报告成本的基本组件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StartupReporter {

    public static final Collection<String> SPRING_BEAN_INSTANTIATE_TYPES = Set
            .of(GeniusBuilder.SPRING_BEANS_INSTANTIATE, GeniusBuilder.SPRING_BEANS_SMART_INSTANTIATE);

    public static final Collection<String> SPRING_CONTEXT_POST_PROCESSOR_TYPES = Set.of(
            GeniusBuilder.SPRING_CONTEXT_BEANDEF_REGISTRY_POST_PROCESSOR,
            GeniusBuilder.SPRING_CONTEXT_BEAN_FACTORY_POST_PROCESSOR);

    public static final Collection<String> SPRING_CONFIG_CLASSES_ENHANCE_TYPES = Set
            .of(GeniusBuilder.SPRING_CONFIG_CLASSES_ENHANCE, GeniusBuilder.SPRING_BEAN_POST_PROCESSOR);

    public final StartupStatics statics;

    public final List<BeanStaticsCustomizer> beanStaticsCustomizers;

    public int bufferSize = 4096;

    public int costThreshold = 50;

    public StartupReporter() {
        this.statics = new StartupStatics();
        this.statics.setApplicationBootTime(ManagementFactory.getRuntimeMXBean().getStartTime());
        this.beanStaticsCustomizers = SpringFactoriesLoader.loadFactories(BeanStaticsCustomizer.class,
                StartupReporter.class.getClassLoader());
    }

    /**
     * 将环境绑定到{@link StartupReporter}
     *
     * @param environment 要绑定的环境
     */
    public void bindToStartupReporter(ConfigurableEnvironment environment) {
        try {
            Binder.get(environment).bind("bus.startup", Bindable.ofInstance(this));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot bind to StartupReporter", ex);
        }
    }

    public void setAppName(String appName) {
        this.statics.setAppName(appName);
    }

    /**
     * 结束应用程序启动
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
     * @param stat 增加的CommonStartupStat
     */
    public void addCommonStartupStat(BaseStatics stat) {
        statics.getStageStats().add(stat);
    }

    /**
     * 按名称查找StartupStatics中报告的阶段
     *
     * @param stageName 策略名称
     * @return 报告的对象，当找不到对象时返回null
     */
    public BaseStatics getStageNyName(String stageName) {
        return statics.getStageStats().stream()
                .filter(commonStartupStat -> commonStartupStat.getName().equals(stageName)).findFirst().orElse(null);
    }

    /**
     * 通过从模型中提取阶段返回{@link StartupTimeline }
     *
     * @return 缓冲阶段从缓冲中输出
     */
    public StartupStatics drainStartupStatics() {
        StartupStatics startupReporterStatics = new StartupStatics();
        startupReporterStatics.setAppName(this.statics.getAppName());
        startupReporterStatics.setApplicationBootElapsedTime(this.statics.getApplicationBootElapsedTime());
        startupReporterStatics.setApplicationBootTime(this.statics.getApplicationBootTime());
        List<BaseStatics> stats = new ArrayList<>();
        Iterator<BaseStatics> iterator = this.statics.getStageStats().iterator();
        while (iterator.hasNext()) {
            stats.add(iterator.next());
            iterator.remove();
        }
        startupReporterStatics.setStageStats(stats);
        return startupReporterStatics;
    }

    /**
     * 转换 {@link BufferingApplicationStartup} 到 {@link BeanStatics} 列表.
     *
     * @param context the {@link ConfigurableApplicationContext}.
     * @return 统计列表
     */
    public List<BeanStatics> generateBeanStats(ConfigurableApplicationContext context) {

        List<BeanStatics> rootBeanList = new ArrayList<>();
        ApplicationStartup applicationStartup = context.getApplicationStartup();
        if (applicationStartup instanceof BufferingApplicationStartup bufferingApplicationStartup) {
            Map<Long, BeanStatics> beanStatIdMap = new HashMap<>();

            StartupTimeline startupTimeline = bufferingApplicationStartup.drainBufferedTimeline();

            // 按成本筛选bean初始化器
            List<StartupTimeline.TimelineEvent> timelineEvents = startupTimeline.getEvents();

            // 将启动转换为bean统计
            timelineEvents.forEach(timelineEvent -> {
                BeanStatics bean = eventToBeanStat(timelineEvent);
                rootBeanList.add(bean);
                beanStatIdMap.put(timelineEvent.getStartupStep().getId(), bean);
            });

            // 构建状态树
            timelineEvents.forEach(timelineEvent -> {
                BeanStatics parentBean = beanStatIdMap.get(timelineEvent.getStartupStep().getParentId());
                BeanStatics bean = beanStatIdMap.get(timelineEvent.getStartupStep().getId());

                if (parentBean != null) {
                    // 父节点实际成本减去子节点
                    parentBean.setRealRefreshElapsedTime(parentBean.getRealRefreshElapsedTime() - bean.getCost());
                    // 删除根列表中的子节点
                    rootBeanList.remove(bean);
                    // 如果子列表开销大于阈值，则将其放到父子列表中。
                    if (filterBeanInitializeByCost(bean)) {
                        parentBean.addChild(bean);
                        customBeanStat(context, bean);
                    }
                } else {
                    // 如果根节点小于阈值，则移除根节点。
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

    private boolean filterBeanInitializeByCost(BeanStatics bean) {
        String name = bean.getType();
        if (SPRING_BEAN_INSTANTIATE_TYPES.contains(name) || SPRING_CONTEXT_POST_PROCESSOR_TYPES.contains(name)
                || SPRING_CONFIG_CLASSES_ENHANCE_TYPES.contains(name)) {
            return bean.getCost() >= costThreshold;
        } else {
            return true;
        }
    }

    private BeanStatics eventToBeanStat(StartupTimeline.TimelineEvent timelineEvent) {
        BeanStatics bean = new BeanStatics();
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

    private String getValueFromTags(StartupStep.Tags tags, String key) {
        for (StartupStep.Tag tag : tags) {
            if (Objects.equals(key, tag.getKey())) {
                return tag.getValue();
            }
        }
        return null;
    }

    private BeanStatics customBeanStat(ConfigurableApplicationContext context, BeanStatics beanStat) {
        if (!context.isActive()) {
            return beanStat;
        }
        String type = beanStat.getType();
        if (SPRING_BEAN_INSTANTIATE_TYPES.contains(type)) {
            String beanName = beanStat.getName();
            Object bean = context.getBean(beanName);
            beanStat.putAttribute("classType", AopProxyUtils.ultimateTargetClass(bean).getName());

            BeanStatics result = beanStat;
            for (BeanStaticsCustomizer customizer : beanStaticsCustomizers) {
                BeanStatics current = customizer.customize(beanName, bean, result);
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
