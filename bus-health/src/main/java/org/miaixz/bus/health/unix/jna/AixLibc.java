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
package org.miaixz.bus.health.unix.jna;

import java.nio.ByteBuffer;

import org.miaixz.bus.health.Builder;

import com.sun.jna.Native;

/**
 * C library for AIX. This class should be considered non-API as it may be removed if/when its code is incorporated into
 * the JNA project.
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
            this.pr_flag = Builder.readIntFromBuffer(buff);
            this.pr_flag2 = Builder.readIntFromBuffer(buff);
            this.pr_nlwp = Builder.readIntFromBuffer(buff);
            this.pr__pad1 = Builder.readIntFromBuffer(buff);
            this.pr_uid = Builder.readLongFromBuffer(buff);
            this.pr_euid = Builder.readLongFromBuffer(buff);
            this.pr_gid = Builder.readLongFromBuffer(buff);
            this.pr_egid = Builder.readLongFromBuffer(buff);
            this.pr_pid = Builder.readLongFromBuffer(buff);
            this.pr_ppid = Builder.readLongFromBuffer(buff);
            this.pr_pgid = Builder.readLongFromBuffer(buff);
            this.pr_sid = Builder.readLongFromBuffer(buff);
            this.pr_ttydev = Builder.readLongFromBuffer(buff);
            this.pr_addr = Builder.readLongFromBuffer(buff);
            this.pr_size = Builder.readLongFromBuffer(buff);
            this.pr_rssize = Builder.readLongFromBuffer(buff);
            this.pr_start = new Timestruc(buff);
            this.pr_time = new Timestruc(buff);
            this.pr_cid = Builder.readShortFromBuffer(buff);
            this.pr__pad2 = Builder.readShortFromBuffer(buff);
            this.pr_argc = Builder.readIntFromBuffer(buff);
            this.pr_argv = Builder.readLongFromBuffer(buff);
            this.pr_envp = Builder.readLongFromBuffer(buff);
            Builder.readByteArrayFromBuffer(buff, this.pr_fname);
            Builder.readByteArrayFromBuffer(buff, this.pr_psargs);
            for (int i = 0; i < pr__pad.length; i++) {
                this.pr__pad[i] = Builder.readLongFromBuffer(buff);
            }
            this.pr_lwp = new AixLwpsInfo(buff);
        }

    }

    /**
     * Represents the lightweight process (thread) information structure on AIX.
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
            this.pr_lwpid = Builder.readLongFromBuffer(buff);
            this.pr_addr = Builder.readLongFromBuffer(buff);
            this.pr_wchan = Builder.readLongFromBuffer(buff);
            this.pr_flag = Builder.readIntFromBuffer(buff);
            this.pr_wtype = Builder.readByteFromBuffer(buff);
            this.pr_state = Builder.readByteFromBuffer(buff);
            this.pr_sname = Builder.readByteFromBuffer(buff);
            this.pr_nice = Builder.readByteFromBuffer(buff);
            this.pr_pri = Builder.readIntFromBuffer(buff);
            this.pr_policy = Builder.readIntFromBuffer(buff);
            Builder.readByteArrayFromBuffer(buff, this.pr_clname);
            this.pr_onpro = Builder.readIntFromBuffer(buff);
            this.pr_bindpro = Builder.readIntFromBuffer(buff);
        }
    }

    /**
     * 64-bit timestruc required for psinfo structure.
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
            this.tv_sec = Builder.readLongFromBuffer(buff);
            this.tv_nsec = Builder.readIntFromBuffer(buff);
            this.pad = Builder.readIntFromBuffer(buff);
        }
    }

}
