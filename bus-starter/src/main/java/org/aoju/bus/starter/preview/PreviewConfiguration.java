package org.aoju.bus.starter.preview;

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.NumberUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Provider;
import org.aoju.bus.office.magic.family.FormatRegistry;
import org.aoju.bus.office.magic.family.JsonFormatRegistry;
import org.aoju.bus.office.magic.family.RegistryInstanceHolder;
import org.aoju.bus.office.metric.OfficeManager;
import org.aoju.bus.office.process.ProcessManager;
import org.aoju.bus.office.provider.LocalProvider;
import org.aoju.bus.office.provider.OnlineProvider;
import org.aoju.bus.office.verbose.LocalOffice;
import org.aoju.bus.office.verbose.OnlineOffice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.util.stream.Stream;

/**
 * 文档在线预览配置
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
@ConditionalOnClass({LocalProvider.class, OnlineProvider.class})
@EnableConfigurationProperties(PreviewProperties.class)
public class PreviewConfiguration {

    @Autowired
    PreviewProperties properties;

    @Bean(name = "localOfficeManager", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(name = "localOfficeManager")
    public OfficeManager localOfficeManager(final ProcessManager processManager) {
        final LocalOffice.Builder builder = LocalOffice.builder();

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
            builder.processManager(processManager);
        }
        return builder.build();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(name = "onlineOfficeManager")
    public OfficeManager onlineOfficeManager() {
        final OnlineOffice.Builder builder = OnlineOffice.builder();
        builder.urlConnection(properties.getUrl());
        builder.poolSize(properties.getPoolSize());
        builder.workingDir(properties.getWorkingDir());
        builder.taskExecutionTimeout(properties.getTaskExecutionTimeout());
        builder.taskQueueTimeout(properties.getTaskQueueTimeout());
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "localDocumentConverter")
    @ConditionalOnBean(name = {"localOfficeManager", "documentFormatRegistry"})
    public Provider localDocumentConverter(
            final OfficeManager localOfficeManager,
            final FormatRegistry formatRegistry) {
        return LocalProvider.builder()
                .officeManager(localOfficeManager)
                .formatRegistry(formatRegistry)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "onlineDocumentConverter")
    @ConditionalOnBean(name = "onlineOfficeManager")
    public Provider onlineDocumentConverter(final OfficeManager onlineOfficeManager) {
        return OnlineProvider.make(onlineOfficeManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "processManager")
    public ProcessManager processManager() {
        return Builder.findBestProcessManager();
    }

    @Bean
    @ConditionalOnMissingBean(name = "documentFormatRegistry")
    public FormatRegistry documentFormatRegistry(final ResourceLoader resourceLoader)
            throws Exception {
        FormatRegistry registry;
        if (StringUtils.isBlank(properties.getDocumentFormatRegistry())) {
            try (InputStream in =
                         resourceLoader.getResource("classpath:document-formats.json").getInputStream()) {
                registry = JsonFormatRegistry.create(in, properties.getFormatOptions());
            }
        } else {
            try (InputStream in =
                         resourceLoader.getResource(properties.getDocumentFormatRegistry()).getInputStream()) {
                registry = JsonFormatRegistry.create(in, properties.getFormatOptions());
            }
        }
        RegistryInstanceHolder.setInstance(registry);
        return registry;
    }

}
