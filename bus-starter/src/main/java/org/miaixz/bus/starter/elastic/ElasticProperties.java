/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.elastic;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration properties for Elasticsearch.
 *
 * @author <a href="mailto:congchun.zheng@gmail.com">Sixawn.ZHENG</a>
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.ELASTIC)
public class ElasticProperties {

    /**
     * Comma-separated list of cluster host addresses. Format: ip1:port,ip2:port
     */
    private String hosts;

    /**
     * The username for authentication.
     */
    private String username;

    /**
     * The password for authentication.
     */
    private String password;

    /**
     * The communication protocol (e.g., "http" or "https"). Default is "http".
     */
    private String schema = "http";

    /**
     * The connection timeout in milliseconds. Default is 6000. 0 means no timeout, -1 means OS-adaptive.
     */
    private int connectTimeout = 6000;

    /**
     * The socket read timeout in milliseconds. Default is 60000. 0 means no timeout, -1 means OS-adaptive.
     */
    private int socketTimeout = 60000;

    /**
     * The connection request timeout in milliseconds. Default is 6000. 0 means no timeout, -1 means OS-adaptive.
     */
    private int connectionRequestTimeout = 6000;

    /**
     * The maximum total number of connections. Default is 2000. 0 means no limit, -1 means OS-adaptive.
     */
    private int maxConnectTotal = 2000;

    /**
     * The maximum number of connections per route. Default is 500. 0 means no limit, -1 means OS-adaptive.
     */
    private int maxConnectPerRoute = 500;

    /**
     * Lazily parses the {@link #hosts} string into a list of individual hosts.
     *
     * @return A list of host strings, or an empty list if {@link #hosts} is not set.
     */
    public List<String> getHostList() {
        if (null == this.hosts || Normal.EMPTY.equalsIgnoreCase(this.hosts.trim())) {
            return Collections.emptyList();
        }
        return Arrays.asList(this.hosts.split(Symbol.COMMA));
    }

}
