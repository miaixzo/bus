/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.starter.office;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.MathKit;
import org.aoju.bus.core.toolkit.StringKit;
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
 * @version 6.1.1
 * @since JDK 1.8+
 */
@ConditionalOnClass({LocalOfficeProvider.class, OnlineOfficeProvider.class})
@EnableConfigurationProperties(OfficeProperties.class)
public class OfficeConfiguration {

    @Autowired
    OfficeProperties properties;

    @Bean
    public OfficeProviderService previewProviderFactory(final OfficeManager localOfficeManager,
                                                        final OfficeManager onlineOfficeManager) {
        return new OfficeProviderService(
                LocalOfficeProvider.builder()
                        .officeManager(localOfficeManager)
                        .formatRegistry(RegistryInstanceHolder.getInstance())
                        .build(),
                OnlineOfficeProvider.make(onlineOfficeManager));
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public OfficeManager localOfficeManager() {
        final LocalOfficePoolManager.Builder builder = LocalOfficePoolManager.builder();
        if (!StringKit.isBlank(this.properties.getPortNumbers())) {
            builder.portNumbers(
                    ArrayKit.toPrimitive(
                            Stream.of(StringKit.split(this.properties.getPortNumbers(), Symbol.COMMA))
                                    .map(str -> MathKit.toInt(str, Builder.DEFAULT_PORT_NUMBER))
                                    .toArray(Integer[]::new)));
        }

        builder.officeHome(this.properties.getOfficeHome());
        builder.workingDir(this.properties.getWorkingDir());
        builder.templateProfileDir(this.properties.getTemplateProfileDir());
        builder.killExistingProcess(this.properties.isKillExistingProcess());
        builder.processTimeout(this.properties.getProcessTimeout());
        builder.processRetryInterval(this.properties.getProcessRetryInterval());
        builder.taskExecutionTimeout(this.properties.getTaskExecutionTimeout());
        builder.maxTasksPerProcess(this.properties.getMaxTasksPerProcess());
        builder.taskQueueTimeout(this.properties.getTaskQueueTimeout());
        final String processManagerClass = this.properties.getProcessManagerClass();
        if (StringKit.isNotEmpty(processManagerClass)) {
            builder.processManager(processManagerClass);
        } else {
            builder.processManager(Builder.findBestProcessManager());
        }
        return builder.build();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public OfficeManager onlineOfficeManager() {
        final OnlineOfficePoolManager.Builder builder = OnlineOfficePoolManager.builder();
        builder.urlConnection(this.properties.getUrl());
        builder.poolSize(this.properties.getPoolSize());
        builder.workingDir(this.properties.getWorkingDir());
        builder.taskExecutionTimeout(this.properties.getTaskExecutionTimeout());
        builder.taskQueueTimeout(this.properties.getTaskQueueTimeout());
        return builder.build();
    }

}
