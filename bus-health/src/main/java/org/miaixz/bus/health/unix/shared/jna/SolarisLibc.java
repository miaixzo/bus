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
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

import org.miaixz.bus.health.Parsing;

/**
 * C library for Solaris. This class should be considered non-API as it may be removed if/when its code is incorporated
 * into the JNA project.
 *
 * @author Kimi Liu
 */
public interface SolarisLibc extends CLibrary {

    /**
     * Singleton instance of the SolarisLibc library.
     */
    SolarisLibc INSTANCE = Native.load("c", SolarisLibc.class);

    /**
     * The UTX_USERSIZE value.
     */
    int UTX_USERSIZE = 32;

    /**
     * The UTX_LINESIZE value.
     */
    int UTX_LINESIZE = 32;

    /**
     * The UTX_IDSIZE value.
     */
    int UTX_IDSIZE = 4;

    /**
     * The UTX_HOSTSIZE value.
     */
    int UTX_HOSTSIZE = 257;

    /**
     * The PRCLSZ value.
     */
    int PRCLSZ = 8;

    /**
     * The PRFNSZ value.
     */
    int PRFNSZ = 16;

    /**
     * The PRLNSZ value.
     */
    int PRLNSZ = 32;

    /**
     * The PRARGSZ value.
     */
    int PRARGSZ = 80;

    /**
     * Reads a line from the current file position in the utmp file. It returns a pointer to a structure containing the
     * fields of the line.
     * <p>
     * Not thread safe
     *
     * @return a {@link SolarisUtmpx} on success, and NULL on failure (which includes the "record not found" case)
     */
    SolarisUtmpx getutxent();

    /**
     * Returns the thread ID of the calling thread.
     *
     * @return the thread ID of the calling thread.
     */
    int thr_self();

    /**
     * JNA wrapper for the Solaris utmpx structure.
     * <p>
     * This class maps to the native Solaris utmpx structure representing user accounting database entries.
     * </p>
     *
     * @author Kimi Liu
     */
    @FieldOrder({ "ut_user", "ut_id", "ut_line", "ut_pid", "ut_type", "ut_exit", "ut_tv", "ut_session", "pad",
            "ut_syslen", "ut_host" })
    class SolarisUtmpx extends Structure {

        /**
         * User login name.
         */
        public byte[] ut_user = new byte[UTX_USERSIZE];
        /**
         * Etc/inittab id (usually line #).
         */
        public byte[] ut_id = new byte[UTX_IDSIZE];
        /**
         * Device name.
         */
        public byte[] ut_line = new byte[UTX_LINESIZE];
        /**
         * Process id.
         */
        public int ut_pid;
        /**
         * Type of entry.
         */
        public short ut_type;
        /**
         * Process termination/exit status.
         */
        public Exit_status ut_exit;
        /**
         * Time entry was made.
         */
        public Timeval ut_tv;
        /**
         * Session ID, used for windowing.
         */
        public int ut_session;
        /**
         * Reserved for future use.
         */
        public int[] pad = new int[5];
        /**
         * Significant length of ut_host including terminating null.
         */
        public short ut_syslen;
        /**
         * Host name.
         */
        public byte[] ut_host = new byte[UTX_HOSTSIZE];

        /**
         * Creates a new SolarisUtmpx instance.
         */
        public SolarisUtmpx() {
            // No initialization required.
        }

    }

    /**
     * JNA wrapper for the exit_status structure.
     * <p>
     * This class maps to the native Solaris exit_status structure which is part of utmpx.
     * </p>
     *
     * @author Kimi Liu
     */
    @FieldOrder({ "e_termination", "e_exit" })
    class Exit_status extends Structure {

        /**
         * Process termination status.
         */
        public short e_termination;
        /**
         * Process exit status.
         */
        public short e_exit;

        /**
         * Creates a new Exit_status instance.
         */
        public Exit_status() {
            // No initialization required.
        }

    }

    /**
     * JNA wrapper for the timeval structure.
     * <p>
     * This class maps to the native Solaris timeval structure: {@code struct timeval { time_t tv_sec; suseconds_t
     * tv_usec; }; }
     * </p>
     *
     * @author Kimi Liu
     */
    @FieldOrder({ "tv_sec", "tv_usec" })
    class Timeval extends Structure {

