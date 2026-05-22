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
package org.miaixz.bus.starter.mapper;

import java.util.Properties;

import org.springframework.core.env.Environment;

import org.miaixz.bus.mapper.feature.audit.AuditProvider;
import org.miaixz.bus.mapper.feature.populate.PopulateProvider;
import org.miaixz.bus.mapper.feature.prefix.TablePrefixProvider;
import org.miaixz.bus.mapper.feature.tenant.TenantProvider;
import org.miaixz.bus.mapper.feature.visible.VisibleProvider;
import org.miaixz.bus.mapper.handler.MybatisInterceptor;
import org.miaixz.bus.mapper.runtime.MapperPluginFactory;
import org.miaixz.bus.mapper.runtime.MapperPluginProviders;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.SpringBuilder;
import org.miaixz.bus.spring.annotation.PlaceHolderBinder;

/**
 * Starter adapter for creating the MyBatis mapper interceptor.
 * <p>
 * Pure plugin-chain construction lives in {@link MapperPluginFactory}; this class only binds Spring Boot properties and
 * resolves provider beans from the Spring container.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MapperPluginBuilder {

    /**
     * Constructs a new MapperPluginBuilder instance.
     */
    public MapperPluginBuilder() {
        // No initialization required.
    }

    /**
     * Builds and configures the primary mapper interceptor.
     * <p>
     * Spring-specific work stops at property binding and provider lookup. The actual handler chain is delegated to
     * {@link MapperPluginFactory} so the mapper module owns plugin assembly without depending on Spring.
     *
     * @param environment Spring environment
     * @return configured interceptor
     */
    public static MybatisInterceptor build(Environment environment) {
        if (environment == null) {
            return MapperPluginFactory.build(null);
        }
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);
        if (properties == null) {
            properties = new MapperProperties();
        }
        return MapperPluginFactory.build(properties, providers(properties));
    }

    /**
     * Resolves optional provider beans needed by mapper handlers.
     * <p>
     * Providers are only queried when the matching simplified configuration exists or when legacy flattened handler
     * configuration is present. This keeps provider lookup lazy while preserving the previous configuration-file path.
     *
     * @param properties mapper properties bound from the Spring environment
     * @return provider holder passed to the pure mapper plugin factory
     */
    private static MapperPluginProviders providers(MapperProperties properties) {
        MapperPluginProviders providers = new MapperPluginProviders();
        Properties resolved = properties.resolveConfigurationProperties();
        boolean hasConfigFile = resolved != null && !resolved.isEmpty();
        if (properties.getTenant() != null || hasConfigFile) {
            providers.setTenantProvider(provider(TenantProvider.class));
        }
        if (properties.getPrefix() != null || hasConfigFile) {
            providers.setPrefixProvider(provider(TablePrefixProvider.class));
        }
        if (properties.getVisible() != null || hasConfigFile) {
            providers.setVisibleProvider(provider(VisibleProvider.class));
        }
        if (properties.getPopulate() != null || hasConfigFile) {
            providers.setPopulateProvider(provider(PopulateProvider.class));
        }
        if (properties.getAudit() != null || hasConfigFile) {
            providers.setAuditProvider(provider(AuditProvider.class));
        }
        return providers;
    }

    /**
     * Looks up a provider bean by type from the Spring container.
     * <p>
     * A missing provider is normal for most applications, so lookup failures are treated as absence rather than startup
     * errors.
     *
     * @param providerType provider type to resolve
     * @param <T>          provider type
     * @return provider bean, or {@code null} when none is available
     */
    private static <T> T provider(Class<T> providerType) {
        try {
            return SpringBuilder.getBean(providerType);
        } catch (Exception e) {
            return null;
        }
    }

}
