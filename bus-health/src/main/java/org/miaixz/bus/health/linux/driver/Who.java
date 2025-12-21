/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.linux.driver;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.OSSession;
import org.miaixz.bus.health.linux.jna.LinuxLibc;
import org.miaixz.bus.health.linux.jna.Systemd;
import org.miaixz.bus.health.unix.jna.CLibrary;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Utility to query logged in users.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Who {

    private static final LinuxLibc LIBC = LinuxLibc.INSTANCE;

    private static boolean useSystemd = Config.get(Config._LINUX_ALLOWSYSTEMD, true);

    private Who() {

    }

    /**
     * Query {@code getutxent} to get logged in users.
     *
     * @return A list of logged in user sessions
     */
    public static synchronized List<OSSession> queryUtxent() {
        // Try systemd first if available
        if (useSystemd) {
            try {
                List<OSSession> systemdSessions = querySystemdNative();
                if (!systemdSessions.isEmpty()) {
                    return systemdSessions;
                }
            } catch (Throwable t) {
                // systemd failed (probably UnsatisfiedLinkError), disable it for future calls
                useSystemd = false;
            }
        }

        List<OSSession> whoList = new ArrayList<>();
        LinuxLibc.LinuxUtmpx ut;
        // Rewind
        LIBC.setutxent();
        try {
            // Iterate
            while (Objects.nonNull(ut = LIBC.getutxent())) {
                if (ut.ut_type == CLibrary.USER_PROCESS || ut.ut_type == CLibrary.LOGIN_PROCESS) {
                    String user = Native.toString(ut.ut_user, Charset.defaultCharset());
                    String device = Native.toString(ut.ut_line, Charset.defaultCharset());
                    String host = Parsing.parseUtAddrV6toIP(ut.ut_addr_v6);
                    long loginTime = ut.ut_tv.tv_sec * 1000L + ut.ut_tv.tv_usec / 1000L;
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

        // If utmp returned no sessions, try systemd file fallback
        if (whoList.isEmpty()) {
            whoList = querySystemdFiles();
            if (whoList.isEmpty()) {
                // Final fallback to who command
                return org.miaixz.bus.health.unix.driver.Who.queryWho();
            }
        }
        return whoList;
    }

    /**
     * Query systemd sessions using native libsystemd calls.
     *
     * @return A list of logged in user sessions from systemd
     */
    private static List<OSSession> querySystemdNative() {
        List<OSSession> sessionList = new ArrayList<>();

        try (ByRef.CloseablePointerByReference sessionsPtr = new ByRef.CloseablePointerByReference()) {
            int count = Systemd.INSTANCE.sd_get_sessions(sessionsPtr);

            if (count > 0) {
                Pointer sessions = sessionsPtr.getValue();
                if (Pointer.nativeValue(sessions) != 0) {
                    try {
                        String[] sessionIds = sessions.getStringArray(0, count);

                        for (String sessionId : sessionIds) {
                            if (Objects.isNull(sessionId)) {
                                continue;
                            }
                            try {
                                // Get username
                                Pointer usernamePointer;
                                try (ByRef.CloseablePointerByReference usernamePtr = new ByRef.CloseablePointerByReference()) {
                                    if (Systemd.INSTANCE.sd_session_get_username(sessionId, usernamePtr) != 0
                                            || Pointer.nativeValue(usernamePtr.getValue()) == 0) {
                                        continue; // Skip this session
                                    }
                                    usernamePointer = usernamePtr.getValue();
                                }

                                String user;
                                try {
                                    user = usernamePointer.getString(0);
                                } finally {
                                    Native.free(Pointer.nativeValue(usernamePointer));
                                }

                                // Get start time
                                long loginTime;
                                try (ByRef.CloseableLongByReference startTimePtr = new ByRef.CloseableLongByReference()) {
                                    if (Systemd.INSTANCE.sd_session_get_start_time(sessionId, startTimePtr) != 0) {
                                        continue; // Skip this session
                                    }
                                    loginTime = startTimePtr.getValue() / 1000L; // Convert μs to ms
                                }

                                // Get TTY (optional)
                                String tty = sessionId; // Default to session ID
                                Pointer ttyPointer = null;
                                try (ByRef.CloseablePointerByReference ttyPtr = new ByRef.CloseablePointerByReference()) {
                                    if (Systemd.INSTANCE.sd_session_get_tty(sessionId, ttyPtr) == 0
                                            && Pointer.nativeValue(ttyPtr.getValue()) != 0) {
                                        ttyPointer = ttyPtr.getValue();
                                    }
                                }
                                if (Pointer.nativeValue(ttyPointer) != 0) {
                                    try {
                                        tty = ttyPointer.getString(0);
                                    } finally {
                                        Native.free(Pointer.nativeValue(ttyPointer));
                                    }
                                }

                                // Get remote host (optional)
                                String remoteHost = "";
                                Pointer remoteHostPointer = null;
                                try (ByRef.CloseablePointerByReference remoteHostPtr = new ByRef.CloseablePointerByReference()) {
                                    if (Systemd.INSTANCE.sd_session_get_remote_host(sessionId, remoteHostPtr) == 0
                                            && Pointer.nativeValue(remoteHostPtr.getValue()) != 0) {
                                        remoteHostPointer = remoteHostPtr.getValue();
                                    }
                                }
                                if (Pointer.nativeValue(remoteHostPointer) != 0) {
                                    try {
                                        remoteHost = remoteHostPointer.getString(0);
                                    } finally {
                                        Native.free(Pointer.nativeValue(remoteHostPointer));
                                    }
                                }

                                if (Builder.isSessionValid(user, tty, loginTime)) {
                                    sessionList.add(new OSSession(user, tty, loginTime, remoteHost));
                                }
                            } catch (Exception e) {
                                // Skip invalid session
                            }
                        }
                    } finally {
                        // Free all strings in the array first
                        Pointer[] ptrs = sessions.getPointerArray(0, count);
                        for (Pointer stringPtr : ptrs) {
                            if (Pointer.nativeValue(stringPtr) != 0) {
                                Native.free(Pointer.nativeValue(stringPtr));
                            }
                        }
                        // Then free the sessions array itself
                        Native.free(Pointer.nativeValue(sessions));
                    }
                }
            }
        } catch (Exception e) {
            // systemd calls failed, return empty list
        }

        return sessionList;
    }

    /**
     * Query systemd sessions from files as fallback when native calls fail.
     *
     * @return A list of logged in user sessions from systemd
     */
    private static List<OSSession> querySystemdFiles() {
        List<OSSession> sessionList = new ArrayList<>();

        // Directly iterate /run/systemd/sessions/ directory
        File sessionsDir = new File("/run/systemd/sessions");
        if (sessionsDir.exists() && sessionsDir.isDirectory()) {
            File[] sessionFiles = sessionsDir
                    .listFiles(file -> Pattern.compile("\\d+").matcher(file.getName()).matches());

            if (Objects.nonNull(sessionFiles)) {
                for (File sessionFile : sessionFiles) {
                    try {
                        Map<String, String> sessionMap = Builder.getKeyValueMapFromFile(sessionFile.getPath(), "=");

                        String user = sessionMap.get("USER");
                        if (Objects.nonNull(user) && !user.isEmpty()) {
                            String tty = sessionMap.getOrDefault("TTY", sessionFile.getName());
                            String remoteHost = sessionMap.getOrDefault("REMOTE_HOST", "");

                            // Try to get login time from REALTIME field or file modification time
                            long loginTime = 0L;
                            String realtime = sessionMap.get("REALTIME");
                            if (Objects.nonNull(realtime)) {
                                loginTime = Parsing.parseLongOrDefault(realtime, 0L) / 1000L; // Convert µs to ms
                            }
                            if (loginTime == 0L) {
                                loginTime = sessionFile.lastModified(); // Fallback to file modification time
                            }

                            if (Builder.isSessionValid(user, tty, loginTime)) {
                                sessionList.add(new OSSession(user, tty, loginTime, remoteHost));
                            }
                        }
                    } catch (Exception e) {
                        // Skip invalid session files
                    }
                }
            }
        }

        return sessionList;
    }
}
