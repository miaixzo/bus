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
package org.miaixz.bus.starter.elastic;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableElastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.Jackson3JsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;

/**
 * Configures the Elasticsearch client on top of the Bus Fabric HTTP runtime.
 * <p>
 * The typed {@link ElasticsearchClient} API is preserved while the transport is provided by {@link FabricTransport}.
 * This removes the Elasticsearch REST 5 client and Apache HttpClient 5 dependencies from the starter.
 * </p>
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(value = { ElasticProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ ElasticsearchClient.class })
@ConditionalOnEnabled(annotation = EnableElastic.class, prefix = GeniusBuilder.ELASTIC)
public class ElasticConfiguration {

    /**
     * Bound elastic configuration properties.
     */
    private final ElasticProperties properties;

    /**
     * Stores the hosts, credentials, timeouts, and connection limits used to construct the client.
     *
     * @param properties bound configuration properties
     */
    public ElasticConfiguration(ElasticProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the Elasticsearch transport backed by Fabric.
     * <p>
     * The transport owns the Fabric context, the connection pool and the asynchronous executor. It is closed by the
     * Spring container when the application context shuts down.
     * </p>
     *
     * @param tlsPolicies optional custom TLS policy; when absent, the Elasticsearch defaults are used
     * @return a configured transport
     * @throws InternalException if the Elasticsearch host list is not configured
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(ElasticsearchTransport.class)
    public ElasticsearchTransport elasticsearchTransport(ObjectProvider<TlsPolicy> tlsPolicies) {
        if (CollKit.isEmpty(this.properties.getHostList())) {
            Logger.error(
                    false,
                    "Starter",
                    "Elasticsearch client initialization failed because cluster host information is not configured.");
            throw new InternalException(
                    "Initialization of Elasticsearch client failed: Elasticsearch cluster host information is not configured.");
        }
        return new FabricTransport(new FabricHttpTransportClient(this.properties, tlsPolicies.getIfAvailable()), null,
                new Jackson3JsonpMapper());
    }

    /**
     * Creates the modern {@link ElasticsearchClient} bean on top of the configured transport.
     *
     * @param transport the Elasticsearch transport
     * @return a new {@link ElasticsearchClient} instance
     */
    @Bean
    @ConditionalOnMissingBean(ElasticsearchClient.class)
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        try {
            return new ElasticsearchClient(transport);
        } catch (RuntimeException | Error exception) {
            closeQuietly(transport);
            throw exception;
        }
    }

    /**
     * Closes a resource without masking the original failure.
     *
     * @param resource resource to close, may be {@code null}
     */
    private static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception ignored) {
            // Preserve the original failure.
        }
    }

}
