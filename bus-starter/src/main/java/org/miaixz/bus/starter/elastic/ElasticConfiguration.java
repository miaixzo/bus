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

import java.util.List;

import jakarta.annotation.Resource;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.GeniusBuilder;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

/**
 * Auto-configuration for the Elasticsearch client.
 * <p>
 * This class sets up the {@link RestClientBuilder} and the new {@link ElasticsearchClient} based on the properties
 * defined in {@link ElasticProperties}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { ElasticProperties.class })
@ConditionalOnProperty(prefix = GeniusBuilder.ELASTIC, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ElasticConfiguration {

    /**
     * Constructs a new ElasticConfiguration instance.
     */
    public ElasticConfiguration() {
        // No initialization required.
    }

    @Resource
    private ElasticProperties properties;

    /**
     * Creates and configures the {@link RestClientBuilder} bean.
     * <p>
     * This builder is the foundation for the Elasticsearch REST client. It is configured with the cluster hosts,
     * timeouts, and connection pool settings from the properties.
     * </p>
     *
     * @return A configured {@link RestClientBuilder} instance.
     * @throws InternalException if the Elasticsearch host list is not configured.
     */
    @Bean
    @ConditionalOnClass(RestClientBuilder.class)
    public RestClientBuilder restClientBuilder() {
        if (CollKit.isEmpty(this.properties.getHostList())) {
            Logger.error(
                    false,
                    "Starter",
                    "Elasticsearch RestClient initialization failed because cluster host information is not configured.");
            throw new InternalException(
                    "Initialization of RestClient failed: Elasticsearch cluster host information is not configured.");
        }

        HttpHost[] hosts = this.properties.getHostList().stream().map(this::buildHttpHost).toArray(HttpHost[]::new);

        RestClientBuilder restClientBuilder = RestClient.builder(hosts);

        // Configure connection timeouts
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(this.properties.getConnectTimeout());
            requestConfigBuilder.setSocketTimeout(this.properties.getSocketTimeout());
            requestConfigBuilder.setConnectionRequestTimeout(this.properties.getConnectionRequestTimeout());
            return requestConfigBuilder;
        });

        // Configure connection pool size
        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(this.properties.getMaxConnectTotal());
            httpClientBuilder.setMaxConnPerRoute(this.properties.getMaxConnectPerRoute());
            return httpClientBuilder;
        });

        return restClientBuilder;
    }

    /**
     * Creates the modern {@link ElasticsearchClient} bean.
     * <p>
     * This client is the recommended way to interact with Elasticsearch in recent versions. It is built on top of the
     * low-level {@link RestClient}.
     * </p>
     *
     * @param restClientBuilder The configured {@link RestClientBuilder} from the context.
     * @return A new {@link ElasticsearchClient} instance.
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClientBuilder restClientBuilder) {
        ElasticsearchTransport transport = new RestClientTransport(restClientBuilder.build(), new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    /**
     * Helper method to build an {@link HttpHost} from a host string (e.g., "ip:port").
     *
     * @param host The host string.
     * @return A new {@link HttpHost} instance.
     * @throws InternalException if the host string format is incorrect.
     */
    private HttpHost buildHttpHost(String host) {
        if (StringKit.isBlank(host) || !host.contains(Symbol.COLON)) {
            throw new InternalException(
                    "Incorrect Elasticsearch cluster node information configuration. Correct format is [ip1:port,ip2:port...]");
        }
        List<String> hostPort = StringKit.split(host, Symbol.COLON);
        return new HttpHost(hostPort.get(Consts.INTEGER_ZERO), Integer.parseInt(hostPort.get(Consts.INTEGER_ONE)),
                this.properties.getSchema());
    }

}
