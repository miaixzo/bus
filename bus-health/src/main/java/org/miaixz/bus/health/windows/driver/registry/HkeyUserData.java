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
import org.miaixz.bus.health.builtin.software.OSSession;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Advapi32Util.Account;
import com.sun.jna.platform.win32.Advapi32Util.InfoKey;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;

/**
 * Utility to read session data from HKEY_USERS
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class HkeyUserData {

    private static final String PATH_DELIMITER = "\\";
    private static final String DEFAULT_DEVICE = "Console";
    private static final String VOLATILE_ENV_SUBKEY = "Volatile Environment";
    private static final String CLIENTNAME = "CLIENTNAME";
    private static final String SESSIONNAME = "SESSIONNAME";

    public static List<OSSession> queryUserSessions() {
        List<OSSession> sessions = new ArrayList<>();
        for (String sidKey : Advapi32Util.registryGetKeys(WinReg.HKEY_USERS)) {
            if (!sidKey.startsWith(".") && !sidKey.endsWith("_Classes")) {
                try {
                    Account a = Advapi32Util.getAccountBySid(sidKey);
                    String name = a.name;
                    String device = DEFAULT_DEVICE;
                    String host = a.domain; // temporary default
                    long loginTime = 0;
                    String keyPath = sidKey + PATH_DELIMITER + VOLATILE_ENV_SUBKEY;
                    if (Advapi32Util.registryKeyExists(WinReg.HKEY_USERS, keyPath)) {
                        HKEY hKey = Advapi32Util.registryGetKey(WinReg.HKEY_USERS, keyPath, WinNT.KEY_READ).getValue();
                        // InfoKey write time is user login time
                        InfoKey info = Advapi32Util.registryQueryInfoKey(hKey, 0);
                        loginTime = info.lpftLastWriteTime.toTime();
                        for (String subKey : Advapi32Util.registryGetKeys(hKey)) {
                            String subKeyPath = keyPath + PATH_DELIMITER + subKey;
                            // Check for session and client name
                            if (Advapi32Util.registryValueExists(WinReg.HKEY_USERS, subKeyPath, SESSIONNAME)) {
                                String session = Advapi32Util
                                        .registryGetStringValue(WinReg.HKEY_USERS, subKeyPath, SESSIONNAME);
                                if (!session.isEmpty()) {
                                    device = session;
                                }
                            }
                            if (Advapi32Util.registryValueExists(WinReg.HKEY_USERS, subKeyPath, CLIENTNAME)) {
                                String client = Advapi32Util
                                        .registryGetStringValue(WinReg.HKEY_USERS, subKeyPath, CLIENTNAME);
                                if (!client.isEmpty() && !DEFAULT_DEVICE.equals(client)) {
                                    host = client;
                                }
                            }
                        }
                        Advapi32Util.registryCloseKey(hKey);
                    }
                    sessions.add(new OSSession(name, device, loginTime, host));
                } catch (Win32Exception ex) {
                    Logger.warn("Error querying SID {} from registry: {}", sidKey, ex.getMessage());
                }
            }
        }
        return sessions;
    }

}
