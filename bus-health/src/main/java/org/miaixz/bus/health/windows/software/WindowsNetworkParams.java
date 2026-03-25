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
package org.miaixz.bus.health.windows.software;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.IPHlpAPI.FIXED_INFO;
import com.sun.jna.platform.win32.IPHlpAPI.IP_ADDR_STRING;

/**
 * WindowsNetworkParams class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class WindowsNetworkParams extends AbstractNetworkParams {

    private static final int COMPUTER_NAME_DNS_DOMAIN_FULLY_QUALIFIED = 3;

    private static String parseIpv4Route() {
        List<String> lines = Executor.runNative("route print -4 0.0.0.0");
        for (String line : lines) {
            String[] fields = Pattern.SPACES_PATTERN.split(line.trim());
            if (fields.length > 2 && "0.0.0.0".equals(fields[0])) {
                return fields[2];
            }
        }
        return Normal.EMPTY;
    }

    private static String parseIpv6Route() {
        List<String> lines = Executor.runNative("route print -6 ::/0");
        for (String line : lines) {
            String[] fields = Pattern.SPACES_PATTERN.split(line.trim());
            if (fields.length > 3 && "::/0".equals(fields[2])) {
                return fields[3];
            }
        }
        return Normal.EMPTY;
    }

    @Override
    public String getHostName() {
        try {
            return Kernel32Util.getComputerName();
        } catch (Win32Exception e) {
            return super.getHostName();
        }
    }

    @Override
    public String getIpv4DefaultGateway() {
        return parseIpv4Route();
    }

    @Override
    public String getIpv6DefaultGateway() {
        return parseIpv6Route();
    }

    @Override
    public String getDomainName() {
        char[] buffer = new char[256];
        try (ByRef.CloseableIntByReference bufferSize = new ByRef.CloseableIntByReference(buffer.length)) {
            if (!Kernel32.INSTANCE.GetComputerNameEx(COMPUTER_NAME_DNS_DOMAIN_FULLY_QUALIFIED, buffer, bufferSize)) {
                Logger.error("Failed to get dns domain name. Error code: {}", Kernel32.INSTANCE.GetLastError());
                return Normal.EMPTY;
            }
        }
        return Native.toString(buffer);
    }

    @Override
    public String[] getDnsServers() {
        try (ByRef.CloseableIntByReference bufferSize = new ByRef.CloseableIntByReference()) {
            int ret = IPHlpAPI.INSTANCE.GetNetworkParams(null, bufferSize);
            if (ret != WinError.ERROR_BUFFER_OVERFLOW) {
                Logger.error("Failed to get network parameters buffer size. Error code: {}", ret);
                return new String[0];
            }

            try (Memory buffer = new Memory(bufferSize.getValue())) {
                ret = IPHlpAPI.INSTANCE.GetNetworkParams(buffer, bufferSize);
                if (ret != 0) {
                    Logger.error("Failed to get network parameters. Error code: {}", ret);
                    return new String[0];
                }
                FIXED_INFO fixedInfo = new FIXED_INFO(buffer);

                List<String> list = new ArrayList<>();
                IP_ADDR_STRING dns = fixedInfo.DnsServerList;
                while (dns != null) {
                    // a char array of size 16.
                    // This array holds an IPv4 address in dotted decimal notation.
                    String addr = Native.toString(dns.IpAddress.String, Charset.US_ASCII);
                    int nullPos = addr.indexOf(0);
                    if (nullPos != -1) {
                        addr = addr.substring(0, nullPos);
                    }
                    list.add(addr);
                    dns = dns.Next;
                }
                return list.toArray(new String[0]);
            }
        }
    }

}
