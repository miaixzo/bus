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
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ a~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * C library for OpenBSD. This class should be considered non-API as it may be removed if/when its code is incorporated
 * into the JNA project.
 */
public interface OpenBsdLibc extends CLibrary {

    /**
     * Singleton instance of the OpenBsdLibc library.
     */
    OpenBsdLibc INSTANCE = Native.load(null, OpenBsdLibc.class);

    /**
     * High kernel: proc, limits
     */
    int CTL_KERN = 1;
    /**
     * High kernel: proc, limits
     */
    int CTL_VM = 1;
    /**
     * Generic CPU/IO
     */
    int CTL_HW = 6;
    /**
     * Machine dependent
     */
    int CTL_MACHDEP = 7;
    /**
     * VFS sysctls
     */
    int CTL_VFS = 10;

    /**
     * String: system version
     */
    int KERN_OSTYPE = 1;
    /**
     * String: system release
     */
    int KERN_OSRELEASE = 2;
    /**
     * Int: system revision
     */
    int KERN_OSREV = 3;
    /**
     * String: compile time info
     */
    int KERN_VERSION = 4;
    /**
     * Int: max vnodes
     */
    int KERN_MAXVNODES = 5;
    /**
     * Int: max processes
     */
    int KERN_MAXPROC = 6;
    /**
     * Int: max arguments to exec
     */
    int KERN_ARGMAX = 8;
    /**
     * Array: cp_time
     */
    int KERN_CPTIME = 40;
    /**
     * Array: cp_time2
     */
    int KERN_CPTIME2 = 71;

    /**
     * Struct uvmexp
     */
    int VM_UVMEXP = 4;

    /**
     * String: machine class
     */
    int HW_MACHINE = 1;
    /**
     * String: specific machine model
     */
    int HW_MODEL = 2;
    /**
     * Int: software page size
     */
    int HW_PAGESIZE = 7;
    /**
     * Get CPU frequency
     */
    int HW_CPUSPEED = 12;
    /**
     * CPU found (includes offline)
     */
    int HW_NCPUFOUND = 21;
    /**
     * Enable SMT/HT/CMT
     */
    int HW_SMT = 24;
    /**
     * Number of cpus being used
     */
    int HW_NCPUONLINE = 25;

    /**
     * Generic filesystem information
     */
    int VFS_GENERIC = 0;
    /**
     * Struct: buffer cache statistics given as next argument
     */
    int VFS_BCACHESTAT = 3;

    /*
     * CPU state indices
     */
    /**
     * Number of CPU states.
     */
    int CPUSTATES = 5;
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
     * CPU interrupt state index (4 on 6.4 and later).
     */
    int CP_INTR = 3;
    /**
     * CPU idle state index (5 on 6.4 and later).
     */
    int CP_IDLE = 4;

    /**
     * Size of a 64-bit unsigned integer.
     */
    int UINT64_SIZE = Native.getNativeSize(long.class);
    /**
     * Size of an integer.
     */
    int INT_SIZE = Native.getNativeSize(int.class);

    /**
     * Returns the thread ID of the calling thread. This is used in the implementation of the thread library (-lpthread)
     * and can appear in the output of system utilities such as ps and kdump.
     *
     * @return the thread ID of the calling thread.
     */
    int getthrid();

    /**
     * OpenBSD Cache stats for memory
     */
    @FieldOrder({ "numbufs", "numbufpages", "numdirtypages", "numcleanpages", "pendingwrites", "pendingreads",
            "numwrites", "numreads", "cachehits", "busymapped", "dmapages", "highpages", "delwribufs", "kvaslots",
            "kvaslots_avail", "highflips", "highflops", "dmaflips" })
    class Bcachestats extends Structure {

        public long numbufs; // number of buffers allocated
        public long numbufpages; // number of pages in buffer cache
        public long numdirtypages; // number of dirty free pages
        public long numcleanpages; // number of clean free pages
        public long pendingwrites; // number of pending writes
        public long pendingreads; // number of pending reads
        public long numwrites; // total writes started
        public long numreads; // total reads started
        public long cachehits; // total reads found in cache
        public long busymapped; // number of busy and mapped buffers
        public long dmapages; // dma reachable pages in buffer cache
        public long highpages; // pages above dma region
        public long delwribufs; // delayed write buffers
        public long kvaslots; // kva slots total
        public long kvaslots_avail; // available kva slots
        public long highflips; // total flips to above DMA
        public long highflops; // total failed flips to above DMA
        public long dmaflips; // total flips from high to DMA

        public Bcachestats(Pointer p) {
            super(p);
            read();
        }
    }

    /**
     * Return type for BSD sysctl kern.boottime
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

}
