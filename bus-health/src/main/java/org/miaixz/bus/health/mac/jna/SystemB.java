/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.mac.jna;

import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.unix.jna.CLibrary;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Union;

/**
 * System class. This class should be considered non-API as it may be removed if/when its code is incorporated into the
 * JNA project.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SystemB extends com.sun.jna.platform.mac.SystemB, CLibrary {

    /**
     * Singleton instance of the SystemB library.
     */
    SystemB INSTANCE = Native.load("System", SystemB.class);

    /**
     * Command to list file descriptors for a process.
     */
    int PROC_PIDLISTFDS = 1;
    /**
     * File descriptor type for a socket.
     */
    int PROX_FDTYPE_SOCKET = 2;
    /**
     * Command to get socket information for a process file descriptor.
     */
    int PROC_PIDFDSOCKETINFO = 3;
    /**
     * Number of timers in the TCP socket info structure.
     */
    int TSI_T_NTIMERS = 4;
    /**
     * Socket information flag for internet sockets.
     */
    int SOCKINFO_IN = 1;
    /**
     * Socket information flag for TCP sockets.
     */
    int SOCKINFO_TCP = 2;

    /**
     * Size of the ut_user field in the utmpx structure.
     */
    int UTX_USERSIZE = 256;
    /**
     * Size of the ut_line field in the utmpx structure.
     */
    int UTX_LINESIZE = 32;
    /**
     * Size of the ut_id field in the utmpx structure.
     */
    int UTX_IDSIZE = 4;
    /**
     * Size of the ut_host field in the utmpx structure.
     */
    int UTX_HOSTSIZE = 256;

    /**
     * The Internet Protocol version 4 (IPv4) address family.
     */
    int AF_INET = 2;
    /**
     * The Internet Protocol version 6 (IPv6) address family.
     */
    int AF_INET6 = 30;

    /**
     * Reads a line from the current file position in the utmp file. It returns a pointer to a structure containing the
     * fields of the line.
     * <p>
     * Not thread safe.
     *
     * @return a {@link MacUtmpx} on success, and NULL on failure (which includes the "record not found" case).
     */
    MacUtmpx getutxent();

    /**
     * Retrieves information about a file descriptor for a process.
     *
     * @param pid        The process ID.
     * @param fd         The file descriptor.
     * @param flavor     The type of information to retrieve.
     * @param buffer     A {@link Structure} to store the retrieved information.
     * @param buffersize The size of the buffer.
     * @return An integer result code.
     */
    int proc_pidfdinfo(int pid, int fd, int flavor, Structure buffer, int buffersize);

    /**
     * JNA wrapper for the Mac utmpx structure.
     * <p>
     * This class maps to the native macOS utmpx structure representing an entry in the user accounting database: {@code
     * struct utmpx {
     *     char ut_user[UTX_USERSIZE];
     *     char ut_id[UTX_IDSIZE];
     *     char ut_line[UTX_LINESIZE]; pid_t ut_pid; short ut_type; struct timeval ut_tv; char ut_host[UTX_HOSTSIZE];
     * char ut_pad[16]; }; }
     * </p>
     */
    @FieldOrder({ "ut_user", "ut_id", "ut_line", "ut_pid", "ut_type", "ut_tv", "ut_host", "ut_pad" })
    class MacUtmpx extends Structure {

        /**
         * Login name.
         */
        public byte[] ut_user = new byte[UTX_USERSIZE];
        /**
         * ID.
         */
        public byte[] ut_id = new byte[UTX_IDSIZE];
        /**
         * TTY name.
         */
        public byte[] ut_line = new byte[UTX_LINESIZE];
        /**
         * Process ID creating the entry.
         */
        public int ut_pid;
        /**
         * Type of this entry.
         */
        public short ut_type;
        /**
         * Time entry was created.
         */
        public Timeval ut_tv;
        /**
         * Host name.
         */
        public byte[] ut_host = new byte[UTX_HOSTSIZE];
        /**
         * Reserved for future use.
         */
        public byte[] ut_pad = new byte[16];
    }

    /**
     * JNA wrapper for the ProcFdInfo structure.
     * <p>
     * This class maps to the native macOS file descriptor information structure.
     * </p>
     */
    @FieldOrder({ "proc_fd", "proc_fdtype" })
    class ProcFdInfo extends Structure {

        /**
         * File descriptor number.
         */
        public int proc_fd;
        /**
         * Type of the file descriptor.
         */
        public int proc_fdtype;
    }

    /**
     * JNA wrapper for the InSockInfo structure.
     * <p>
     * This class maps to the native macOS internet socket information structure.
     * </p>
     */
    @FieldOrder({ "insi_fport", "insi_lport", "insi_gencnt", "insi_flags", "insi_flow", "insi_vflag", "insi_ip_ttl",
            "rfu_1", "insi_faddr", "insi_laddr", "insi_v4", "insi_v6" })
    class InSockInfo extends Structure {

        /**
         * Foreign port.
         */
        public int insi_fport;
        /**
         * Local port.
         */
        public int insi_lport;
        /**
         * Generation count of this instance.
         */
        public long insi_gencnt;
        /**
         * Generic IP/datagram flags.
         */
        public int insi_flags;
        /**
         * Flow ID.
         */
        public int insi_flow;

        /**
         * IP version flag (e.g., ini_IPV4 or ini_IPV6).
         */
        public byte insi_vflag;
        /**
         * Time to live protocol.
         */
        public byte insi_ip_ttl;
        /**
         * Reserved for future use.
         */
        public int rfu_1;
        /**
         * Foreign host table entry (protocol dependent part, v4 only in last element).
         */
        public int[] insi_faddr = new int[4];
        /**
         * Local host table entry.
         */
        public int[] insi_laddr = new int[4];
        /**
         * Type of service for IPv4.
         */
        public byte insi_v4;
        /**
         * IPv6 address bytes.
         */
        public byte[] insi_v6 = new byte[9];
    }

    /**
     * JNA wrapper for the TcpSockInfo structure.
     * <p>
     * This class maps to the native macOS TCP socket information structure.
     * </p>
     */
    @FieldOrder({ "tcpsi_ini", "tcpsi_state", "tcpsi_timer", "tcpsi_mss", "tcpsi_flags", "rfu_1", "tcpsi_tp" })
    class TcpSockInfo extends Structure {

        /**
         * Internet socket information.
         */
        public InSockInfo tcpsi_ini;
        /**
         * TCP state.
         */
        public int tcpsi_state;
        /**
         * TCP timers.
         */
        public int[] tcpsi_timer = new int[TSI_T_NTIMERS];
        /**
         * Maximum segment size.
         */
        public int tcpsi_mss;
        /**
         * TCP flags.
         */
        public int tcpsi_flags;
        /**
         * Reserved for future use.
         */
        public int rfu_1;
        /**
         * Opaque handle of TCP protocol control block.
         */
        public long tcpsi_tp;
    }

    /**
     * JNA wrapper for the SocketInfo structure.
     * <p>
     * This class maps to the native macOS IP socket information structure.
     * </p>
     */
    @FieldOrder({ "soi_stat", "soi_so", "soi_pcb", "soi_type", "soi_protocol", "soi_family", "soi_options",
            "soi_linger", "soi_state", "soi_qlen", "soi_incqlen", "soi_qlimit", "soi_timeo", "soi_error", "soi_oobmark",
            "soi_rcv", "soi_snd", "soi_kind", "rfu_1", "soi_proto" })
    class SocketInfo extends Structure {

        /**
         * Vnode information statistics.
         */
        public long[] soi_stat = new long[17];
        /**
         * Opaque handle of socket.
         */
        public long soi_so;
        /**
         * Opaque handle of protocol control block.
         */
        public long soi_pcb;
        /**
         * Socket type.
         */
        public int soi_type;
        /**
         * Protocol family.
         */
        public int soi_protocol;
        /**
         * Address family.
         */
        public int soi_family;
        /**
         * Socket options.
         */
        public short soi_options;
        /**
         * Linger time.
         */
        public short soi_linger;
        /**
         * Socket state.
         */
        public short soi_state;
        /**
         * Queue length.
         */
        public short soi_qlen;
        /**
         * Incoming queue length.
         */
        public short soi_incqlen;
        /**
         * Queue limit.
         */
        public short soi_qlimit;
        /**
         * Timeout.
         */
        public short soi_timeo;
        /**
         * Error code.
         */
        public short soi_error;
        /**
         * Out-of-band mark.
         */
        public int soi_oobmark;
        /**
         * Socket receive buffer information.
         */
        public int[] soi_rcv = new int[6];
        /**
         * Socket send buffer information.
         */
        public int[] soi_snd = new int[6];
        /**
         * Socket kind.
         */
        public int soi_kind;
        /**
         * Reserved for future use.
         */
        public int rfu_1;
        /**
         * Protocol-specific information (union).
         */
        public Pri soi_proto;
    }

    /**
     * JNA wrapper for the ProcFileInfo structure.
     * <p>
     * This class maps to the native macOS file information structure.
     * </p>
     */
    @FieldOrder({ "fi_openflags", "fi_status", "fi_offset", "fi_type", "fi_guardflags" })
    class ProcFileInfo extends Structure {

        /**
         * Open flags.
         */
        public int fi_openflags;
        /**
         * File status flags.
         */
        public int fi_status;
        /**
         * File offset.
         */
        public long fi_offset;
        /**
         * File type.
         */
        public int fi_type;
        /**
         * Guard flags.
         */
        public int fi_guardflags;
    }

    /**
     * JNA wrapper for the SocketFdInfo structure.
     * <p>
     * This class maps to the native macOS socket file descriptor information structure.
     * </p>
     */
    @FieldOrder({ "pfi", "psi" })
    class SocketFdInfo extends Structure implements AutoCloseable {

        /**
         * Process file information.
         */
        public ProcFileInfo pfi;
        /**
         * Socket information.
         */
        public SocketInfo psi;

        /**
         * Closes the memory associated with this structure.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /**
     * JNA wrapper for the Pri union.
     * <p>
     * This class maps to the native macOS union for protocol-specific socket information.
     * </p>
     */
    class Pri extends Union {

        /**
         * Internet socket information.
         */
        public InSockInfo pri_in;
        /**
         * TCP socket information.
         */
        public TcpSockInfo pri_tcp;
        /**
         * Maximum size of the union, used for memory allocation.
         */
        public byte[] max_size = new byte[524];
    }

}
