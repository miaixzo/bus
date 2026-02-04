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
package org.miaixz.bus.health.unix.platform.aix.software;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.health.unix.jna.AixLibc;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.LibCAPI;

/**
 * AixNetworkParams class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class AixNetworkParams extends AbstractNetworkParams {

    private static final AixLibc LIBC = AixLibc.INSTANCE;

    private static String getDefaultGateway(String netstat) {
        /*-
        $ netstat -rnf inet
        Routing tables
        Destination        Gateway           Flags   Refs     Use  If   Exp  Groups
        *
        Route Tree for Protocol Family 2 (Internet):
        default            192.168.10.1      UG        9    873816 en0      -      -
        127/8              127.0.0.1         U         9    839480 lo0      -      -
        192.168.10.0       192.168.10.80     UHSb      0         0 en0      -      -   =>
        192.168.10/24      192.168.10.80     U         3    394820 en0      -      -
        192.168.10.80      127.0.0.1         UGHS      0         7 lo0      -      -
        192.168.10.255     192.168.10.80     UHSb      2      7466 en0      -      -
        */
        for (String line : Executor.runNative(netstat)) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length > 7 && "default".equals(split[0])) {
                return split[1];
            }
        }
        return Normal.UNKNOWN;
    }

    @Override
    public String getHostName() {
        byte[] hostnameBuffer = new byte[LibCAPI.HOST_NAME_MAX + 1];
        if (0 != LIBC.gethostname(hostnameBuffer, hostnameBuffer.length)) {
            return super.getHostName();
        }
        return Native.toString(hostnameBuffer);
    }

    @Override
    public String getIpv4DefaultGateway() {
        return getDefaultGateway("netstat -rnf inet");
    }

    @Override
    public String getIpv6DefaultGateway() {
        return getDefaultGateway("netstat -rnf inet6");
    }

}
