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
package org.miaixz.bus.health.linux.jna;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * JNA bindings for libsystemd. This class should be considered non-API as it may be removed if/when its code is
 * incorporated into the JNA project.
 */
@ThreadSafe
public interface Systemd extends Library {

    Systemd INSTANCE = Native.load("systemd", Systemd.class);

    /**
     * Get start time of session
     *
     * @param session Session ID or null for current session
     * @param usec    Pointer to store microseconds since epoch
     * @return 0 on success, negative errno on failure
     */
    int sd_session_get_start_time(String session, LongByReference usec);

    /**
     * Get username of session
     *
     * @param session  Session ID or null for current session
     * @param username Pointer to store username string (must be freed)
     * @return 0 on success, negative errno on failure
     */
    int sd_session_get_username(String session, PointerByReference username);

    /**
     * Get TTY of session
     *
     * @param session Session ID or null for current session
     * @param tty     Pointer to store TTY string (must be freed)
     * @return 0 on success, negative errno on failure
     */
    int sd_session_get_tty(String session, PointerByReference tty);

    /**
     * Get remote host of session
     *
     * @param session     Session ID or null for current session
     * @param remote_host Pointer to store remote host string (must be freed)
     * @return 0 on success, negative errno on failure
     */
    int sd_session_get_remote_host(String session, PointerByReference remote_host);

    /**
     * Enumerate sessions
     *
     * @param sessions Pointer to store array of session IDs (must be freed)
     * @return Number of sessions on success, negative errno on failure
     */
    int sd_get_sessions(PointerByReference sessions);

}
