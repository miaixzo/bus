/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.linux.software;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.health.linux.jna.LinuxLibc;
import org.miaixz.bus.health.unix.jna.CLibrary;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import com.sun.jna.platform.unix.LibCAPI;

/**
 * LinuxNetworkParams class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class LinuxNetworkParams extends AbstractNetworkParams {

    private static final LinuxLibc LIBC = LinuxLibc.INSTANCE;

    private static final String IPV4_DEFAULT_DEST = "0.0.0.0"; // NOSONAR
    private static final String IPV6_DEFAULT_DEST = "::/0";

    @Override
    public String getDomainName() {
        try (CLibrary.Addrinfo hint = new CLibrary.Addrinfo()) {
            hint.ai_flags = CLibrary.AI_CANONNAME;
            String hostname;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                Logger.warn("Unknown host exception when getting address of local host: {}", e.getMessage());
                return Normal.EMPTY;
            }
            try (ByRef.CloseablePointerByReference ptr = new ByRef.CloseablePointerByReference()) {
                int res = LIBC.getaddrinfo(hostname, null, hint, ptr);
                if (res > 0) {
                    if (Logger.isErrorEnabled()) {
                        Logger.error("Failed getaddrinfo(): {}", LIBC.gai_strerror(res));
                    }
                    return Normal.EMPTY;
                }
                try (CLibrary.Addrinfo info = new CLibrary.Addrinfo(ptr.getValue())) {
                    return info.ai_canonname == null ? hostname : info.ai_canonname.trim();
                }
            }
        }
    }

    @Override
    public String getHostName() {
        byte[] hostnameBuffer = new byte[LibCAPI.HOST_NAME_MAX + 1];
        if (0 != LibC.INSTANCE.gethostname(hostnameBuffer, hostnameBuffer.length)) {
            return super.getHostName();
        }
        return Native.toString(hostnameBuffer);
    }

    @Override
    public String getIpv4DefaultGateway() {
        List<String> routes = Executor.runNative("route -A inet -n");
        if (routes.size() <= 2) {
            return Normal.EMPTY;
        }

        String gateway = Normal.EMPTY;
        int minMetric = Integer.MAX_VALUE;

        for (int i = 2; i < routes.size(); i++) {
            String[] fields = Pattern.SPACES_PATTERN.split(routes.get(i));
            if (fields.length > 4 && fields[0].equals(IPV4_DEFAULT_DEST)) {
                boolean isGateway = fields[3].indexOf('G') != -1;
                int metric = Parsing.parseIntOrDefault(fields[4], Integer.MAX_VALUE);
                if (isGateway && metric < minMetric) {
                    minMetric = metric;
                    gateway = fields[1];
                }
            }
        }
        return gateway;
    }

    @Override
    public String getIpv6DefaultGateway() {
        List<String> routes = Executor.runNative("route -A inet6 -n");
        if (routes.size() <= 2) {
            return Normal.EMPTY;
        }

        String gateway = Normal.EMPTY;
        int minMetric = Integer.MAX_VALUE;

        for (int i = 2; i < routes.size(); i++) {
            String[] fields = Pattern.SPACES_PATTERN.split(routes.get(i));
            if (fields.length > 3 && fields[0].equals(IPV6_DEFAULT_DEST)) {
                boolean isGateway = fields[2].indexOf('G') != -1;
                int metric = Parsing.parseIntOrDefault(fields[3], Integer.MAX_VALUE);
                if (isGateway && metric < minMetric) {
                    minMetric = metric;
                    gateway = fields[1];
                }
            }
        }
        return gateway;
    }

}