        /**
         * Seconds.
         */
        public NativeLong tv_sec;
        /**
         * Microseconds.
         */
        public NativeLong tv_usec;

        /**
         * Creates a new Timeval instance.
         */
        public Timeval() {
            // No initialization required.
        }

    }

    /**
     * Structure for psinfo file
     *
     * @author Kimi Liu
     */
    class SolarisPsInfo {

        /**
         * Process flags (DEPRECATED; do not use).
         */
        public int pr_flag;
        /**
         * Number of active lwps in the process.
         */
        public int pr_nlwp;
        /**
         * Unique process id.
         */
        public int pr_pid;
        /**
         * Process id of parent.
         */
        public int pr_ppid;
        /**
         * Pid of process group leader.
         */
        public int pr_pgid;
        /**
         * Session id.
         */
        public int pr_sid;
        /**
         * Real user id.
         */
        public int pr_uid;
        /**
         * Effective user id.
         */
        public int pr_euid;
        /**
         * Real group id.
         */
        public int pr_gid;
        /**
         * Effective group id.
         */
        public int pr_egid;
        /**
         * Address of process.
         */
        public Pointer pr_addr;
        /**
         * Size of process image in Kbytes.
         */
        public size_t pr_size;
        /**
         * Resident set size in Kbytes.
         */
        public size_t pr_rssize;
        /**
         * Resident set size of private mappings.
         */
        public size_t pr_rssizepriv;
        /**
         * Controlling tty device (or PRNODEV).
         */
        public NativeLong pr_ttydev;
        /**
         * The following percent numbers are 16-bit binary fractions [0 .. 1] with the binary point to the right of the
         * high-order bit (1.0 == 0x8000).
         * <p>
         * % of recent cpu time used by all lwps.
         */
        public short pr_pctcpu;
        /**
         * % of system memory used by process.
         */
        public short pr_pctmem;
        /**
         * Process start time, from the epoch.
         */
        public Timestruc pr_start;
        /**
         * Usr+sys cpu time for this process.
         */
        public Timestruc pr_time;
        /**
         * Usr+sys cpu time for reaped children.
         */
        public Timestruc pr_ctime;
        /**
         * Name of exec'ed file.
         */
        public byte[] pr_fname = new byte[PRFNSZ];
        /**
         * Initial characters of arg list.
         */
        public byte[] pr_psargs = new byte[PRARGSZ];
        /**
         * If zombie, the wait() status.
         */
        public int pr_wstat;
        /**
         * Initial argument count.
         */
        public int pr_argc;
        /**
         * Address of initial argument vector.
         */
        public Pointer pr_argv;
        /**
         * Address of initial environment vector.
         */
        public Pointer pr_envp;
        /**
         * Data model of the process.
         */
        public byte pr_dmodel;
        /**
         * The pr_pad2 value.
         */
        public byte[] pr_pad2 = new byte[3];

        /**
         * Task id.
         */
        public int pr_taskid;
        /**
         * Project id.
         */
        public int pr_projid;
        /**
         * Number of zombie lwps in the process.
         */
        public int pr_nzomb;
        /**
         * Pool id.
         */
        public int pr_poolid;
        /**
         * Zone id.
         */
        public int pr_zoneid;
        /**
         * Process contract id.
         */
        public int pr_contract;
        /**
         * 4 bytes reserved for future use.
         */
        public int pr_filler;
        /**
         * Information for representative lwp.
         */
        public SolarisLwpsInfo pr_lwp;

