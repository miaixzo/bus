/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy;

import java.net.InetAddress;

import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReverseDNS {

    static final boolean DISABLED = isFalse(ReverseDNS.class.getName());

    private static boolean isFalse(String name) {
        try {
            String s = System.getProperty(name);
            return ((s != null) && s.equalsIgnoreCase("false"));
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        return false;
    }

    public static String hostNameOf(InetAddress inetAddress) {
        if (DISABLED)
            return inetAddress.getHostAddress();

        if (!Logger.isDebugEnabled())
            return inetAddress.getHostName();

        String hostAddress = inetAddress.getHostAddress();
        Logger.debug("rDNS {} -> ...", hostAddress);
        long start = System.nanoTime();
        String hostName = inetAddress.getHostName();
        long end = System.nanoTime();
        Logger.debug("rDNS {} -> {} in {} ms", hostAddress, hostName, (end - start) / 1000);
        return hostName;
    }

}
