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

import java.nio.ByteBuffer;

import com.sun.jna.Native;

import org.miaixz.bus.health.Parsing;

/**
 * C library for AIX. This class should be considered non-API as it may be removed if/when its code is incorporated into
 * the JNA project.
 *
 * @author Kimi Liu
 */
public interface AixLibc extends CLibrary {

    /**
     * Singleton instance of the AixLibc library.
     */
    AixLibc INSTANCE = Native.load("c", AixLibc.class);

    /**
     * Size of the {@code pr_clname} field in {@code AixLwpsInfo}.
     */
    int PRCLSZ = 8;

    /**
     * Size of the {@code pr_fname} field in {@code AixPsInfo}.
     */
    int PRFNSZ = 16;

    /**
     * Size of the {@code pr_psargs} field in {@code AixPsInfo}.
     */
    int PRARGSZ = 80;

    /**
     * Returns the caller's kernel thread ID.
     *
     * @return the caller's kernel thread ID.
     */
    int thread_self();

    /**
     * Represents the process information structure on AIX.
     *
     * @author Kimi Liu
     */
    class AixPsInfo {

        /**
         * Process flags from proc struct p_flag.
         */
        public int pr_flag;

        /**
         * Process flags from proc struct p_flag2.
         */
        public int pr_flag2;

        /**
         * Number of threads in process.
         */
        public int pr_nlwp;

        /**
         * Reserved for future use.
         */
        public int pr__pad1;

        /**
         * Real user ID.
         */
        public long pr_uid;

        /**
         * Effective user ID.
         */
        public long pr_euid;

        /**
         * Real group ID.
         */
        public long pr_gid;

        /**
         * Effective group ID.
         */
        public long pr_egid;

        /**
         * Unique process ID.
         */
        public long pr_pid;

        /**
         * Process ID of parent.
         */
        public long pr_ppid;

        /**
         * PID of process group leader.
         */
        public long pr_pgid;

        /**
         * Session ID.
         */
        public long pr_sid;

        /**
         * Controlling tty device.
         */
        public long pr_ttydev;

        /**
         * Internal address of proc struct.
         */
        public long pr_addr;

        /**
         * Size of process image in KB (1024) units.
         */
        public long pr_size;

        /**
         * Resident set size in KB (1024) units.
         */
        public long pr_rssize;

        /**
         * Process start time, time since epoch.
         */
        public Timestruc pr_start;

        /**
         * User + system CPU time for this process.
         */
        public Timestruc pr_time;

        /**
         * Corral ID.
         */
        public short pr_cid;

        /**
         * Reserved for future use.
         */
        public short pr__pad2;

        /**
         * Initial argument count.
         */
        public int pr_argc;

        /**
         * Address of initial argument vector in user process.
         */
        public long pr_argv;

        /**
         * Address of initial environment vector in user process.
         */
        public long pr_envp;

        /**
         * Last component of exec()ed pathname.
         */
        public byte[] pr_fname = new byte[PRFNSZ];

        /**
         * Initial characters of arg list.
         */
        public byte[] pr_psargs = new byte[PRARGSZ];

        /**
         * Reserved for future use.
         */
        public long[] pr__pad = new long[8];

        /**
         * "Representative" thread info.
         */
        public AixLwpsInfo pr_lwp;

