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
