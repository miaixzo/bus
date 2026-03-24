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

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * Auto-configuration for the Apache Curator ZooKeeper client.
 * <p>
 * This class enables {@link ZookeeperProperties} and creates a {@link CuratorFramework} bean, which is the main entry
 * point for interacting with ZooKeeper.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(ZookeeperProperties.class)
public class ZookeeperConfiguration {

    @Resource
    private ZookeeperProperties properties;

    /**
     * Creates and configures the {@link CuratorFramework} client bean.
     * <p>
     * The bean's lifecycle is managed by Spring, with its {@code start} and {@code close} methods called automatically
     * on application startup and shutdown. The client is configured with connection settings and a retry policy based
     * on the {@link ZookeeperProperties}. This bean is only created if no other {@link CuratorFramework} bean is
     * already present.
     * </p>
     *
     * @return A fully configured and started {@link CuratorFramework} instance.
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework zkClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(this.properties.getBaseSleepTimeMs(),
                this.properties.getMaxRetries());
        return CuratorFrameworkFactory.builder().connectString(this.properties.getConnectString())
                .namespace(this.properties.getNamespace()).sessionTimeoutMs(this.properties.getSessionTimeoutMs())
                .connectionTimeoutMs(this.properties.getConnectionTimeoutMs()).retryPolicy(retryPolicy).build();
    }

}
