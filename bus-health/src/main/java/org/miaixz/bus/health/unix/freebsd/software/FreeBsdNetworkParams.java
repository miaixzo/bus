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
package org.miaixz.bus.health.unix.freebsd.software;

import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.LibCAPI;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.NetworkParams;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.health.unix.shared.driver.BsdRouteDump;
import org.miaixz.bus.health.unix.shared.driver.NetstatRoute;
import org.miaixz.bus.health.unix.shared.driver.RouteTableDump;
import org.miaixz.bus.health.unix.shared.jna.CLibrary;
import org.miaixz.bus.health.unix.shared.jna.FreeBsdLibc;
import org.miaixz.bus.logger.Logger;

/**
 * FreeBsdNetworkParams class.
 *
 * @author Kimi Liu
 */
@ThreadSafe
final class FreeBsdNetworkParams extends AbstractNetworkParams {

    /**
     * The LIBC constant.
     */
    private static final FreeBsdLibc LIBC = FreeBsdLibc.INSTANCE;

    /**
     * Returns the domain name.
     *
     * @return the get domain name result
     */
    @Override
    public String getDomainName() {
        try (CLibrary.Addrinfo hint = new CLibrary.Addrinfo()) {
            hint.ai_flags = CLibrary.AI_CANONNAME;
            String hostname = getHostName();

            try (ByRef.CloseablePointerByReference ptr = new ByRef.CloseablePointerByReference()) {
                int res = LIBC.getaddrinfo(hostname, null, hint, ptr);
                if (res > 0) {
                    if (Logger.isErrorEnabled()) {
                        Logger.warn(false, "Health", "Failed getaddrinfo(): {}", LIBC.gai_strerror(res));
                    }
                    return Normal.EMPTY;
                }
                try (CLibrary.Addrinfo info = new CLibrary.Addrinfo(ptr.getValue())) {
                    String canonname = info.ai_canonname.trim();
                    LIBC.freeaddrinfo(ptr.getValue());
                    return canonname;
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
        byte[] hostnameBuffer = new byte[LibCAPI.HOST_NAME_MAX + 1];
        if (0 != LIBC.gethostname(hostnameBuffer, hostnameBuffer.length)) {
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
        return searchGateway(Executor.runNative("route -4 get default"));
    }

    /**
     * Returns the ipv6 default gateway.
     *
     * @return the get ipv6 default gateway result
     */
    @Override
    public String getIpv6DefaultGateway() {
        return searchGateway(Executor.runNative("route -6 get default"));
    }

    /**
     * Returns the routing table.
     *
     * @return the routing table
     */
    @Override
    public List<NetworkParams.IPRoute> getRoutes() {
        byte[] dump = queryRouteDump();
        if (dump.length > 0) {
            List<NetworkParams.IPRoute> routes = RouteTableDump
                    .parse(dump, RouteTableDump.Layout.FREEBSD, queryInterfaceNameByIndex());
            if (!routes.isEmpty()) {
                return routes;
            }
        }
        return NetstatRoute
                .queryRoutes("netstat -rn -f inet", "netstat -rn -f inet6", Normal._3, queryInterfaceIndexByName());
    }

    /**
     * Fetches the kernel routing table dump.
     *
     * @return the routing table dump, or an empty array
     */
    protected byte[] queryRouteDump() {
        return BsdRouteDump.queryRouteDump(LIBC);
    }

}
