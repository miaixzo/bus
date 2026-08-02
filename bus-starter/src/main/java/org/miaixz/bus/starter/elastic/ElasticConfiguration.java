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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.starter.GeniusBuilder;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.Jackson3JsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;

/**
 * Configures the Elasticsearch low-level REST client.
 * <p>
 * This class sets up the {@link Rest5ClientBuilder} and the new {@link ElasticsearchClient} based on the properties
 * defined in {@link ElasticProperties}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { ElasticProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ Rest5Client.class, Rest5ClientTransport.class, ElasticsearchClient.class })
@ConditionalOnProperty(prefix = GeniusBuilder.ELASTIC, name = "enabled", havingValue = "true", matchIfMissing = false)
public class ElasticConfiguration {

    /**
     * Bound elastic configuration properties.
     */
    private final ElasticProperties properties;

    /**
     * Stores the hosts, credentials, timeouts, and connection limits used to construct the REST client.
     *
     * @param properties bound configuration properties
     */
    public ElasticConfiguration(ElasticProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates and configures the low-level {@link Rest5Client} bean.
     * <p>
     * This builder is the foundation for the Elasticsearch REST client. It is configured with the cluster hosts,
     * timeouts, and connection pool settings from the properties.
     * </p>
     *
     * @return A configured {@link Rest5Client} instance.
     * @throws InternalException if the Elasticsearch host list is not configured.
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(Rest5Client.class)
    public Rest5Client restClient() {
        if (CollKit.isEmpty(this.properties.getHostList())) {
            Logger.error(
                    false,
                    "Starter",
                    "Elasticsearch RestClient initialization failed because cluster host information is not configured.");
            throw new InternalException(
                    "Initialization of RestClient failed: Elasticsearch cluster host information is not configured.");
        }

        URI[] hosts = this.properties.getHostList().stream().map(this::buildUri).toArray(URI[]::new);

        Rest5ClientBuilder restClientBuilder = Rest5Client.builder(hosts);

        // Configure connection timeouts
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(this.properties.getConnectTimeout(), TimeUnit.MILLISECONDS);
            requestConfigBuilder.setResponseTimeout(this.properties.getSocketTimeout(), TimeUnit.MILLISECONDS);
            requestConfigBuilder
                    .setConnectionRequestTimeout(this.properties.getConnectionRequestTimeout(), TimeUnit.MILLISECONDS);
        });

        // Configure connection pool size
        restClientBuilder.setConnectionManagerCallback(connectionManagerBuilder -> {
            connectionManagerBuilder.setMaxConnTotal(this.properties.getMaxConnectTotal());
            connectionManagerBuilder.setMaxConnPerRoute(this.properties.getMaxConnectPerRoute());
        });

        return restClientBuilder.build();
    }

    /**
     * Creates the transport and closes the low-level client if transport construction fails.
     *
     * @param restClient configured low-level client
     * @return Elasticsearch transport
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(Rest5ClientTransport.class)
    public Rest5ClientTransport restClientTransport(Rest5Client restClient) {
        try {
            return new Rest5ClientTransport(restClient, new Jackson3JsonpMapper());
        } catch (RuntimeException | Error exception) {
            closeAfterFailure(exception, restClient);
            throw exception;
        }
    }

    /**
     * Creates the modern {@link ElasticsearchClient} bean.
     * <p>
     * This client is the recommended way to interact with Elasticsearch in recent versions. It is built on top of the
     * low-level {@link Rest5Client}.
     * </p>
     *
     * @param transport  The configured {@link Rest5ClientTransport} from the context.
     * @param restClient The configured low-level client used for failure rollback.
     * @return A new {@link ElasticsearchClient} instance.
     */
    @Bean
    @ConditionalOnMissingBean(ElasticsearchClient.class)
    public ElasticsearchClient elasticsearchClient(Rest5ClientTransport transport, Rest5Client restClient) {
        try {
            return new ElasticsearchClient(transport);
        } catch (RuntimeException | Error exception) {
            closeAfterFailure(exception, transport, restClient);
            throw exception;
        }
    }

    /**
     * Helper method to build a {@link URI} from a host string (e.g., "ip:port").
     *
     * @param host The host string.
     * @return A new {@link URI} instance.
     * @throws InternalException if the host string format is incorrect.
     */
    private URI buildUri(String host) {
        if (StringKit.isBlank(host) || !host.contains(Symbol.COLON)) {
            throw new InternalException(
                    "Incorrect Elasticsearch cluster node information configuration. Correct format is [ip1:port,ip2:port...]");
        }
        List<String> hostPort = StringKit.split(host, Symbol.COLON);
        try {
            return new URI(this.properties.getSchema(), null, hostPort.get(Consts.INTEGER_ZERO),
                    Integer.parseInt(hostPort.get(Consts.INTEGER_ONE)), null, null, null);
        } catch (URISyntaxException e) {
            throw new InternalException(
                    "Incorrect Elasticsearch cluster node information configuration. Correct format is [ip1:port,ip2:port...]",
                    e);
        }
    }

    /**
     * Closes partially created resources and attaches close failures to the original failure.
     *
     * @param failure   original creation failure
     * @param resources resources to close in declaration order
     */
    private void closeAfterFailure(Throwable failure, AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            try {
                resource.close();
            } catch (Exception closeException) {
                failure.addSuppressed(closeException);
            }
        }
    }

}
