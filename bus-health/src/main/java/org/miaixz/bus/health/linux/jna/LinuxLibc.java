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
package org.miaixz.bus.health.linux.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.linux.LibC;

import org.miaixz.bus.health.unix.shared.jna.CLibrary;

/**
 * Linux C Library. This class should be considered non-API as it may be removed if/when its code is incorporated into
 * the JNA project.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface LinuxLibc extends LibC, CLibrary {

    /**
     * The INSTANCE value.
     */
    LinuxLibc INSTANCE = Native.load("c", LinuxLibc.class);

    /**
     * SYS_gettid Defined in one of: arch/arm64/include/asm/unistd32.h, 224 arch/x86/include/uapi/asm/unistd_32.h, 224
     * arch/x86/include/uapi/asm/unistd_64.h, 186 include/uapi/asm-generic/unistd.h, 178
     */
    NativeLong SYS_GETTID = new NativeLong(Platform.isIntel() ? (Platform.is64Bit() ? 186 : 224)
            : ((Platform.isARM() && Platform.is64Bit()) ? 224 : 178));

    /**
     * Reads a line from the current file position in the utmp file. It returns a pointer to a structure containing the
     * fields of the line.
     * <p>
     * Not thread safe
     *
     * @return a {@link LinuxUtmpx} on success, and NULL on failure (which includes the "record not found" case)
     */
    LinuxUtmpx getutxent();

    /**
     * Returns the caller's thread ID (TID). In a single-threaded process, the thread ID is equal to the process ID. In
     * a multithreaded process, all threads have the same PID, but each one has a unique TID.
     *
     * @return the thread ID of the calling thread.
     */
    int gettid();

    /**
     * syscall() performs the system call whose assembly language interface has the specified number with the specified
     * arguments.
     *
     * @param number sys call number
     * @param args   sys call arguments
     * @return The return value is defined by the system call being invoked. In general, a 0 return value indicates
     *         success. A -1 return value indicates an error, and an error code is stored in errno.
     */
    NativeLong syscall(NativeLong number, Object... args);

    /**
     * Returns resource usage for the specified target.
     *
     * @param who    the usage target, such as {@link #RUSAGE_SELF}
     * @param rusage the resource usage structure to populate
     * @return 0 on success; -1 on failure
     */
    int getrusage(int who, Rusage rusage);

    /**
     * JNA wrapper for the rusage structure.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "ru_utime_sec", "ru_utime_usec", "ru_stime_sec", "ru_stime_usec", "ru_maxrss", "ru_ixrss", "ru_idrss",
            "ru_isrss", "ru_minflt", "ru_majflt", "ru_nswap", "ru_inblock", "ru_oublock", "ru_msgsnd", "ru_msgrcv",
            "ru_nsignals", "ru_nvcsw", "ru_nivcsw" })
    class Rusage extends Structure {

        /**
         * User time seconds.
         */
        public NativeLong ru_utime_sec;

        /**
         * User time microseconds.
         */
        public NativeLong ru_utime_usec;

        /**
         * System time seconds.
         */
        public NativeLong ru_stime_sec;

        /**
         * System time microseconds.
         */
        public NativeLong ru_stime_usec;

        /**
         * Maximum resident set size.
         */
        public NativeLong ru_maxrss;

        /**
         * Shared memory size.
         */
        public NativeLong ru_ixrss;

        /**
         * Unshared data size.
         */
        public NativeLong ru_idrss;

        /**
         * Unshared stack size.
         */
        public NativeLong ru_isrss;

        /**
         * Minor page faults.
         */
        public NativeLong ru_minflt;

        /**
         * Major page faults.
         */
        public NativeLong ru_majflt;

        /**
         * Swap count.
         */
        public NativeLong ru_nswap;

        /**
         * Block input operations.
         */
        public NativeLong ru_inblock;

        /**
         * Block output operations.
         */
        public NativeLong ru_oublock;

        /**
         * Sent messages.
         */
        public NativeLong ru_msgsnd;

        /**
         * Received messages.
         */
        public NativeLong ru_msgrcv;

        /**
         * Received signals.
         */
        public NativeLong ru_nsignals;

        /**
         * Voluntary context switches.
         */
        public NativeLong ru_nvcsw;

        /**
         * Involuntary context switches.
         */
        public NativeLong ru_nivcsw;

    }

    /**
     * JNA wrapper for the utmpx structure.
     * <p>
     * This class maps to the native Linux utmpx structure: {@code
     * struct utmpx {
     *     short ut_type;
     *     pid_t ut_pid;
     *     char ut_line[UT_LINESIZE];
     *     char ut_id[4];
     *     char ut_user[UT_NAMESIZE];
     *     char ut_host[UT_HOSTSIZE];
     *     struct exit_status ut_exit; int ut_session; struct timeval ut_tv; int32_t ut_addr_v6[4]; char reserved[20];
     * }; }
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "ut_type", "ut_pid", "ut_line", "ut_id", "ut_user", "ut_host", "ut_exit", "ut_session", "ut_tv",
            "ut_addr_v6", "reserved" })
    class LinuxUtmpx extends Structure {

        /**
         * Constructs a new {@code LinuxUtmpx} instance.
         */
        LinuxUtmpx() {
            // No initialization required.
        }

        /**
         * The ut_type value.
         */
        public short ut_type; // Type of login.
        /**
         * The ut_pid value.
         */
        public int ut_pid; // Process ID of login process.
        /**
         * The ut_line value.
         */
        public byte[] ut_line = new byte[UT_LINESIZE]; // Devicename.
        /**
         * The ut_id value.
         */
        public byte[] ut_id = new byte[4]; // Inittab ID.
        /**
         * The ut_user value.
         */
        public byte[] ut_user = new byte[UT_NAMESIZE]; // Username.
        /**
         * The ut_host value.
         */
        public byte[] ut_host = new byte[UT_HOSTSIZE]; // Hostname for remote login.
        /**
         * The ut_exit value.
         */
        public Exit_status ut_exit; // Exit status of a process marked as DEAD_PROCESS.
        /**
         * The ut_session value.
         */
        public int ut_session; // Session ID, used for windowing.
        /**
         * The ut_tv value.
         */
        public Ut_Tv ut_tv; // Time entry was made.
        /**
         * The ut_addr_v6 value.
         */
        public int[] ut_addr_v6 = new int[4]; // Internet address of remote host; IPv4 address uses just ut_addr_v6[0]
        /**
         * The reserved value.
         */
        public byte[] reserved = new byte[20]; // Reserved for future use.

    }

    /**
     * JNA wrapper for the exit_status structure.
     * <p>
     * This class maps to the native Linux exit_status structure which is part of utmpx.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "e_termination", "e_exit" })
    class Exit_status extends Structure {

        /**
         * Constructs a new {@code Exit_status} instance.
         */
        Exit_status() {
            // No initialization required.
        }

        /**
         * The e_termination value.
         */
        public short e_termination; // Process termination status
        /**
         * The e_exit value.
         */
        public short e_exit; // Process exit status

    }

    /**
     * JNA wrapper for the timeval structure (32-bit version).
     * <p>
     * This class maps to the native Linux timeval structure: {@code struct timeval { int tv_sec; int tv_usec; }; }
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "tv_sec", "tv_usec" })
    class Ut_Tv extends Structure {

        /**
         * Constructs a new {@code Ut_Tv} instance.
         */
        Ut_Tv() {
            // No initialization required.
        }

        /**
         * The tv_sec value.
         */
        public int tv_sec; // seconds
        /**
         * The tv_usec value.
         */
        public int tv_usec; // microseconds

    }

}
