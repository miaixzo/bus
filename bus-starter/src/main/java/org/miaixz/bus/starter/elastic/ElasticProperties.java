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

import lombok.Data;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.spring.BusXConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>@description ElasticSearch 配置属性类 </p>
 *
 * @author <a href="mailto:congchun.zheng@gmail.com">Sixawn.ZHENG</a>
 * @since Java 17+
 */
@Data
@ConfigurationProperties(prefix = BusXConfig.ELASTIC)
public class ElasticProperties {
    /**
     * 集群主机地址, 多个用英文逗号,隔开
     * 格式: ip1:port,ip2:port
     */
    private String hosts;
    /**
     * 通讯协议
     */
    private String schema = "http";

    /**
     * 建立连接超时时间: 毫秒, 默认 6000， 0 - 无限制，-1 - OS 适配
     */
    private int connectTimeout = 6000;
    /**
     * 读超时: 毫秒，默认 60000， 0 - 无限制，-1 - OS 适配
     */
    private int socketTimeout = 60000;
    /**
     * 连接请求超时: 毫秒，默认 6000， 0 - 无限制，-1 - OS 适配
     */
    private int connectionRequestTimeout = 6000;

    /**
     * 最大连接数: 默认 2000， 0 - 无限制，-1 - OS 适配
     */
    private int maxConnectTotal = 2000;
    /**
     * 最大每批连接数: 默认 200， 0 - 无限制，-1 - OS 适配
     */
    private int maxConnectPerRoute = 500;

    /**
     * 集群主机地址列表
     */
    private List<String> hostList;

    public List<String> getHostList() {
        if (null == this.hosts || "".equalsIgnoreCase(this.hosts.trim())) {
            return Collections.emptyList();
        }
        return Arrays.asList(this.hosts.split(Symbol.COMMA));
    }
}