        /**
         * Creates a new SolarisPsInfo instance.
         *
         * @param buff the buff
         */
        public SolarisPsInfo(ByteBuffer buff) {
            this.pr_flag = Parsing.readIntFromBuffer(buff);
            this.pr_nlwp = Parsing.readIntFromBuffer(buff);
            this.pr_pid = Parsing.readIntFromBuffer(buff);
            this.pr_ppid = Parsing.readIntFromBuffer(buff);
            this.pr_pgid = Parsing.readIntFromBuffer(buff);
            this.pr_sid = Parsing.readIntFromBuffer(buff);
            this.pr_uid = Parsing.readIntFromBuffer(buff);
            this.pr_euid = Parsing.readIntFromBuffer(buff);
            this.pr_gid = Parsing.readIntFromBuffer(buff);
            this.pr_egid = Parsing.readIntFromBuffer(buff);
            this.pr_addr = Parsing.readPointerFromBuffer(buff);
            this.pr_size = Parsing.readSizeTFromBuffer(buff);
            this.pr_rssize = Parsing.readSizeTFromBuffer(buff);
            this.pr_rssizepriv = Parsing.readSizeTFromBuffer(buff);
            this.pr_ttydev = Parsing.readNativeLongFromBuffer(buff);
            this.pr_pctcpu = Parsing.readShortFromBuffer(buff);
            this.pr_pctmem = Parsing.readShortFromBuffer(buff);
            // Force 8 byte alignment
            if (Native.LONG_SIZE > 4) {
                Parsing.readIntFromBuffer(buff);
            }
            this.pr_start = new Timestruc(buff);
            this.pr_time = new Timestruc(buff);
            this.pr_ctime = new Timestruc(buff);
            Parsing.readByteArrayFromBuffer(buff, this.pr_fname);
            Parsing.readByteArrayFromBuffer(buff, this.pr_psargs);
            this.pr_wstat = Parsing.readIntFromBuffer(buff);
            this.pr_argc = Parsing.readIntFromBuffer(buff);
            this.pr_argv = Parsing.readPointerFromBuffer(buff);
            this.pr_envp = Parsing.readPointerFromBuffer(buff);
            this.pr_dmodel = Parsing.readByteFromBuffer(buff);
            Parsing.readByteArrayFromBuffer(buff, this.pr_pad2);
            this.pr_taskid = Parsing.readIntFromBuffer(buff);
            this.pr_projid = Parsing.readIntFromBuffer(buff);
            this.pr_nzomb = Parsing.readIntFromBuffer(buff);
            this.pr_poolid = Parsing.readIntFromBuffer(buff);
            this.pr_zoneid = Parsing.readIntFromBuffer(buff);
            this.pr_contract = Parsing.readIntFromBuffer(buff);
            this.pr_filler = Parsing.readIntFromBuffer(buff);
            this.pr_lwp = new SolarisLwpsInfo(buff);
        }

    }

    /**
     * Nested Structure for psinfo file
     *
     * @author Kimi Liu
     */
    class SolarisLwpsInfo {

        /**
         * Lwp flags (DEPRECATED; do not use).
         */
        public int pr_flag;
        /**
         * Lwp id.
         */
        public int pr_lwpid;
        /**
         * DEPRECATED was internal address of lwp.
         */
        public Pointer pr_addr;
        /**
         * DEPRECATED was wait addr for sleeping lwp.
         */
        public Pointer pr_wchan;
        /**
         * Synchronization event type.
         */
        public byte pr_stype;
        /**
         * Numeric lwp state.
         */
        public byte pr_state;
        /**
         * Printable character for pr_state.
         */
        public byte pr_sname;
        /**
         * Nice for cpu usage.
         */
        public byte pr_nice;
        /**
         * System call number (if in syscall).
         */
        public short pr_syscall;
        /**
         * Pre-SVR4, low value is high priority.
         */
        public byte pr_oldpri;
        /**
         * Pre-SVR4, cpu usage for scheduling.
         */
        public byte pr_cpu;
        /**
         * Priority, high value = high priority.
         */
        public int pr_pri;
        /**
         * The following percent numbers are 16-bit binary fractions [0 .. 1] with the binary point to the right of the
         * high-order bit (1.0 == 0x8000).
         * <p>
         * % of recent cpu time used by this lwp.
         */
        public short pr_pctcpu;
        /**
         * The pr_pad value.
         */
        public short pr_pad;

