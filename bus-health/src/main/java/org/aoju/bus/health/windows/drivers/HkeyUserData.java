/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.health.windows.drivers;

import com.sun.jna.platform.win32.Advapi32Util.Account;
import com.sun.jna.platform.win32.Advapi32Util.InfoKey;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.builtin.software.OSSession;
import org.aoju.bus.health.windows.Advapi32Kit;
import org.aoju.bus.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to read session data from HKEY_USERS
 *
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
 */
@ThreadSafe
public final class HkeyUserData {

    private static final String DEFAULT_DEVICE = "Console";
    private static final String VOLATILE_ENV_SUBKEY = "Volatile Environment";
    private static final String CLIENTNAME = "CLIENTNAME";
    private static final String SESSIONNAME = "SESSIONNAME";

    private HkeyUserData() {
    }

    public static List<OSSession> queryUserSessions() {
        List<OSSession> sessions = new ArrayList<>();
        for (String sidKey : Advapi32Kit.registryGetKeys(WinReg.HKEY_USERS)) {
            if (!sidKey.startsWith(".") && !sidKey.endsWith("_Classes")) {
                try {
                    Account a = Advapi32Kit.getAccountBySid(sidKey);
                    String name = a.name;
                    String device = DEFAULT_DEVICE;
                    String host = a.domain; // temporary default
                    long loginTime = 0;
                    String keyPath = sidKey + "\\" + VOLATILE_ENV_SUBKEY;
                    if (Advapi32Kit.registryKeyExists(WinReg.HKEY_USERS, keyPath)) {
                        HKEY hKey = Advapi32Kit.registryGetKey(WinReg.HKEY_USERS, keyPath, WinNT.KEY_READ).getValue();
                        // InfoKey write time is user login time
                        InfoKey info = Advapi32Kit.registryQueryInfoKey(hKey, 0);
                        loginTime = info.lpftLastWriteTime.toTime();
                        for (String subKey : Advapi32Kit.registryGetKeys(hKey)) {
                            String subKeyPath = keyPath + "\\" + subKey;
                            // Check for session and client name
                            if (Advapi32Kit.registryValueExists(WinReg.HKEY_USERS, subKeyPath, SESSIONNAME)) {
                                String session = Advapi32Kit.registryGetStringValue(WinReg.HKEY_USERS, subKeyPath,
                                        SESSIONNAME);
                                if (!session.isEmpty()) {
                                    device = session;
                                }
                            }
                            if (Advapi32Kit.registryValueExists(WinReg.HKEY_USERS, subKeyPath, CLIENTNAME)) {
                                String client = Advapi32Kit.registryGetStringValue(WinReg.HKEY_USERS, subKeyPath,
                                        CLIENTNAME);
                                if (!client.isEmpty() && !DEFAULT_DEVICE.equals(client)) {
                                    host = client;
                                }
                            }
                        }
                        Advapi32Kit.registryCloseKey(hKey);
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
