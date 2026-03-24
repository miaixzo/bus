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
package org.miaixz.bus.health.unix.platform.solaris.driver;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.builtin.software.OSSession;
import org.miaixz.bus.health.unix.jna.CLibrary;
import org.miaixz.bus.health.unix.jna.SolarisLibc;

import com.sun.jna.Native;

/**
 * Utility to query logged in users.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Who {

    private static final SolarisLibc LIBC = SolarisLibc.INSTANCE;

    /**
     * Query {@code getutxent} to get logged in users.
     *
     * @return A list of logged in user sessions
     */
    public static synchronized List<OSSession> queryUtxent() {
        List<OSSession> whoList = new ArrayList<>();
        SolarisLibc.SolarisUtmpx ut;
        // Rewind
        LIBC.setutxent();
        try {
            // Iterate
            while ((ut = LIBC.getutxent()) != null) {
                if (ut.ut_type == CLibrary.USER_PROCESS || ut.ut_type == CLibrary.LOGIN_PROCESS) {
                    String user = Native.toString(ut.ut_user, Charset.US_ASCII);
                    String device = Native.toString(ut.ut_line, Charset.US_ASCII);
                    String host = Native.toString(ut.ut_host, Charset.US_ASCII);
                    long loginTime = ut.ut_tv.tv_sec.longValue() * 1000L + ut.ut_tv.tv_usec.longValue() / 1000L;
                    // Sanity check. If errors, default to who command line
                    if (!Builder.isSessionValid(user, device, loginTime)) {
                        return org.miaixz.bus.health.unix.driver.Who.queryWho();
                    }
                    whoList.add(new OSSession(user, device, loginTime, host));
                }
            }
        } finally {
            // Close
            LIBC.endutxent();
        }
        return whoList;
    }

}
