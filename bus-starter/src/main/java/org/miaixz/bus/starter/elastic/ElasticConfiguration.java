/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.util.List;

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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.Resource;

/**
 * ElasticSearch 规则配置
 *
 * @author <a href="mailto:congchun.zheng@gmail.com">Sixawn.ZHENG</a>
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { ElasticProperties.class })
public class ElasticConfiguration {

    @Resource
    private ElasticProperties properties;

    @Bean
    @ConditionalOnClass
    public RestClientBuilder restClientBuilder() {
        if (CollKit.isEmpty(this.properties.getHostList())) {
            Logger.error("[ElasticConfiguration.restClientBuilder] 初始化 RestClient 失败: 未配置集群主机信息");
            throw new InternalException("初始化 RestClient 失败: 未配置 ElasticSearch 集群主机信息");
        }

        RestClientBuilder restClientBuilder = RestClient
                .builder(this.properties.getHostList().stream().map(this::buildHttpHost).toArray(HttpHost[]::new));

        // 连接延时配置
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(this.properties.getConnectTimeout());
            requestConfigBuilder.setSocketTimeout(this.properties.getSocketTimeout());
            requestConfigBuilder.setConnectionRequestTimeout(this.properties.getConnectionRequestTimeout());
            return requestConfigBuilder;
        });

        // 连接数配置
        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(this.properties.getMaxConnectTotal());
            httpClientBuilder.setMaxConnPerRoute(this.properties.getMaxConnectPerRoute());
            return httpClientBuilder;
        });

        return restClientBuilder;
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClientBuilder restClientBuilder) {
        ElasticsearchTransport transport = new RestClientTransport(restClientBuilder.build(), new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
        return client;
    }

    private HttpHost buildHttpHost(String host) {
        if (StringKit.isBlank(host) || !host.contains(Symbol.COLON)) {
            throw new InternalException("ElasticSearch集群节点信息配置错误, 正确格式[ ip1:port,ip2:port... ]");
        }
        List<String> hostPort = StringKit.split(host, Symbol.COLON);
        return new HttpHost(hostPort.get(Consts.INTEGER_ZERO), Integer.parseInt(hostPort.get(Consts.INTEGER_ONE)),
                this.properties.getSchema());
    }

}