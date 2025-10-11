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
package org.miaixz.bus.starter.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Auto-configuration for the Elasticsearch client.
 * <p>
 * This class sets up the {@link RestClientBuilder} and the new {@link ElasticsearchClient} based on the properties
 * defined in {@link ElasticProperties}.
 *
 * @author <a href="mailto:congchun.zheng@gmail.com">Sixawn.ZHENG</a>
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { ElasticProperties.class })
public class ElasticConfiguration {

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
                    "[ElasticConfiguration.restClientBuilder] Initialization of RestClient failed: Cluster host information is not configured.");
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
