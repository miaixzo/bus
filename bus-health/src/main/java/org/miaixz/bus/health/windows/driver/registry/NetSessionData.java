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
package org.miaixz.bus.health.windows.driver.registry;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.OSSession;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Netapi32;
import com.sun.jna.platform.win32.Netapi32.SESSION_INFO_10;

/**
 * Utility to read process data from HKEY_PERFORMANCE_DATA information with backup from Performance Counters or WMI
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class NetSessionData {

    private static final Netapi32 NET = Netapi32.INSTANCE;

    public static List<OSSession> queryUserSessions() {
        List<OSSession> sessions = new ArrayList<>();
        try (ByRef.CloseablePointerByReference bufptr = new ByRef.CloseablePointerByReference();
                ByRef.CloseableIntByReference entriesread = new ByRef.CloseableIntByReference();
                ByRef.CloseableIntByReference totalentries = new ByRef.CloseableIntByReference()) {
            if (0 == NET.NetSessionEnum(
                    null,
                    null,
                    null,
                    10,
                    bufptr,
                    Netapi32.MAX_PREFERRED_LENGTH,
                    entriesread,
                    totalentries,
                    null)) {
                Pointer buf = bufptr.getValue();
                SESSION_INFO_10 si10 = new SESSION_INFO_10(buf);
                if (entriesread.getValue() > 0) {
                    SESSION_INFO_10[] sessionInfo = (SESSION_INFO_10[]) si10.toArray(entriesread.getValue());
                    for (SESSION_INFO_10 si : sessionInfo) {
                        // time field is connected seconds
                        long logonTime = System.currentTimeMillis() - (1000L * si.sesi10_time);
                        sessions.add(new OSSession(si.sesi10_username, "Network session", logonTime, si.sesi10_cname));
                    }
                }
                NET.NetApiBufferFree(buf);
            }
        }
        return sessions;
    }

}