        /**
         * Lwp start time, from the epoch.
         */
        public Timestruc pr_start;
        /**
         * Cpu time for this lwp.
         */
        public Timestruc pr_time;
        /**
         * Scheduling class name.
         */
        public byte[] pr_clname = new byte[PRCLSZ];
        /**
         * Binary compatibility -- unused.
         */
        public byte[] pr_oldname = new byte[PRFNSZ];
        /**
         * Processor which last ran this lwp.
         */
        public int pr_onpro;
        /**
         * Processor to which lwp is bound.
         */
        public int pr_bindpro;
        /**
         * Processor set to which lwp is bound.
         */
        public int pr_bindpset;
        /**
         * Home lgroup.
         */
        public int pr_lgrp;
        /**
         * Timestamp of when thread last ran on a processor.
         */
        public long pr_last_onproc;
        /**
         * Name of system lwp.
         */
        public byte[] pr_name = new byte[PRLNSZ];

        /**
         * Creates a new SolarisLwpsInfo instance.
         *
         * @param buff the buff
         */
        public SolarisLwpsInfo(ByteBuffer buff) {
            this.pr_flag = Parsing.readIntFromBuffer(buff);
            this.pr_lwpid = Parsing.readIntFromBuffer(buff);
            this.pr_addr = Parsing.readPointerFromBuffer(buff);
            this.pr_wchan = Parsing.readPointerFromBuffer(buff);
            this.pr_stype = Parsing.readByteFromBuffer(buff);
            this.pr_state = Parsing.readByteFromBuffer(buff);
            this.pr_sname = Parsing.readByteFromBuffer(buff);
            this.pr_nice = Parsing.readByteFromBuffer(buff);
            this.pr_syscall = Parsing.readShortFromBuffer(buff);
            this.pr_oldpri = Parsing.readByteFromBuffer(buff);
            this.pr_cpu = Parsing.readByteFromBuffer(buff);
            this.pr_pri = Parsing.readIntFromBuffer(buff);
            this.pr_pctcpu = Parsing.readShortFromBuffer(buff);
            this.pr_pad = Parsing.readShortFromBuffer(buff);
            this.pr_start = new Timestruc(buff);
            this.pr_time = new Timestruc(buff);
            Parsing.readByteArrayFromBuffer(buff, this.pr_clname);
            Parsing.readByteArrayFromBuffer(buff, this.pr_oldname);
            this.pr_onpro = Parsing.readIntFromBuffer(buff);
            this.pr_bindpro = Parsing.readIntFromBuffer(buff);
            this.pr_bindpset = Parsing.readIntFromBuffer(buff);
            this.pr_lgrp = Parsing.readIntFromBuffer(buff);
            this.pr_last_onproc = Parsing.readLongFromBuffer(buff);
            Parsing.readByteArrayFromBuffer(buff, this.pr_name);
        }

    }

    /**
     * Structure for usage file
     *
     * @author Kimi Liu
     */
    class SolarisPrUsage {

        /**
         * Lwp id. 0: process or defunct.
         */
        public int pr_lwpid;
        /**
         * Number of contributing lwps.
         */
        public int pr_count;
        /**
         * Current time stamp.
         */
        public Timestruc pr_tstamp;
        /**
         * Process/lwp creation time stamp.
         */
        public Timestruc pr_create;
        /**
         * Process/lwp termination time stamp.
         */
        public Timestruc pr_term;
        /**
         * Total lwp real (elapsed) time.
         */
        public Timestruc pr_rtime;
        /**
         * User level cpu time.
         */
        public Timestruc pr_utime;
        /**
         * System call cpu time.
         */
        public Timestruc pr_stime;
        /**
         * Other system trap cpu time.
         */
        public Timestruc pr_ttime;
        /**
         * Text page fault sleep time.
         */
        public Timestruc pr_tftime;
        /**
         * Data page fault sleep time.
         */
        public Timestruc pr_dftime;
        /**
         * Kernel page fault sleep time.
         */
        public Timestruc pr_kftime;
        /**
         * User lock wait sleep time.
         */
        public Timestruc pr_ltime;
        /**
         * All other sleep time.
         */
        public Timestruc pr_slptime;
        /**
         * Wait-cpu (latency) time.
         */
        public Timestruc pr_wtime;
        /**
         * Stopped time.
         */
        public Timestruc pr_stoptime;
        /**
         * Filler for future expansion.
         */
        public Timestruc[] filltime = new Timestruc[6];
        /**
         * Minor page faults.
         */
        public NativeLong pr_minf;
        /**
         * Major page faults.
         */
        public NativeLong pr_majf;
        /**
         * Swaps.
         */
        public NativeLong pr_nswap;
        /**
         * Input blocks.
         */
        public NativeLong pr_inblk;
        /**
         * Output blocks.
         */
        public NativeLong pr_oublk;
        /**
         * Messages sent.
         */
        public NativeLong pr_msnd;
        /**
         * Messages received.
         */
        public NativeLong pr_mrcv;
        /**
         * Signals received.
         */
        public NativeLong pr_sigs;
        /**
         * Voluntary context switches.
         */
        public NativeLong pr_vctx;
        /**
         * Involuntary context switches.
         */
        public NativeLong pr_ictx;
        /**
         * System calls.
         */
        public NativeLong pr_sysc;
        /**
         * Chars read and written.
         */
        public NativeLong pr_ioch;
        /**
         * Filler for future expansion.
         */
        public NativeLong[] filler = new NativeLong[10];

