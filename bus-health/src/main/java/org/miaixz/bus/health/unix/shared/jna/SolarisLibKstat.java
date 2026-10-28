/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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
package org.miaixz.bus.health.unix.shared.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;

/**
 * Declares the Solaris {@code libkstat} functions that accept {@code kstat_ctl_t *} with that argument mapped as an
 * opaque pointer. JNA's {@code KstatCtl} structure has an incorrect inline chain layout and must not be passed to these
 * functions because doing so reads and writes beyond the native allocation.
 *
 * @author Kimi Liu
 */
public interface SolarisLibKstat extends Library {

    /** Singleton instance of the Solaris kstat library. */
    SolarisLibKstat INSTANCE = Native.load("kstat", SolarisLibKstat.class);

    /**
     * Opens the kstat control structure.
     *
     * @return an opaque control pointer, or {@code null} on failure
     */
    Pointer kstat_open();

    /**
     * Closes a kstat control structure.
     *
     * @param kc the opaque control pointer
     * @return {@code 0} on success or {@code -1} on failure
     */
    int kstat_close(Pointer kc);

    /**
     * Synchronizes the user's kstat chain with the kernel.
     *
     * @param kc the opaque control pointer
     * @return the new chain identifier, {@code 0} when unchanged, or {@code -1} on failure
     */
    int kstat_chain_update(Pointer kc);

    /**
     * Looks up a kstat record.
     *
     * @param kc          the opaque control pointer
     * @param ks_module   the module name, or {@code null} to ignore it
     * @param ks_instance the instance number, or {@code -1} to ignore it
     * @param ks_name     the record name, or {@code null} to ignore it
     * @return the matching record, or {@code null}
     */
    Kstat kstat_lookup(Pointer kc, String ks_module, int ks_instance, String ks_name);

    /**
     * Reads the selected kstat record.
     *
     * @param kc  the opaque control pointer
     * @param ksp the kstat record
     * @param p   an optional destination pointer
     * @return the current chain identifier, or {@code -1} on failure
     */
    int kstat_read(Pointer kc, Kstat ksp, Pointer p);

    /**
     * Looks up a named data value in a kstat record.
     *
     * @param ksp  the kstat record
     * @param name the data name
     * @return the requested data pointer, or {@code null}
     */
    Pointer kstat_data_lookup(Kstat ksp, String name);

}
