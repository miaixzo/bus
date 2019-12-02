/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.software.unix.freebsd;

import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.common.unix.CLibrary;
import org.aoju.bus.health.common.unix.freebsd.FreeBsdLibc;
import org.aoju.bus.health.software.AbstractNetwork;
import org.aoju.bus.logger.Logger;

import static com.sun.jna.platform.unix.LibCAPI.HOST_NAME_MAX;

/**
 * <p>
 * FreeBsdNetworkParams class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public class FreeBsdNetwork extends AbstractNetwork {

    private static final FreeBsdLibc LIBC = FreeBsdLibc.INSTANCE;

    @Override
    public String getDomainName() {
        FreeBsdLibc.Addrinfo hint = new FreeBsdLibc.Addrinfo();
        hint.ai_flags = CLibrary.AI_CANONNAME;
        String hostname = getHostName();

        PointerByReference ptr = new PointerByReference();
        int res = LIBC.getaddrinfo(hostname, null, hint, ptr);
        if (res > 0) {
            Logger.warn("Failed getaddrinfo(): {}", LIBC.gai_strerror(res));
            return "";
        }
        FreeBsdLibc.Addrinfo info = new FreeBsdLibc.Addrinfo(ptr.getValue());
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
        return searchGateway(Command.runNative("route -4 get default"));
    }

    @Override
    public String getIpv6DefaultGateway() {
        return searchGateway(Command.runNative("route -6 get default"));
    }
}