        /**
         * Creates a new SolarisPrUsage instance.
         *
         * @param buff the buff
         */
        public SolarisPrUsage(ByteBuffer buff) {
            this.pr_lwpid = Parsing.readIntFromBuffer(buff);
            this.pr_count = Parsing.readIntFromBuffer(buff);
            this.pr_tstamp = new Timestruc(buff);
            this.pr_create = new Timestruc(buff);
            this.pr_term = new Timestruc(buff);
            this.pr_rtime = new Timestruc(buff);
            this.pr_utime = new Timestruc(buff);
            this.pr_stime = new Timestruc(buff);
            this.pr_ttime = new Timestruc(buff);
            this.pr_tftime = new Timestruc(buff);
            this.pr_dftime = new Timestruc(buff);
            this.pr_kftime = new Timestruc(buff);
            this.pr_ltime = new Timestruc(buff);
            this.pr_slptime = new Timestruc(buff);
            this.pr_wtime = new Timestruc(buff);
            this.pr_stoptime = new Timestruc(buff);
            for (int i = 0; i < filltime.length; i++) {
                this.filltime[i] = new Timestruc(buff);
            }
            this.pr_minf = Parsing.readNativeLongFromBuffer(buff);
            this.pr_majf = Parsing.readNativeLongFromBuffer(buff);
            this.pr_nswap = Parsing.readNativeLongFromBuffer(buff);
            this.pr_inblk = Parsing.readNativeLongFromBuffer(buff);
            this.pr_oublk = Parsing.readNativeLongFromBuffer(buff);
            this.pr_msnd = Parsing.readNativeLongFromBuffer(buff);
            this.pr_mrcv = Parsing.readNativeLongFromBuffer(buff);
            this.pr_sigs = Parsing.readNativeLongFromBuffer(buff);
            this.pr_vctx = Parsing.readNativeLongFromBuffer(buff);
            this.pr_ictx = Parsing.readNativeLongFromBuffer(buff);
            this.pr_sysc = Parsing.readNativeLongFromBuffer(buff);
            this.pr_ioch = Parsing.readNativeLongFromBuffer(buff);
            for (int i = 0; i < filler.length; i++) {
                this.filler[i] = Parsing.readNativeLongFromBuffer(buff);
            }
        }

    }

    /**
     * 32/64-bit timestruc required for psinfo and lwpsinfo structures
     *
     * @author Kimi Liu
     */
    class Timestruc {

        /**
         * Seconds.
         */
        public NativeLong tv_sec;
        /**
         * Nanoseconds.
         */
        public NativeLong tv_nsec;

        /**
         * Creates a new Timestruc instance.
         *
         * @param buff the buff
         */
        public Timestruc(ByteBuffer buff) {
            this.tv_sec = Parsing.readNativeLongFromBuffer(buff);
            this.tv_nsec = Parsing.readNativeLongFromBuffer(buff);
        }

    }

}
