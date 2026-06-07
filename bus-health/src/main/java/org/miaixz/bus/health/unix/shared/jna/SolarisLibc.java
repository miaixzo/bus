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

import org.miaixz.bus.health.Builder;

/**
 * C library for Solaris. This class should be considered non-API as it may be removed if/when its code is incorporated
 * into the JNA project.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * @since Java 21+
     */
    @FieldOrder({ "ut_user", "ut_id", "ut_line", "ut_pid", "ut_type", "ut_exit", "ut_tv", "ut_session", "pad",
            "ut_syslen", "ut_host" })
    class SolarisUtmpx extends Structure {

        /**
         * Creates a new SolarisUtmpx instance.
         */
        public SolarisUtmpx() {
        }

        /**
         * The ut_user value.
         */
        public byte[] ut_user = new byte[UTX_USERSIZE]; // user login name
        /**
         * The ut_id value.
         */
        public byte[] ut_id = new byte[UTX_IDSIZE]; // etc/inittab id (usually line #)
        /**
         * The ut_line value.
         */
        public byte[] ut_line = new byte[UTX_LINESIZE]; // device name
        /**
         * The ut_pid value.
         */
        public int ut_pid; // process id
        /**
         * The ut_type value.
         */
        public short ut_type; // type of entry
        /**
         * The ut_exit value.
         */
        public Exit_status ut_exit; // process termination/exit status
        /**
         * The ut_tv value.
         */
        public Timeval ut_tv; // time entry was made
        /**
         * The ut_session value.
         */
        public int ut_session; // session ID, used for windowing
        /**
         * The pad value.
         */
        public int[] pad = new int[5]; // reserved for future use
        /**
         * The ut_syslen value.
         */
        public short ut_syslen; // significant length of ut_host including terminating null
        /**
         * The ut_host value.
         */
        public byte[] ut_host = new byte[UTX_HOSTSIZE]; // host name

    }

    /**
     * JNA wrapper for the exit_status structure.
     * <p>
     * This class maps to the native Solaris exit_status structure which is part of utmpx.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "e_termination", "e_exit" })
    class Exit_status extends Structure {

        /**
         * Creates a new Exit_status instance.
         */
        public Exit_status() {
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
     * JNA wrapper for the timeval structure.
     * <p>
     * This class maps to the native Solaris timeval structure: {@code struct timeval { time_t tv_sec; suseconds_t
     * tv_usec; }; }
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "tv_sec", "tv_usec" })
    class Timeval extends Structure {

        /**
         * Creates a new Timeval instance.
         */
        public Timeval() {
        }

        /**
         * The tv_sec value.
         */
        public NativeLong tv_sec; // seconds
        /**
         * The tv_usec value.
         */
        public NativeLong tv_usec; // microseconds

    }

    /**
     * Structure for psinfo file
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class SolarisPsInfo {

        /**
         * The pr_flag value.
         */
        public int pr_flag; // process flags (DEPRECATED; do not use)
        /**
         * The pr_nlwp value.
         */
        public int pr_nlwp; // number of active lwps in the process
        /**
         * The pr_pid value.
         */
        public int pr_pid; // unique process id
        /**
         * The pr_ppid value.
         */
        public int pr_ppid; // process id of parent
        /**
         * The pr_pgid value.
         */
        public int pr_pgid; // pid of process group leader
        /**
         * The pr_sid value.
         */
        public int pr_sid; // session id
        /**
         * The pr_uid value.
         */
        public int pr_uid; // real user id
        /**
         * The pr_euid value.
         */
        public int pr_euid; // effective user id
        /**
         * The pr_gid value.
         */
        public int pr_gid; // real group id
        /**
         * The pr_egid value.
         */
        public int pr_egid; // effective group id
        /**
         * The pr_addr value.
         */
        public Pointer pr_addr; // address of process
        /**
         * The pr_size value.
         */
        public size_t pr_size; // size of process image in Kbytes
        /**
         * The pr_rssize value.
         */
        public size_t pr_rssize; // resident set size in Kbytes
        /**
         * The pr_rssizepriv value.
         */
        public size_t pr_rssizepriv; // resident set size of private mappings
        /**
         * The pr_ttydev value.
         */
        public NativeLong pr_ttydev; // controlling tty device (or PRNODEV)
        // The following percent numbers are 16-bit binary
        // fractions [0 .. 1] with the binary point to the
        // right of the high-order bit (1.0 == 0x8000)
        /**
         * The pr_pctcpu value.
         */
        public short pr_pctcpu; // % of recent cpu time used by all lwps
        /**
         * The pr_pctmem value.
         */
        public short pr_pctmem; // % of system memory used by process
        /**
         * The pr_start value.
         */
        public Timestruc pr_start; // process start time, from the epoch
        /**
         * The pr_time value.
         */
        public Timestruc pr_time; // usr+sys cpu time for this process
        /**
         * The pr_ctime value.
         */
        public Timestruc pr_ctime; // usr+sys cpu time for reaped children
        /**
         * The pr_fname value.
         */
        public byte[] pr_fname = new byte[PRFNSZ]; // name of exec'ed file
        /**
         * The pr_psargs value.
         */
        public byte[] pr_psargs = new byte[PRARGSZ]; // initial characters of arg list
        /**
         * The pr_wstat value.
         */
        public int pr_wstat; // if zombie, the wait() status
        /**
         * The pr_argc value.
         */
        public int pr_argc; // initial argument count
        /**
         * The pr_argv value.
         */
        public Pointer pr_argv; // address of initial argument vector
        /**
         * The pr_envp value.
         */
        public Pointer pr_envp; // address of initial environment vector
        /**
         * The pr_dmodel value.
         */
        public byte pr_dmodel; // data model of the process
        /**
         * The pr_pad2 value.
         */
        public byte[] pr_pad2 = new byte[3];

        /**
         * The pr_taskid value.
         */
        public int pr_taskid; // task id
        /**
         * The pr_projid value.
         */
        public int pr_projid; // project id
        /**
         * The pr_nzomb value.
         */
        public int pr_nzomb; // number of zombie lwps in the process
        /**
         * The pr_poolid value.
         */
        public int pr_poolid; // pool id
        /**
         * The pr_zoneid value.
         */
        public int pr_zoneid; // zone id
        /**
         * The pr_contract value.
         */
        public int pr_contract; // process contract id
        /**
         * The pr_filler value.
         */
        public int pr_filler; // 4 bytes reserved for future use
        /**
         * The pr_lwp value.
         */
        public SolarisLwpsInfo pr_lwp; // information for representative lwp

        /**
         * Creates a new SolarisPsInfo instance.
         *
         * @param buff the buff
         */
        public SolarisPsInfo(ByteBuffer buff) {
            this.pr_flag = Builder.readIntFromBuffer(buff);
            this.pr_nlwp = Builder.readIntFromBuffer(buff);
            this.pr_pid = Builder.readIntFromBuffer(buff);
            this.pr_ppid = Builder.readIntFromBuffer(buff);
            this.pr_pgid = Builder.readIntFromBuffer(buff);
            this.pr_sid = Builder.readIntFromBuffer(buff);
            this.pr_uid = Builder.readIntFromBuffer(buff);
            this.pr_euid = Builder.readIntFromBuffer(buff);
            this.pr_gid = Builder.readIntFromBuffer(buff);
            this.pr_egid = Builder.readIntFromBuffer(buff);
            this.pr_addr = Builder.readPointerFromBuffer(buff);
            this.pr_size = Builder.readSizeTFromBuffer(buff);
            this.pr_rssize = Builder.readSizeTFromBuffer(buff);
            this.pr_rssizepriv = Builder.readSizeTFromBuffer(buff);
            this.pr_ttydev = Builder.readNativeLongFromBuffer(buff);
            this.pr_pctcpu = Builder.readShortFromBuffer(buff);
            this.pr_pctmem = Builder.readShortFromBuffer(buff);
            // Force 8 byte alignment
            if (Native.LONG_SIZE > 4) {
                Builder.readIntFromBuffer(buff);
            }
            this.pr_start = new Timestruc(buff);
            this.pr_time = new Timestruc(buff);
            this.pr_ctime = new Timestruc(buff);
            Builder.readByteArrayFromBuffer(buff, this.pr_fname);
            Builder.readByteArrayFromBuffer(buff, this.pr_psargs);
            this.pr_wstat = Builder.readIntFromBuffer(buff);
            this.pr_argc = Builder.readIntFromBuffer(buff);
            this.pr_argv = Builder.readPointerFromBuffer(buff);
            this.pr_envp = Builder.readPointerFromBuffer(buff);
            this.pr_dmodel = Builder.readByteFromBuffer(buff);
            Builder.readByteArrayFromBuffer(buff, this.pr_pad2);
            this.pr_taskid = Builder.readIntFromBuffer(buff);
            this.pr_projid = Builder.readIntFromBuffer(buff);
            this.pr_nzomb = Builder.readIntFromBuffer(buff);
            this.pr_poolid = Builder.readIntFromBuffer(buff);
            this.pr_zoneid = Builder.readIntFromBuffer(buff);
            this.pr_contract = Builder.readIntFromBuffer(buff);
            this.pr_filler = Builder.readIntFromBuffer(buff);
            this.pr_lwp = new SolarisLwpsInfo(buff);
        }

    }

    /**
     * Nested Structure for psinfo file
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class SolarisLwpsInfo {

        /**
         * The pr_flag value.
         */
        public int pr_flag; // lwp flags (DEPRECATED; do not use)
        /**
         * The pr_lwpid value.
         */
        public int pr_lwpid; // lwp id
        /**
         * The pr_addr value.
         */
        public Pointer pr_addr; // DEPRECATED was internal address of lwp
        /**
         * The pr_wchan value.
         */
        public Pointer pr_wchan; // DEPRECATED was wait addr for sleeping lwp
        /**
         * The pr_stype value.
         */
        public byte pr_stype; // synchronization event type
        /**
         * The pr_state value.
         */
        public byte pr_state; // numeric lwp state
        /**
         * The pr_sname value.
         */
        public byte pr_sname; // printable character for pr_state
        /**
         * The pr_nice value.
         */
        public byte pr_nice; // nice for cpu usage
        /**
         * The pr_syscall value.
         */
        public short pr_syscall; // system call number (if in syscall)
        /**
         * The pr_oldpri value.
         */
        public byte pr_oldpri; // pre-SVR4, low value is high priority
        /**
         * The pr_cpu value.
         */
        public byte pr_cpu; // pre-SVR4, cpu usage for scheduling
        /**
         * The pr_pri value.
         */
        public int pr_pri; // priority, high value = high priority
        // The following percent numbers are 16-bit binary
        // fractions [0 .. 1] with the binary point to the
        // right of the high-order bit (1.0 == 0x8000)
        /**
         * The pr_pctcpu value.
         */
        public short pr_pctcpu; // % of recent cpu time used by this lwp
        /**
         * The pr_pad value.
         */
        public short pr_pad;

        /**
         * The pr_start value.
         */
        public Timestruc pr_start; // lwp start time, from the epoch
        /**
         * The pr_time value.
         */
        public Timestruc pr_time; // cpu time for this lwp
        /**
         * The pr_clname value.
         */
        public byte[] pr_clname = new byte[PRCLSZ]; // scheduling class name
        /**
         * The pr_oldname value.
         */
        public byte[] pr_oldname = new byte[PRFNSZ]; // binary compatibility -- unused
        /**
         * The pr_onpro value.
         */
        public int pr_onpro; // processor which last ran this lwp
        /**
         * The pr_bindpro value.
         */
        public int pr_bindpro; // processor to which lwp is bound
        /**
         * The pr_bindpset value.
         */
        public int pr_bindpset; // processor set to which lwp is bound
        /**
         * The pr_lgrp value.
         */
        public int pr_lgrp; // home lgroup
        /**
         * The pr_last_onproc value.
         */
        public long pr_last_onproc; // Timestamp of when thread last ran on a processor
        /**
         * The pr_name value.
         */
        public byte[] pr_name = new byte[PRLNSZ]; // name of system lwp

        /**
         * Creates a new SolarisLwpsInfo instance.
         *
         * @param buff the buff
         */
        public SolarisLwpsInfo(ByteBuffer buff) {
            this.pr_flag = Builder.readIntFromBuffer(buff);
            this.pr_lwpid = Builder.readIntFromBuffer(buff);
            this.pr_addr = Builder.readPointerFromBuffer(buff);
            this.pr_wchan = Builder.readPointerFromBuffer(buff);
            this.pr_stype = Builder.readByteFromBuffer(buff);
            this.pr_state = Builder.readByteFromBuffer(buff);
            this.pr_sname = Builder.readByteFromBuffer(buff);
            this.pr_nice = Builder.readByteFromBuffer(buff);
            this.pr_syscall = Builder.readShortFromBuffer(buff);
            this.pr_oldpri = Builder.readByteFromBuffer(buff);
            this.pr_cpu = Builder.readByteFromBuffer(buff);
            this.pr_pri = Builder.readIntFromBuffer(buff);
            this.pr_pctcpu = Builder.readShortFromBuffer(buff);
            this.pr_pad = Builder.readShortFromBuffer(buff);
            this.pr_start = new Timestruc(buff);
            this.pr_time = new Timestruc(buff);
            Builder.readByteArrayFromBuffer(buff, this.pr_clname);
            Builder.readByteArrayFromBuffer(buff, this.pr_oldname);
            this.pr_onpro = Builder.readIntFromBuffer(buff);
            this.pr_bindpro = Builder.readIntFromBuffer(buff);
            this.pr_bindpset = Builder.readIntFromBuffer(buff);
            this.pr_lgrp = Builder.readIntFromBuffer(buff);
            this.pr_last_onproc = Builder.readLongFromBuffer(buff);
            Builder.readByteArrayFromBuffer(buff, this.pr_name);
        }

    }

    /**
     * Structure for usage file
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class SolarisPrUsage {

        /**
         * The pr_lwpid value.
         */
        public int pr_lwpid; // lwp id. 0: process or defunct
        /**
         * The pr_count value.
         */
        public int pr_count; // number of contributing lwps
        /**
         * The pr_tstamp value.
         */
        public Timestruc pr_tstamp; // current time stamp
        /**
         * The pr_create value.
         */
        public Timestruc pr_create; // process/lwp creation time stamp
        /**
         * The pr_term value.
         */
        public Timestruc pr_term; // process/lwp termination time stamp
        /**
         * The pr_rtime value.
         */
        public Timestruc pr_rtime; // total lwp real (elapsed) time
        /**
         * The pr_utime value.
         */
        public Timestruc pr_utime; // user level cpu time
        /**
         * The pr_stime value.
         */
        public Timestruc pr_stime; // system call cpu time
        /**
         * The pr_ttime value.
         */
        public Timestruc pr_ttime; // other system trap cpu time
        /**
         * The pr_tftime value.
         */
        public Timestruc pr_tftime; // text page fault sleep time
        /**
         * The pr_dftime value.
         */
        public Timestruc pr_dftime; // data page fault sleep time
        /**
         * The pr_kftime value.
         */
        public Timestruc pr_kftime; // kernel page fault sleep time
        /**
         * The pr_ltime value.
         */
        public Timestruc pr_ltime; // user lock wait sleep time
        /**
         * The pr_slptime value.
         */
        public Timestruc pr_slptime; // all other sleep time
        /**
         * The pr_wtime value.
         */
        public Timestruc pr_wtime; // wait-cpu (latency) time
        /**
         * The pr_stoptime value.
         */
        public Timestruc pr_stoptime; // stopped time
        /**
         * The filltime value.
         */
        public Timestruc[] filltime = new Timestruc[6]; // filler for future expansion
        /**
         * The pr_minf value.
         */
        public NativeLong pr_minf; // minor page faults
        /**
         * The pr_majf value.
         */
        public NativeLong pr_majf; // major page faults
        /**
         * The pr_nswap value.
         */
        public NativeLong pr_nswap; // swaps
        /**
         * The pr_inblk value.
         */
        public NativeLong pr_inblk; // input blocks
        /**
         * The pr_oublk value.
         */
        public NativeLong pr_oublk; // output blocks
        /**
         * The pr_msnd value.
         */
        public NativeLong pr_msnd; // messages sent
        /**
         * The pr_mrcv value.
         */
        public NativeLong pr_mrcv; // messages received
        /**
         * The pr_sigs value.
         */
        public NativeLong pr_sigs; // signals received
        /**
         * The pr_vctx value.
         */
        public NativeLong pr_vctx; // voluntary context switches
        /**
         * The pr_ictx value.
         */
        public NativeLong pr_ictx; // involuntary context switches
        /**
         * The pr_sysc value.
         */
        public NativeLong pr_sysc; // system calls
        /**
         * The pr_ioch value.
         */
        public NativeLong pr_ioch; // chars read and written
        /**
         * The filler value.
         */
        public NativeLong[] filler = new NativeLong[10]; // filler for future expansion

        /**
         * Creates a new SolarisPrUsage instance.
         *
         * @param buff the buff
         */
        public SolarisPrUsage(ByteBuffer buff) {
            this.pr_lwpid = Builder.readIntFromBuffer(buff);
            this.pr_count = Builder.readIntFromBuffer(buff);
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
            this.pr_minf = Builder.readNativeLongFromBuffer(buff);
            this.pr_majf = Builder.readNativeLongFromBuffer(buff);
            this.pr_nswap = Builder.readNativeLongFromBuffer(buff);
            this.pr_inblk = Builder.readNativeLongFromBuffer(buff);
            this.pr_oublk = Builder.readNativeLongFromBuffer(buff);
            this.pr_msnd = Builder.readNativeLongFromBuffer(buff);
            this.pr_mrcv = Builder.readNativeLongFromBuffer(buff);
            this.pr_sigs = Builder.readNativeLongFromBuffer(buff);
            this.pr_vctx = Builder.readNativeLongFromBuffer(buff);
            this.pr_ictx = Builder.readNativeLongFromBuffer(buff);
            this.pr_sysc = Builder.readNativeLongFromBuffer(buff);
            this.pr_ioch = Builder.readNativeLongFromBuffer(buff);
            for (int i = 0; i < filler.length; i++) {
                this.filler[i] = Builder.readNativeLongFromBuffer(buff);
            }
        }

    }

    /**
     * 32/64-bit timestruc required for psinfo and lwpsinfo structures
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class Timestruc {

        /**
         * The tv_sec value.
         */
        public NativeLong tv_sec; // seconds
        /**
         * The tv_nsec value.
         */
        public NativeLong tv_nsec; // nanoseconds

        /**
         * Creates a new Timestruc instance.
         *
         * @param buff the buff
         */
        public Timestruc(ByteBuffer buff) {
            this.tv_sec = Builder.readNativeLongFromBuffer(buff);
            this.tv_nsec = Builder.readNativeLongFromBuffer(buff);
        }

    }

}
