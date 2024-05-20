/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.mac.software;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.LibCAPI;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.health.unix.jna.CLibrary;
import org.miaixz.bus.logger.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

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
        } catch (UnknownHostException e) {
            Logger.error("Unknown host exception when getting address of local host: {}", e.getMessage());
            return Normal.EMPTY;
        }
        try (CLibrary.Addrinfo hint = new CLibrary.Addrinfo(); ByRef.CloseablePointerByReference ptr = new ByRef.CloseablePointerByReference()) {
            hint.ai_flags = CLibrary.AI_CANONNAME;
            int res = SYS.getaddrinfo(hostname, null, hint, ptr);
            if (res > 0) {
                if (Logger.isError()) {
                    Logger.error("Failed getaddrinfo(): {}", SYS.gai_strerror(res));
                }
                return Normal.EMPTY;
            }
            CLibrary.Addrinfo info = new CLibrary.Addrinfo(ptr.getValue()); // NOSONAR
            String canonname = info.ai_canonname.trim();
            SYS.freeaddrinfo(ptr.getValue());
            return canonname;
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