        /**
         * Constructs an {@code AixPsInfo} object by reading data from a ByteBuffer.
         *
         * @param buff The ByteBuffer to read from.
         */
        public AixPsInfo(ByteBuffer buff) {
            this.pr_flag = Parsing.readIntFromBuffer(buff);
            this.pr_flag2 = Parsing.readIntFromBuffer(buff);
            this.pr_nlwp = Parsing.readIntFromBuffer(buff);
            this.pr__pad1 = Parsing.readIntFromBuffer(buff);
            this.pr_uid = Parsing.readLongFromBuffer(buff);
            this.pr_euid = Parsing.readLongFromBuffer(buff);
            this.pr_gid = Parsing.readLongFromBuffer(buff);
            this.pr_egid = Parsing.readLongFromBuffer(buff);
            this.pr_pid = Parsing.readLongFromBuffer(buff);
            this.pr_ppid = Parsing.readLongFromBuffer(buff);
            this.pr_pgid = Parsing.readLongFromBuffer(buff);
            this.pr_sid = Parsing.readLongFromBuffer(buff);
            this.pr_ttydev = Parsing.readLongFromBuffer(buff);
            this.pr_addr = Parsing.readLongFromBuffer(buff);
            this.pr_size = Parsing.readLongFromBuffer(buff);
            this.pr_rssize = Parsing.readLongFromBuffer(buff);
            this.pr_start = new Timestruc(buff);
            this.pr_time = new Timestruc(buff);
            this.pr_cid = Parsing.readShortFromBuffer(buff);
            this.pr__pad2 = Parsing.readShortFromBuffer(buff);
            this.pr_argc = Parsing.readIntFromBuffer(buff);
            this.pr_argv = Parsing.readLongFromBuffer(buff);
            this.pr_envp = Parsing.readLongFromBuffer(buff);
            Parsing.readByteArrayFromBuffer(buff, this.pr_fname);
            Parsing.readByteArrayFromBuffer(buff, this.pr_psargs);
            for (int i = 0; i < pr__pad.length; i++) {
                this.pr__pad[i] = Parsing.readLongFromBuffer(buff);
            }
            this.pr_lwp = new AixLwpsInfo(buff);
        }

    }

    /**
     * Represents the lightweight process (thread) information structure on AIX.
     *
     * @author Kimi Liu
     */
    class AixLwpsInfo {

        /**
         * Thread ID.
         */
        public long pr_lwpid;

        /**
         * Internal address of thread.
         */
        public long pr_addr;

        /**
         * Wait address for sleeping thread.
         */
        public long pr_wchan;

        /**
         * Thread flags.
         */
        public int pr_flag;

        /**
         * Type of thread wait.
         */
        public byte pr_wtype;

        /**
         * Numeric scheduling state.
         */
        public byte pr_state;

        /**
         * Printable character representing pr_state.
         */
        public byte pr_sname;

        /**
         * Nice value for CPU usage.
         */
        public byte pr_nice;

        /**
         * Priority, high value = high priority.
         */
        public int pr_pri;

        /**
         * Scheduling policy.
         */
        public int pr_policy;

        /**
         * Printable character representing pr_policy.
         */
        public byte[] pr_clname = new byte[PRCLSZ];

        /**
         * Processor on which thread last ran.
         */
        public int pr_onpro;

        /**
         * Processor to which thread is bound.
         */
        public int pr_bindpro;

        /**
         * Constructs an {@code AixLwpsInfo} object by reading data from a ByteBuffer.
         *
         * @param buff The ByteBuffer to read from.
         */
        public AixLwpsInfo(ByteBuffer buff) {
            this.pr_lwpid = Parsing.readLongFromBuffer(buff);
            this.pr_addr = Parsing.readLongFromBuffer(buff);
            this.pr_wchan = Parsing.readLongFromBuffer(buff);
            this.pr_flag = Parsing.readIntFromBuffer(buff);
            this.pr_wtype = Parsing.readByteFromBuffer(buff);
            this.pr_state = Parsing.readByteFromBuffer(buff);
            this.pr_sname = Parsing.readByteFromBuffer(buff);
            this.pr_nice = Parsing.readByteFromBuffer(buff);
            this.pr_pri = Parsing.readIntFromBuffer(buff);
            this.pr_policy = Parsing.readIntFromBuffer(buff);
            Parsing.readByteArrayFromBuffer(buff, this.pr_clname);
            this.pr_onpro = Parsing.readIntFromBuffer(buff);
            this.pr_bindpro = Parsing.readIntFromBuffer(buff);
        }

    }

    /**
     * 64-bit timestruc required for psinfo structure.
     *
     * @author Kimi Liu
     */
    class Timestruc {

        /**
         * Seconds.
         */
        public long tv_sec;

        /**
         * Nanoseconds.
         */
        public int tv_nsec;

        /**
         * Padding.
         */
        public int pad;

        /**
         * Constructs a {@code Timestruc} object by reading data from a ByteBuffer.
         *
         * @param buff The ByteBuffer to read from.
         */
        public Timestruc(ByteBuffer buff) {
            this.tv_sec = Parsing.readLongFromBuffer(buff);
            this.tv_nsec = Parsing.readIntFromBuffer(buff);
            this.pad = Parsing.readIntFromBuffer(buff);
        }

    }

}
