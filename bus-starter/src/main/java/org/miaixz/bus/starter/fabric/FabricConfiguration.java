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
package org.miaixz.bus.starter.fabric;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.fabric.network.dns.observe.DnsMetrics;
import org.miaixz.bus.fabric.network.dns.observe.DnsQueryLog;
import org.miaixz.bus.fabric.network.dns.provider.DnsDynamicUpdateSink;
import org.miaixz.bus.fabric.network.dns.provider.DnsSnapshotListener;
import org.miaixz.bus.fabric.network.dns.provider.DnsSnapshotProvider;
import org.miaixz.bus.fabric.network.dns.server.DnsEndpoint;
import org.miaixz.bus.fabric.network.dns.server.DnsServer;
import org.miaixz.bus.fabric.network.dns.server.DnsServerOptions;
import org.miaixz.bus.fabric.network.dns.server.DnsTransport;
import org.miaixz.bus.fabric.network.dns.server.DnsTsigKey;
import org.miaixz.bus.fabric.network.dns.zone.CidrBlock;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableFabric;

/**
 * Configures TCP, WebSocket, and DNS fabric communication services.
 * <p>
 * This class is the single Spring configuration entry for Fabric and creates the runtime beans for each enabled
 * protocol capability.
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(value = { FabricProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.fabric.Fabric")
@ConditionalOnEnabled(annotation = EnableFabric.class, prefix = GeniusBuilder.FABRIC)
public class FabricConfiguration {

    /**
     * Bound fabric configuration properties.
     */
    private final FabricProperties properties;

    /**
     * Stores the transport properties used to construct TCP and WebSocket services.
     *
     * @param properties bound configuration properties
     */
    public FabricConfiguration(FabricProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the {@link SocketQuickService} bean.
     * <p>
     * This bean starts and stops the configured fabric socket server with the Spring application context. The bean is
     * only created when no other socket quick service bean is already present.
     * </p>
     *
     * @return socket quick service
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(SocketQuickService.class)
    @ConditionalOnProperty(prefix = GeniusBuilder.FABRIC
            + ".socket", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SocketQuickService socketQuickService() {
        return new SocketQuickService(this.properties);
    }

    /**
     * Creates the {@link WebSocketQuickService} bean.
     * <p>
     * This bean starts and stops the configured fabric WebSocket server with the Spring application context. The bean
     * is only created when the WebSocket service is explicitly enabled.
     * </p>
     *
     * @return WebSocket quick service
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(WebSocketQuickService.class)
    @ConditionalOnProperty(prefix = GeniusBuilder.FABRIC
            + ".websocket", name = "enabled", havingValue = "true", matchIfMissing = false)
    public WebSocketQuickService webSocketQuickService() {
        return new WebSocketQuickService(this.properties);
    }

    /**
     * Creates and starts the optional Fabric DNS server.
     *
     * @param provider    external DNS snapshot provider
     * @param tlsPolicies optional TLS policies for DNS over TLS
     * @param listeners   optional snapshot lifecycle listeners
     * @param updateSinks optional Dynamic Update sinks
     * @param tsigKeys    optional TSIG keys
     * @return configured DNS server
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean(DnsServer.class)
    @ConditionalOnProperty(prefix = GeniusBuilder.FABRIC
            + ".dns", name = "enabled", havingValue = "true", matchIfMissing = false)
    public DnsServer dnsServer(
            final DnsSnapshotProvider provider,
            final ObjectProvider<TlsPolicy> tlsPolicies,
            final ObjectProvider<DnsSnapshotListener> listeners,
            final ObjectProvider<DnsDynamicUpdateSink> updateSinks,
            final ObjectProvider<DnsTsigKey> tsigKeys) {
        return DnsServer.create(dnsOptions(provider, tlsPolicies, listeners, updateSinks, tsigKeys));
    }

    /**
     * Composes immutable DNS server options from bound properties and ordered extension Beans.
     *
     * @param provider    external DNS snapshot provider
     * @param tlsPolicies optional TLS policies for DNS over TLS
     * @param listeners   optional snapshot lifecycle listeners
     * @param updateSinks optional Dynamic Update sinks
     * @param tsigKeys    optional TSIG keys
     * @return complete DNS server options
     */
    private DnsServerOptions dnsOptions(
            final DnsSnapshotProvider provider,
            final ObjectProvider<TlsPolicy> tlsPolicies,
            final ObjectProvider<DnsSnapshotListener> listeners,
            final ObjectProvider<DnsDynamicUpdateSink> updateSinks,
            final ObjectProvider<DnsTsigKey> tsigKeys) {
        final FabricProperties.Dns dns = properties.getDns();
        DnsServerOptions options = DnsServerOptions.provider(provider, List.of(dnsEndpoint(dns)))
                .withMaxUdpPayloadBytes(dns.getMaxUdpPayloadBytes()).withRateLimitPerSecond(dns.getRateLimitPerSecond())
                .withSnapshotListeners(listeners.orderedStream().toList())
                .withTsigKeys(tsigKeys.orderedStream().toList()).withIoThreads(dns.getIoThreads())
                .withTcpIdleTimeout(dns.getTcpIdleTimeout()).withTcpMaxInFlight(dns.getTcpMaxInFlight())
                .withTcpMaxFrameBytes(dns.getTcpMaxFrameBytes()).withQuicMaxStreams(dns.getQuicMaxStreams())
                .withQuicIdleTimeout(dns.getQuicIdleTimeout())
                .withRecursionAllowedCidrs(dns.isRecursion() ? cidrs(dns.getRecursionAllowedCidrs()) : List.of())
                .withZoneTransferAllowedCidrs(
                        dns.isZoneTransfer() ? cidrs(dns.getZoneTransferAllowedCidrs()) : List.of());
        final DnsDynamicUpdateSink updateSink = updateSinks.orderedStream().findFirst().orElse(null);
        if (dns.isDynamicUpdate() && updateSink != null) {
            options = options.withDynamicUpdateSink(updateSink);
        }
        options = options.withCache(
                dns.isCache() ? dns.getCacheMaxEntries() : 0,
                dns.getCacheTtl(),
                dns.getCacheServeStaleTtl(),
                dns.getCachePrefetchBeforeExpiry());
        if (dns.getTransport() == DnsTransport.DOT || dns.isDot()) {
            options = options.withTlsPolicy(tlsPolicies.orderedStream().findFirst().orElse(null));
        }
        if (dns.isMetrics()) {
            options = options.withMetrics(DnsMetrics.create());
        }
        if (dns.isQueryLog()) {
            options = options.withQueryLog(DnsQueryLog.create());
        }
        return options;
    }

    /**
     * Parses DNS starter CIDR strings.
     *
     * @param values CIDR strings
     * @return immutable CIDR blocks
     */
    private List<CidrBlock> cidrs(final List<String> values) {
        return values.stream().map(CidrBlock::parse).toList();
    }

    /**
     * Creates the transport-specific DNS listener endpoint.
     *
     * @param dns bound DNS transport and network settings
     * @return configured DNS endpoint
     */
    private DnsEndpoint dnsEndpoint(final FabricProperties.Dns dns) {
        return switch (dns.getTransport()) {
            case UDP -> DnsEndpoint.udp(dns.getHost(), dns.getPort());
            case TCP -> DnsEndpoint.tcp(dns.getHost(), dns.getPort());
            case DOH -> DnsEndpoint.doh(dns.getHost(), dns.getPort());
            case DOQ -> DnsEndpoint.doq(dns.getHost(), dns.getPort());
            case DOT -> DnsEndpoint.dot(dns.getHost(), dns.getPort());
        };
    }

}
