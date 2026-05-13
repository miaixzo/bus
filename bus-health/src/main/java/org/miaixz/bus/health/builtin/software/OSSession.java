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
package org.miaixz.bus.health.builtin.software;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * This class encapsulates information about users who are currently logged in to an operating system.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public class OSSession {

    /**
     * The LOGIN_FORMAT constant.
     */
    private static final DateTimeFormatter LOGIN_FORMAT = DateTimeFormatter
            .ofPattern(Fields.NORM_DATETIME_MINUTE, Locale.ROOT);

    /**
     * The userName value.
     */
    private final String userName;

    /**
     * The terminalDevice value.
     */
    private final String terminalDevice;

    /**
     * The loginTime value.
     */
    private final long loginTime;

    /**
     * The host value.
     */
    private final String host;

    /**
     * Creates a new OSSession instance.
     *
     * @param userName       the user name
     * @param terminalDevice the terminal device
     * @param loginTime      the login time
     * @param host           the host
     */
    public OSSession(String userName, String terminalDevice, long loginTime, String host) {
        this.userName = userName;
        this.terminalDevice = terminalDevice;
        this.loginTime = loginTime;
        this.host = host;
    }

    /**
     * Gets the login name of the user
     *
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the terminal device (such as tty, pts, etc.) the user used to log in
     *
     * @return the terminalDevice
     */
    public String getTerminalDevice() {
        return terminalDevice;
    }

    /**
     * Gets the time the user logged in
     *
     * @return the loginTime, in milliseconds since the 1970 epoch
     */
    public long getLoginTime() {
        return loginTime;
    }

    /**
     * Gets the remote host from which the user logged in
     *
     * @return the host as either an IPv4 or IPv6 representation. If the host is unspecified, may also be an empty
     *         string, depending on the platform.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        String loginStr = loginTime == 0 ? "No login"
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(loginTime), ZoneId.systemDefault()).format(LOGIN_FORMAT);
        String hostStr = Normal.EMPTY;
        if (!host.isEmpty() && !host.equals("::") && !host.equals("0.0.0.0")) {
            hostStr = ", (" + host + Symbol.PARENTHESE_RIGHT;
        }
        return String.format(Locale.ROOT, "%s, %s, %s%s", userName, terminalDevice, loginStr, hostStr);
    }

}
