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
package org.miaixz.bus.health.builtin.software.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.software.NetworkParams;
import org.miaixz.bus.logger.Logger;

/**
 * Common NetworkParams implementation.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public abstract class AbstractNetworkParams implements NetworkParams {

    /**
     * The NAMESERVER constant.
     */
    private static final String NAMESERVER = "nameserver";

    /**
     * Memoized local host lookup.
     */
    private final SupplierX<InetAddress> localHost = Memoizer
            .memoize(this::queryLocalHost, Memoizer.defaultExpiration());

    /**
     * Constructs a new AbstractNetworkParams instance.
     */
    public AbstractNetworkParams() {
        // No initialization required.
    }

    /**
     * Convenience method to parse the output of the `route` command. While the command arguments vary between OS's the
     * output is consistently parsable.
     *
     * @param lines output of OS-specific route command
     * @return default gateway
     */
    protected static String searchGateway(List<String> lines) {
        for (String line : lines) {
            String leftTrimmed = line.replaceFirst("^\\s+", Normal.EMPTY);
            if (leftTrimmed.startsWith("gateway:")) {
                String[] split = Pattern.SPACES_PATTERN.split(leftTrimmed);
                if (split.length < 2) {
                    return Normal.EMPTY;
                }
                return split[1].split(Symbol.PERCENT)[0];
            }
        }
        return Normal.EMPTY;
    }

    /**
     * Maps interface names to their indices.
     *
     * @return A map of interface name to interface index.
     */
    protected static Map<String, Integer> queryInterfaceIndexByName() {
        Map<String, Integer> map = new HashMap<>();
        for (NetworkInterface netIf : queryNetworkInterfaces()) {
            map.put(netIf.getName(), netIf.getIndex());
        }
        return map;
    }

    /**
     * Maps interface indices to their names.
     *
     * @return A map of interface index to interface name.
     */
    protected static Map<Integer, String> queryInterfaceNameByIndex() {
        Map<Integer, String> map = new HashMap<>();
        for (NetworkInterface netIf : queryNetworkInterfaces()) {
            map.put(netIf.getIndex(), netIf.getName());
        }
        return map;
    }

    /**
     * Queries available network interfaces.
     *
     * @return A list of network interfaces.
     */
    private static List<NetworkInterface> queryNetworkInterfaces() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            return interfaces == null ? Collections.emptyList() : Collections.list(interfaces);
        } catch (Exception e) {
            Logger.debug(false, "Health", "Socket exception when retrieving interfaces", e);
            return Collections.emptyList();
        }
    }

    /**
     * Returns the domain name.
     *
     * @return the get domain name result
     */
    @Override
    public String getDomainName() {
        InetAddress addr = this.localHost.get();
        return addr == null ? Normal.EMPTY : addr.getCanonicalHostName();
    }

    /**
     * Returns the host name.
     *
     * @return the get host name result
     */
    @Override
    public String getHostName() {
        InetAddress addr = this.localHost.get();
        if (addr == null) {
            return Normal.EMPTY;
        }
        String hn = addr.getHostName();
        int dot = hn.indexOf(Symbol.C_DOT);
        if (dot == -1) {
            return hn;
        }
        return hn.substring(0, dot);
    }

    /**
     * Resolves the local host.
     *
     * @return The local host, or {@code null} if it does not resolve.
     */
    protected InetAddress queryLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            Logger.debug(false, "Health", "Unknown host exception when getting address of local host", e);
            return null;
        }
    }

    /**
     * Returns the dns servers.
     *
     * @return the get dns servers result
     */
    @Override
    public String[] getDnsServers() {
        List<String> resolv = Builder.readFile("/etc/resolv.conf");
        String key = NAMESERVER;
        int maxNameServer = 3;
        List<String> servers = new ArrayList<>();
        for (int i = 0; i < resolv.size() && servers.size() < maxNameServer; i++) {
            String line = resolv.get(i);
            if (line.startsWith(key)) {
                String value = line.substring(key.length()).replaceFirst("^[ \t]+", Normal.EMPTY);
                if (!value.isEmpty() && value.charAt(0) != Symbol.C_HASH && value.charAt(0) != Symbol.C_SEMICOLON) {
                    String val = value.split("[ \t#;]", 2)[0];
                    servers.add(val);
                }
            }
        }
        return servers.toArray(Normal.EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "Host name: %s, Domain name: %s, DNS servers: %s, IPv4 Gateway: %s, IPv6 Gateway: %s",
                this.getHostName(),
                this.getDomainName(),
                Arrays.toString(this.getDnsServers()),
                this.getIpv4DefaultGateway(),
                this.getIpv6DefaultGateway());
    }

}
