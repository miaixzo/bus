/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
