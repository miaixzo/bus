/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.starter.elastic;

import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.miaixz.bus.base.normal.Consts;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * <p>@description ElasticSearch 规则配置 </p>
 *
 * @author <a href="mailto:congchun.zheng@gmail.com">Sixawn.ZHENG</a>
 * @since Java 17+
 */
@EnableConfigurationProperties(ElasticProperties.class)
public class ElasticConfiguration {

    @Resource
    private ElasticProperties elasticProperties;

    @Bean
    @ConditionalOnClass
    public RestClientBuilder restClientBuilder() {
        if (CollKit.isEmpty(this.elasticProperties.getHostList())) {
            Logger.error("[ElasticConfiguration.restClientBuilder] 初始化 RestClient 失败: 未配置集群主机信息");
            throw new InternalException("初始化 RestClient 失败: 未配置 ElasticSearch 集群主机信息");
        }

        HttpHost[] hosts = this.elasticProperties.getHostList().stream()
                .map(this::buildHttpHost)
                .toArray(HttpHost[]::new);

        return RestClient.builder(hosts);
    }

    private HttpHost buildHttpHost(String host) {
        if (StringKit.isBlank(host) || !host.contains(Symbol.COLON)) {
            throw new InternalException("ElasticSearch集群节点信息配置错误, 正确格式[ ip1:port,ip2:port... ]");
        }
        List<String> hostPort = StringKit.split(host, Symbol.COLON);
        return new HttpHost(hostPort.get(Consts.INTEGER_ZERO), Integer.parseInt(hostPort.get(Consts.INTEGER_ONE)),
                this.elasticProperties.getSchema());
    }

    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient(RestClientBuilder restClientBuilder) {
        // 异步 HttpClient 连接超时配置
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(this.elasticProperties.getConnectTimeout())
                        .setSocketTimeout(this.elasticProperties.getSocketTimeout())
                        .setConnectionRequestTimeout(this.elasticProperties.getConnectionRequestTimeout())
        );
        // 异步 HttpClient 连接数配置
        restClientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder ->
                httpAsyncClientBuilder
                        .setMaxConnTotal(this.elasticProperties.getMaxConnectTotal())
                        .setMaxConnPerRoute(this.elasticProperties.getMaxConnectPerRoute()));
        return new RestHighLevelClient(restClientBuilder);
    }
}
