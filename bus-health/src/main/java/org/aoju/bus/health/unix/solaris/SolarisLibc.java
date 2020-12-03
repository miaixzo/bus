/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.unix.solaris;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import org.aoju.bus.health.unix.CLibrary;

/**
 * C动态库。这个类应该被认为是非api的，因为如果/当
 * 它的代码被合并到JNA项目中时，它可能会被删除
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public interface SolarisLibc extends CLibrary {

    SolarisLibc INSTANCE = Native.load("c", SolarisLibc.class);

    int UTX_USERSIZE = 32;
    int UTX_LINESIZE = 32;
    int UTX_IDSIZE = 4;
    int UTX_HOSTSIZE = 257;

    /**
     * Reads a line from the current file position in the utmp file. It returns a
     * pointer to a structure containing the fields of the line.
     * <p>
     * Not thread safe
     *
     * @return a {@link SolarisUtmpx} on success, and NULL on failure (which
     * includes the "record not found" case)
     */
    SolarisUtmpx getutxent();

    @FieldOrder({"ut_user", "ut_id", "ut_line", "ut_pid", "ut_type", "ut_tv", "ut_session", "ut_syslen", "ut_host"})
    class SolarisUtmpx extends Structure {
        public byte[] ut_user = new byte[UTX_USERSIZE]; // user login name
        public byte[] ut_id = new byte[UTX_IDSIZE]; // etc/inittab id (usually line #)
        public byte[] ut_line = new byte[UTX_LINESIZE]; // device name
        public int ut_pid; // process id
        public short ut_type; // type of entry
        public Timeval ut_tv; // time entry was made
        public int ut_session; // session ID, used for windowing
        public short ut_syslen; // significant length of ut_host including terminating null
        public byte[] ut_host = new byte[UTX_HOSTSIZE]; // host name
    }

    /**
     * Part of utmpx structure
     */
    @FieldOrder({"e_termination", "e_exit"})
    class Exit_status extends Structure {
        public short e_termination; // Process termination status
        public short e_exit; // Process exit status
    }

    /**
     * 64-bit timeval required for utmpx structure
     */
    @FieldOrder({"tv_sec", "tv_usec"})
    class Timeval extends Structure {
        public NativeLong tv_sec; // seconds
        public NativeLong tv_usec; // microseconds
    }

}
