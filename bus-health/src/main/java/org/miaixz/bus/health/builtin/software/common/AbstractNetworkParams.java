/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.builtin.software.NetworkParams;

/**
 * Common NetworkParams implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public abstract class AbstractNetworkParams implements NetworkParams {

    private static final String NAMESERVER = "nameserver";

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

    @Override
    public String getDomainName() {
        InetAddress localHost;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            localHost = InetAddress.getLoopbackAddress();
        }
        return localHost.getCanonicalHostName();
    }

    @Override
    public String getHostName() {
        InetAddress localHost;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            localHost = InetAddress.getLoopbackAddress();
        }
        String hn = localHost.getHostName();
        int dot = hn.indexOf('.');
        if (dot == -1) {
            return hn;
        }
        return hn.substring(0, dot);
    }

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
                if (value.length() != 0 && value.charAt(0) != Symbol.C_HASH && value.charAt(0) != Symbol.C_SEMICOLON) {
                    String val = value.split("[ \t#;]", 2)[0];
                    servers.add(val);
                }
            }
        }
        return servers.toArray(new String[0]);
    }

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
