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
package org.miaixz.bus.health.mac.software;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.health.unix.jna.CLibrary;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.LibCAPI;

/**
 * MacNetworkParams class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class MacNetworkParams extends AbstractNetworkParams {

    private static final SystemB SYS = SystemB.INSTANCE;

    private static final String IPV6_ROUTE_HEADER = "Internet6:";

    private static final String DEFAULT_GATEWAY = "default";

    @Override
    public String getDomainName() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
            if (hostname == null || hostname.isEmpty()) {
                Logger.debug("Could not determine hostname");
                return Normal.EMPTY;
            }
        } catch (UnknownHostException e) {
            Logger.debug("Unknown host exception when getting address of local host: {}", e.getMessage());
            return Normal.EMPTY;
        }
        try (CLibrary.Addrinfo hint = new CLibrary.Addrinfo();
                ByRef.CloseablePointerByReference ptr = new ByRef.CloseablePointerByReference()) {
            hint.ai_flags = CLibrary.AI_CANONNAME;
            int res = SYS.getaddrinfo(hostname, null, hint, ptr);
            if (res != 0) {
                if (Logger.isDebugEnabled()) {
                    Logger.debug("Failed getaddrinfo(): {}", SYS.gai_strerror(res));
                }
                return Normal.EMPTY;
            }
            try (CLibrary.Addrinfo info = new CLibrary.Addrinfo(ptr.getValue())) {
                String canonname = info.ai_canonname.trim();
                SYS.freeaddrinfo(ptr.getValue());
                return canonname;
            }
        }
    }

    @Override
    public String getHostName() {
        byte[] hostnameBuffer = new byte[LibCAPI.HOST_NAME_MAX + 1];
        if (0 != SYS.gethostname(hostnameBuffer, hostnameBuffer.length)) {
            return super.getHostName();
        }
        return Native.toString(hostnameBuffer);
    }

    @Override
    public String getIpv4DefaultGateway() {
        return searchGateway(Executor.runNative("route -n get default"));
    }

    @Override
    public String getIpv6DefaultGateway() {
        List<String> lines = Executor.runNative("netstat -nr");
        boolean v6Table = false;
        for (String line : lines) {
            if (v6Table && line.startsWith(DEFAULT_GATEWAY)) {
                String[] fields = Pattern.SPACES_PATTERN.split(line);
                if (fields.length > 2 && fields[2].contains("G")) {
                    return fields[1].split(Symbol.PERCENT)[0];
                }
            } else if (line.startsWith(IPV6_ROUTE_HEADER)) {
                v6Table = true;
            }
        }
        return Normal.EMPTY;
    }

}
