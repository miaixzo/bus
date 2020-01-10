/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.starter.preview;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.NumberUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.bridge.LocalOfficePoolManager;
import org.aoju.bus.office.bridge.OnlineOfficePoolManager;
import org.aoju.bus.office.magic.family.RegistryInstanceHolder;
import org.aoju.bus.office.metric.OfficeManager;
import org.aoju.bus.office.provider.LocalOfficeProvider;
import org.aoju.bus.office.provider.OnlineOfficeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.stream.Stream;

/**
 * 文档在线预览配置
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
@ConditionalOnClass({LocalOfficeProvider.class, OnlineOfficeProvider.class})
@EnableConfigurationProperties(PreviewProperties.class)
public class PreviewConfiguration {

    @Autowired
    PreviewProperties properties;

    @Bean
    public PreviewProviderService previewProviderFactory(final OfficeManager localOfficeManager,
                                                         final OfficeManager onlineOfficeManager) {
        return new PreviewProviderService(
                LocalOfficeProvider.builder()
                        .officeManager(localOfficeManager)
                        .formatRegistry(RegistryInstanceHolder.getInstance())
                        .build(),
                OnlineOfficeProvider.make(onlineOfficeManager));
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public OfficeManager localOfficeManager() {
        final LocalOfficePoolManager.Builder builder = LocalOfficePoolManager.builder();
        if (!StringUtils.isBlank(properties.getPortNumbers())) {
            builder.portNumbers(
                    ArrayUtils.toPrimitive(
                            Stream.of(StringUtils.split(properties.getPortNumbers(), Symbol.COMMA))
                                    .map(str -> NumberUtils.toInt(str, Builder.DEFAULT_PORT_NUMBER))
                                    .toArray(Integer[]::new)));
        }

        builder.officeHome(properties.getOfficeHome());
        builder.workingDir(properties.getWorkingDir());
        builder.templateProfileDir(properties.getTemplateProfileDir());
        builder.killExistingProcess(properties.isKillExistingProcess());
        builder.processTimeout(properties.getProcessTimeout());
        builder.processRetryInterval(properties.getProcessRetryInterval());
        builder.taskExecutionTimeout(properties.getTaskExecutionTimeout());
        builder.maxTasksPerProcess(properties.getMaxTasksPerProcess());
        builder.taskQueueTimeout(properties.getTaskQueueTimeout());
        final String processManagerClass = properties.getProcessManagerClass();
        if (StringUtils.isNotEmpty(processManagerClass)) {
            builder.processManager(processManagerClass);
        } else {
            builder.processManager(Builder.findBestProcessManager());
        }
        return builder.build();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public OfficeManager onlineOfficeManager() {
        final OnlineOfficePoolManager.Builder builder = OnlineOfficePoolManager.builder();
        builder.urlConnection(properties.getUrl());
        builder.poolSize(properties.getPoolSize());
        builder.workingDir(properties.getWorkingDir());
        builder.taskExecutionTimeout(properties.getTaskExecutionTimeout());
        builder.taskQueueTimeout(properties.getTaskQueueTimeout());
        return builder.build();
    }

}
