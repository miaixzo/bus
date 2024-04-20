/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.unix;

import com.sun.jna.Native;
import org.miaixz.bus.health.Builder;

import java.nio.ByteBuffer;

/**
 * C library. This class should be considered non-API as it may be removed
 * if/when its code is incorporated into the JNA project.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AixLibc extends CLibrary {

    AixLibc INSTANCE = Native.load("c", AixLibc.class);

    int PRCLSZ = 8;
    int PRFNSZ = 16;
    int PRARGSZ = 80;

    /**
     * Returns the caller's kernel thread ID.
     *
     * @return the caller's kernel thread ID.
     */
    int thread_self();

    class AixPsInfo {
        public int pr_flag; // process flags from proc struct p_flag
        public int pr_flag2; // process flags from proc struct p_flag2
        public int pr_nlwp; // number of threads in process
        public int pr__pad1; // reserved for future use
        public long pr_uid; // real user id
        public long pr_euid; // effective user id
        public long pr_gid; // real group id
        public long pr_egid; // effective group id
        public long pr_pid; // unique process id
        public long pr_ppid; // process id of parent
        public long pr_pgid; // pid of process group leader
        public long pr_sid; // session id
        public long pr_ttydev; // controlling tty device
        public long pr_addr; // internal address of proc struct
        public long pr_size; // size of process image in KB (1024) units
        public long pr_rssize; // resident set size in KB (1024) units
        public Timestruc pr_start; // process start time, time since epoch
        public Timestruc pr_time; // usr+sys cpu time for this process
        public short pr_cid; // corral id
        public short pr__pad2; // reserved for future use
        public int pr_argc; // initial argument count
        public long pr_argv; // address of initial argument vector in user process
        public long pr_envp; // address of initial environment vector in user process
        public byte[] pr_fname = new byte[PRFNSZ]; // last component of exec()ed pathname
        public byte[] pr_psargs = new byte[PRARGSZ]; // initial characters of arg list
        public long[] pr__pad = new long[8]; // reserved for future use
        public AixLwpsInfo pr_lwp; // "representative" thread info

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

    class AixLwpsInfo {
        public long pr_lwpid; // thread id
        public long pr_addr; // internal address of thread
        public long pr_wchan; // wait addr for sleeping thread
        public int pr_flag; // thread flags
        public byte pr_wtype; // type of thread wait
        public byte pr_state; // numeric scheduling state
        public byte pr_sname; // printable character representing pr_state
        public byte pr_nice; // nice for cpu usage
        public int pr_pri; // priority, high value = high priority
        public int pr_policy; // scheduling policy
        public byte[] pr_clname = new byte[PRCLSZ]; // printable character representing pr_policy
        public int pr_onpro; // processor on which thread last ran
        public int pr_bindpro; // processor to which thread is bound

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
     * 64-bit timestruc required for psinfo structure
     */
    class Timestruc {
        public long tv_sec; // seconds
        public int tv_nsec; // nanoseconds
        public int pad; // nanoseconds

        public Timestruc(ByteBuffer buff) {
            this.tv_sec = Builder.readLongFromBuffer(buff);
            this.tv_nsec = Builder.readIntFromBuffer(buff);
            this.pad = Builder.readIntFromBuffer(buff);
        }
    }

}
