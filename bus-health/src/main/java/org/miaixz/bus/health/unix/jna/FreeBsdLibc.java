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

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.NativeLongByReference;

/**
 * C library for FreeBSD. This class should be considered non-API as it may be removed if/when its code is incorporated
 * into the JNA project.
 */
public interface FreeBsdLibc extends CLibrary {

    /**
     * Singleton instance of the FreeBsdLibc library.
     */
    FreeBsdLibc INSTANCE = Native.load("libc", FreeBsdLibc.class);

    /**
     * Size of the ut_user field in a utmpx structure.
     */
    int UTX_USERSIZE = 32;
    /**
     * Size of the ut_line field in a utmpx structure.
     */
    int UTX_LINESIZE = 16;
    /**
     * Size of the ut_id field in a utmpx structure.
     */
    int UTX_IDSIZE = 8;
    /**
     * Size of the ut_host field in a utmpx structure.
     */
    int UTX_HOSTSIZE = 128;
    /**
     * Size of a 64-bit unsigned integer.
     */
    int UINT64_SIZE = Native.getNativeSize(long.class);

    /*
     * Data size
     */
    /**
     * Size of an integer.
     */
    int INT_SIZE = Native.getNativeSize(int.class);
    /**
     * Number of CPU states.
     */
    int CPUSTATES = 5;

    /*
     * CPU state indices
     */
    /**
     * CPU user state index.
     */
    int CP_USER = 0;
    /**
     * CPU nice state index.
     */
    int CP_NICE = 1;
    /**
     * CPU system state index.
     */
    int CP_SYS = 2;
    /**
     * CPU interrupt state index.
     */
    int CP_INTR = 3;
    /**
     * CPU idle state index.
     */
    int CP_IDLE = 4;

    /**
     * Reads a line from the current file position in the utmp file. It returns a pointer to a structure containing the
     * fields of the line.
     * <p>
     * Not thread safe
     *
     * @return a {@link FreeBsdUtmpx} on success, and NULL on failure (which includes the "record not found" case)
     */
    FreeBsdUtmpx getutxent();

    /**
     * Stores the system-wide thread identifier for the current kernel-scheduled thread in the variable pointed by the
     * argument id.
     *
     * @param id The thread identifier is an integer in the range from PID_MAX + 2 (100001) to INT_MAX. The thread
     *           identifier is guaranteed to be unique at any given time, for each running thread in the system.
     * @return If successful, returns zero, otherwise -1 is returned, and errno is set to indicate the error.
     */
    int thr_self(NativeLongByReference id);

    /**
     * JNA wrapper for the FreeBSD utmpx structure.
     * <p>
     * This class maps to the native FreeBSD utmpx structure representing user accounting database entries.
     * </p>
     */
    @FieldOrder({ "ut_type", "ut_tv", "ut_id", "ut_pid", "ut_user", "ut_line", "ut_host", "ut_spare" })
    class FreeBsdUtmpx extends Structure {

        /**
         * Type of entry.
         */
        public short ut_type;
        /**
         * Time entry was made.
         */
        public Timeval ut_tv;
        /**
         * etc/inittab id (usually line #).
         */
        public byte[] ut_id = new byte[UTX_IDSIZE];
        /**
         * Process id.
         */
        public int ut_pid;
        /**
         * User login name.
         */
        public byte[] ut_user = new byte[UTX_USERSIZE];
        /**
         * Device name.
         */
        public byte[] ut_line = new byte[UTX_LINESIZE];
        /**
         * Host name.
         */
        public byte[] ut_host = new byte[UTX_HOSTSIZE];
        /**
         * Spare bytes.
         */
        public byte[] ut_spare = new byte[64];
    }

    /**
     * JNA wrapper for the timeval structure.
     * <p>
     * This class maps to the native FreeBSD timeval structure: {@code struct timeval { time_t tv_sec; suseconds_t
     * tv_usec; }; }
     * </p>
     */
    @FieldOrder({ "tv_sec", "tv_usec" })
    class Timeval extends Structure {

        /**
         * Seconds.
         */
        public long tv_sec;
        /**
         * Microseconds.
         */
        public long tv_usec;
    }

    /**
     * JNA wrapper for the CPU time statistics structure.
     * <p>
     * This class maps to the native FreeBSD CPU time tracking structure.
     * </p>
     */
    @FieldOrder({ "cpu_ticks" })
    class CpTime extends Structure implements AutoCloseable {

        /**
         * CPU ticks.
         */
        public long[] cpu_ticks = new long[CPUSTATES];

        /**
         * Closes the memory associated with this structure.
         */
        @Override
        public void close() {
            Pointer p = this.getPointer();
            if (p instanceof Memory) {
                ((Memory) p).close();
            }
        }
    }

}
