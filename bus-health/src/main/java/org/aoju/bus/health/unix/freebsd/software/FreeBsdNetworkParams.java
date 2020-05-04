/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.unix.freebsd.software;

import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.AbstractNetworkParams;
import org.aoju.bus.health.unix.CLibrary;
import org.aoju.bus.health.unix.freebsd.FreeBsdLibc;
import org.aoju.bus.logger.Logger;

import static com.sun.jna.platform.unix.LibCAPI.HOST_NAME_MAX;

/**
 * FreeBsdNetworkParams class.
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@ThreadSafe
final class FreeBsdNetworkParams extends AbstractNetworkParams {

    private static final FreeBsdLibc LIBC = FreeBsdLibc.INSTANCE;

    @Override
    public String getDomainName() {
        CLibrary.Addrinfo hint = new CLibrary.Addrinfo();
        hint.ai_flags = CLibrary.AI_CANONNAME;
        String hostname = getHostName();

        PointerByReference ptr = new PointerByReference();
        int res = LIBC.getaddrinfo(hostname, null, hint, ptr);
        if (res > 0) {
            if (Logger.get().isError()) {
                Logger.warn("Failed getaddrinfo(): {}", LIBC.gai_strerror(res));
            }
            return Normal.EMPTY;
        }
        CLibrary.Addrinfo info = new CLibrary.Addrinfo(ptr.getValue());
        String canonname = info.ai_canonname.trim();
        LIBC.freeaddrinfo(ptr.getValue());
        return canonname;
    }

    @Override
    public String getHostName() {
        byte[] hostnameBuffer = new byte[HOST_NAME_MAX + 1];
        if (0 != LIBC.gethostname(hostnameBuffer, hostnameBuffer.length)) {
            return super.getHostName();
        }
        return Native.toString(hostnameBuffer);
    }

    @Override
    public String getIpv4DefaultGateway() {
        return searchGateway(Executor.runNative("route -4 get default"));
    }

    @Override
    public String getIpv6DefaultGateway() {
        return searchGateway(Executor.runNative("route -6 get default"));
    }

}
