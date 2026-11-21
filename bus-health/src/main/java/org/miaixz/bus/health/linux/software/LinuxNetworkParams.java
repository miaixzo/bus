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
package org.miaixz.bus.health.linux.software;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import com.sun.jna.platform.unix.LibCAPI;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.NetworkParams;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.driver.proc.RouteTable;
import org.miaixz.bus.health.linux.jna.LinuxLibc;
import org.miaixz.bus.health.unix.shared.jna.CLibrary;
import org.miaixz.bus.logger.Logger;

/**
 * LinuxNetworkParams class.
 *
 * @author Kimi Liu
 */
@ThreadSafe
final class LinuxNetworkParams extends AbstractNetworkParams {

    /**
     * Resolver configuration containing systemd-resolved upstream servers.
     */
    private static final String RESOLVED_UPSTREAM_CONF = "/run/systemd/resolve/resolv.conf";

    /**
     * Addresses used by systemd-resolved stub listeners.
     */
    private static final Set<String> RESOLVED_STUBS = new HashSet<>(Arrays.asList("127.0.0.53", "127.0.0.54"));

    /**
     * The LIBC constant.
     */
    private static final LinuxLibc LIBC = LinuxLibc.INSTANCE;

    /**
     * Returns configured DNS servers, replacing systemd-resolved stub addresses with their upstream servers.
     *
     * @return the DNS server addresses
     */
    @Override
    public String[] getDnsServers() {
        return getDnsServers(
                Builder.readFile("/etc/resolv.conf"),
                () -> Builder.readFile(RESOLVED_UPSTREAM_CONF, false));
    }

    /**
     * Selects DNS servers from the primary resolver configuration or systemd-resolved's upstream file.
     *
     * @param resolv   the primary resolver configuration lines
     * @param upstream supplies systemd-resolved's upstream configuration lines
     * @return the DNS server addresses
     */
    static String[] getDnsServers(List<String> resolv, Supplier<List<String>> upstream) {
        String[] servers = parseDnsServers(resolv);
        if (servers.length > 0 && Arrays.stream(servers).allMatch(RESOLVED_STUBS::contains)) {
            String[] upstreamServers = parseDnsServers(upstream.get(), Integer.MAX_VALUE);
            if (upstreamServers.length > 0) {
                return upstreamServers;
            }
        }
        return servers;
    }

    /**
     * Returns the domain name.
     *
     * @return the get domain name result
     */
    @Override
    public String getDomainName() {
        try (CLibrary.Addrinfo hint = new CLibrary.Addrinfo()) {
            hint.ai_flags = CLibrary.AI_CANONNAME;
            String hostname;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                Logger.warn(
                        false,
                        "Health",
                        "Unknown host exception when getting address of local host: {}",
                        e.getClass().getSimpleName());
                return Normal.EMPTY;
            }
            try (ByRef.CloseablePointerByReference ptr = new ByRef.CloseablePointerByReference()) {
                int res = LIBC.getaddrinfo(hostname, null, hint, ptr);
                if (res != 0) {
                    if (Logger.isErrorEnabled()) {
                        Logger.error(false, "Health", "Failed getaddrinfo(): {}", LIBC.gai_strerror(res));
                    }
                    return Normal.EMPTY;
                }
                CLibrary.Addrinfo info = new CLibrary.Addrinfo(ptr.getValue());
                try {
                    return info.ai_canonname == null ? hostname : info.ai_canonname.trim();
                } finally {
                    LIBC.freeaddrinfo(ptr.getValue());
                }
            }
        }
    }

    /**
     * Returns the host name.
     *
     * @return the get host name result
     */
    @Override
    public String getHostName() {
        String hostname = Builder.getStringFromFile(ProcPath.SYS_KERNEL_HOSTNAME);
        if (!hostname.isEmpty()) {
            return hostname;
        }
        byte[] hostnameBuffer = new byte[LibCAPI.HOST_NAME_MAX + 1];
        if (0 != LibC.INSTANCE.gethostname(hostnameBuffer, hostnameBuffer.length)) {
            return super.getHostName();
        }
        return Native.toString(hostnameBuffer);
    }

    /**
     * Returns the ipv4 default gateway.
     *
     * @return the get ipv4 default gateway result
     */
    @Override
    public String getIpv4DefaultGateway() {
        return defaultGateway(RouteTable.queryIpv4Routes(Collections.emptyMap()));
    }

    /**
     * Returns the ipv6 default gateway.
     *
     * @return the get ipv6 default gateway result
     */
    @Override
    public String getIpv6DefaultGateway() {
        return defaultGateway(RouteTable.queryIpv6Routes(Collections.emptyMap()));
    }

    /**
     * Selects the default gateway with the lowest metric.
     *
     * @param routes The routes for one address family.
     * @return The gateway address, or an empty string if no default gateway exists.
     */
    static String defaultGateway(List<NetworkParams.IPRoute> routes) {
        NetworkParams.IPRoute best = null;
        for (NetworkParams.IPRoute route : routes) {
            if (route.getPrefixLength() == Normal._0 && route.isGateway() && route.getGateway().length > Normal._0
                    && (best == null || route.getMetric() < best.getMetric())) {
                best = route;
            }
        }
        return best == null ? Normal.EMPTY : Parsing.formatIpAddress(best.getGateway());
    }

    /**
     * Returns the routing table.
     *
     * @return the routing table
     */
    @Override
    public List<NetworkParams.IPRoute> getRoutes() {
        Map<String, Integer> ifIndexByName = queryInterfaceIndexByName();
        List<NetworkParams.IPRoute> routes = new ArrayList<>(RouteTable.queryIpv4Routes(ifIndexByName));
        routes.addAll(RouteTable.queryIpv6Routes(ifIndexByName));
        return routes;
    }

}
