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
package org.miaixz.bus.starter.zookeeper;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the ZooKeeper client.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.ZOOKEEPER)
public class ZookeeperProperties {

    /**
     * The connection string for the ZooKeeper server(s). Multiple servers can be specified, separated by commas (e.g.,
     * "host1:port1,host2:port2").
     */
    private String connectString;

    /**
     * The namespace to be used, which acts as a root path for all znodes created by this client. This provides a form
     * of multi-tenancy.
     */
    private String namespace;

    /**
     * The connection timeout in milliseconds. Default is 15000 ms.
     */
    private int connectionTimeoutMs = 15000;

    /**
     * The session timeout in milliseconds. Default is 60000 ms.
     */
    private int sessionTimeoutMs = 60000;

    /**
     * The initial sleep time in milliseconds for the retry policy. This is used to calculate the wait time for
     * subsequent retries. Default is 1000 ms.
     */
    private int baseSleepTimeMs = 1000;

    /**
     * The maximum number of retries for transient connection errors. Default is 3.
     */
    private int maxRetries = 3;

}
