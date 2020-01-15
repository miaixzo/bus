/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.health.software;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.logger.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common NetworkParams implementation.
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public abstract class AbstractNetwork implements NetworkParams {

    private static final String NAMESERVER = "nameserver";

    /**
     * Convenience method to parse the output of the `route` command. While the
     * command arguments vary between OS's the output is consistently parsable.
     *
     * @param lines output of OS-specific route command
     * @return default gateway
     */
    protected static String searchGateway(List<String> lines) {
        for (String line : lines) {
            String leftTrimmed = line.replaceFirst("^\\s+", "");
            if (leftTrimmed.startsWith("gateway:")) {
                String[] split = Builder.whitespaces.split(leftTrimmed);
                if (split.length < 2) {
                    return "";
                }
                return split[1].split(Symbol.PERCENT)[0];
            }
        }
        return "";
    }

    @Override
    public String getDomainName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            Logger.error("Unknown host exception when getting address of local host: {}", e);
            return "";
        }
    }

    @Override
    public String getHostName() {
        try {
            String hn = InetAddress.getLocalHost().getHostName();
            int dot = hn.indexOf(Symbol.C_DOT);
            if (dot == -1) {
                return hn;
            } else {
                return hn.substring(0, dot);
            }
        } catch (UnknownHostException e) {
            Logger.error("Unknown host exception when getting address of local host: {}", e);
            return "";
        }
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
                String value = line.substring(key.length()).replaceFirst("^[ \t]+", "");
                if (value.length() != 0 && value.charAt(0) != Symbol.C_SHAPE && value.charAt(0) != Symbol.C_SEMICOLON) {
                    String val = value.split("[ \t#;]", 2)[0];
                    servers.add(val);
                }
            }
        }
        return servers.toArray(Normal.EMPTY_STRING_ARRAY);
    }

    @Override
    public String toString() {
        return String.format("Host name: %s, Domain name: %s, DNS servers: %s, IPv4 Gateway: %s, IPv6 Gateway: %s",
                this.getHostName(), this.getDomainName(), Arrays.toString(this.getDnsServers()),
                this.getIpv4DefaultGateway(), this.getIpv6DefaultGateway());

    }

}
