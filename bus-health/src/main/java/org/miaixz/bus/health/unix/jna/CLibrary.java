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

import org.miaixz.bus.health.Builder;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.unix.LibCAPI;
import com.sun.jna.ptr.PointerByReference;

/**
 * C library with code common to all *nix-based operating systems. This class should be considered non-API as it may be
 * removed if/when its code is incorporated into the JNA project.
 */
public interface CLibrary extends LibCAPI, Library {

    /**
     * Flag for getaddrinfo(): if specified, return canonical name.
     */
    int AI_CANONNAME = 2;

    /**
     * Size of the ut_line field in a utmpx structure.
     */
    int UT_LINESIZE = 32;
    /**
     * Size of the ut_name field in a utmpx structure.
     */
    int UT_NAMESIZE = 32;
    /**
     * Size of the ut_host field in a utmpx structure.
     */
    int UT_HOSTSIZE = 256;
    /**
     * Session leader of a logged in user.
     */
    int LOGIN_PROCESS = 6;
    /**
     * Normal process.
     */
    int USER_PROCESS = 7;

    /**
     * Returns the process ID of the calling process. The ID is guaranteed to be unique and is useful for constructing
     * temporary file names.
     *
     * @return the process ID of the calling process.
     */
    int getpid();

    /**
     * Given node and service, which identify an Internet host and a service, getaddrinfo() returns one or more addrinfo
     * structures, each of which contains an Internet address that can be specified in a call to bind(2) or connect(2).
     *
     * @param node    a numerical network address or a network hostname, whose network addresses are looked up and
     *                resolved.
     * @param service sets the port in each returned address structure.
     * @param hints   specifies criteria for selecting the socket address structures returned in the list pointed to by
     *                res.
     * @param res     returned address structure
     * @return 0 on success; sets errno on failure
     */
    int getaddrinfo(String node, String service, Addrinfo hints, PointerByReference res);

    /*
     * Between macOS and FreeBSD there are multiple versions of some tcp/udp/ip stats structures. Since we only need a
     * few of the hundreds of fields, we can improve performance by selectively reading the ints from the appropriate
     * offsets, which are consistent across the structure. These classes include the common fields and offsets.
     */

    /**
     * Frees the memory that was allocated for the dynamically allocated linked list res.
     *
     * @param res Pointer to linked list returned by getaddrinfo
     */
    void freeaddrinfo(Pointer res);

    /**
     * Translates getaddrinfo error codes to a human readable string, suitable for error reporting.
     *
     * @param e Error code from getaddrinfo
     * @return A human-readable version of the error code
     */
    String gai_strerror(int e);

    /**
     * Rewinds the file pointer to the beginning of the utmp file. It is generally a good idea to call it before any of
     * the other functions.
     */
    void setutxent();

    /**
     * Closes the utmp file. It should be called when the user code is done accessing the file with the other functions.
     */
    void endutxent();

    /**
     * The sysctl() function retrieves system information and allows processes with appropriate privileges to set system
     * information. The information available from sysctl() consists of integers, strings, and tables.
     * <p>
     * The state is described using a "Management Information Base" (MIB) style name, listed in name, which is a namelen
     * length array of integers.
     * <p>
     * The information is copied into the buffer specified by oldp. The size of the buffer is given by the location
     * specified by oldlenp before the call, and that location gives the amount of data copied after a successful call
     * and after a call that returns with the error code ENOMEM. If the amount of data available is greater than the
     * size of the buffer supplied, the call supplies as much data as fits in the buffer provided and returns with the
     * error code ENOMEM. If the old value is not desired, oldp and oldlenp should be set to NULL.
     * <p>
     * The size of the available data can be determined by calling sysctl() with the NULL argument for oldp. The size of
     * the available data will be returned in the location pointed to by oldlenp. For some operations, the amount of
     * space may change often. For these operations, the system attempts to round up so that the returned size is large
     * enough for a call to return the data shortly thereafter.
     * <p>
     * To set a new value, newp is set to point to a buffer of length newlen from which the requested value is to be
     * taken. If a new value is not to be set, newp should be set to NULL and newlen set to 0.
     *
     * @param name    MIB array of integers
     * @param namelen length of the MIB array
     * @param oldp    Information retrieved
     * @param oldlenp Size of information retrieved
     * @param newp    Information to be written
     * @param newlen  Size of information to be written
     * @return 0 on success; sets errno on failure
     */
    int sysctl(int[] name, int namelen, Pointer oldp, size_t.ByReference oldlenp, Pointer newp, size_t newlen);

    /**
     * The sysctlbyname() function accepts an ASCII representation of the name and internally looks up the integer name
     * vector. Apart from that, it behaves the same as the standard sysctl() function.
     *
     * @param name    ASCII representation of the MIB name
     * @param oldp    Information retrieved
     * @param oldlenp Size of information retrieved
     * @param newp    Information to be written
     * @param newlen  Size of information to be written
     * @return 0 on success; sets errno on failure
     */
    int sysctlbyname(String name, Pointer oldp, size_t.ByReference oldlenp, Pointer newp, size_t newlen);

    /**
     * The sysctlnametomib() function accepts an ASCII representation of the name, looks up the integer name vector, and
     * returns the numeric representation in the mib array pointed to by mibp. The number of elements in the mib array
     * is given by the location specified by sizep before the call, and that location gives the number of entries copied
     * after a successful call. The resulting mib and size may be used in subsequent sysctl() calls to get the data
     * associated with the requested ASCII name. This interface is intended for use by applications that want to
     * repeatedly request the same variable (the sysctl() function runs in about a third the time as the same request
     * made via the sysctlbyname() function).
     * <p>
     * The number of elements in the mib array can be determined by calling sysctlnametomib() with the NULL argument for
     * mibp.
     * <p>
     * The sysctlnametomib() function is also useful for fetching mib prefixes. If size on input is greater than the
     * number of elements written, the array still contains the additional elements which may be written
     * programmatically.
     *
     * @param name  ASCII representation of the name
     * @param mibp  Integer array containing the corresponding name vector.
     * @param sizep On input, number of elements in the returned array; on output, the number of entries copied.
     * @return 0 on success; sets errno on failure
     */
    int sysctlnametomib(String name, Pointer mibp, size_t.ByReference sizep);

    /**
     * Opens a file at the given absolute path.
     *
     * @param absolutePath The absolute path of the file to open.
     * @param i            File access flags.
     * @return a file descriptor on success, or -1 on failure.
     */
    int open(String absolutePath, int i);

    /**
     * Reads from a file descriptor at a given offset.
     *
     * @param fildes The file descriptor.
     * @param buf    A buffer to read data into.
     * @param nbyte  The number of bytes to read.
     * @param offset The offset in the file to read from.
     * @return the number of bytes read on success, or -1 on failure.
     */
    ssize_t pread(int fildes, Pointer buf, size_t nbyte, NativeLong offset);

    /**
     * Socket address structure.
     */
    @FieldOrder({ "sa_family", "sa_data" })
    class Sockaddr extends Structure {

        /**
         * Address family.
         */
        public short sa_family;
        /**
         * Socket address data.
         */
        public byte[] sa_data = new byte[14];

        /**
         * A reference to a {@link Sockaddr} structure.
         */
        public static class ByReference extends Sockaddr implements Structure.ByReference {
        }
    }

    /**
     * TCP statistics structure for BSD.
     */
    class BsdTcpstat {

        /**
         * Connections initiated.
         */
        public int tcps_connattempt;
        /**
         * Connections accepted.
         */
        public int tcps_accepts;
        /**
         * Connections dropped.
         */
        public int tcps_drops;
        /**
         * Embryonic connections dropped.
         */
        public int tcps_conndrops;
        /**
         * Packets sent.
         */
        public int tcps_sndpack;
        /**
         * Packets retransmitted.
         */
        public int tcps_sndrexmitpack;
        /**
         * Packets received.
         */
        public int tcps_rcvpack;
        /**
         * Packets received with bad checksum.
         */
        public int tcps_rcvbadsum;
        /**
         * Packets received with bad offset.
         */
        public int tcps_rcvbadoff;
        /**
         * Packets dropped for lack of memory.
         */
        public int tcps_rcvmemdrop;
        /**
         * Packets received shorter than header.
         */
        public int tcps_rcvshort;
    }

    /**
     * UDP statistics structure for BSD.
     */
    class BsdUdpstat {

        /**
         * Total input packets.
         */
        public int udps_ipackets;
        /**
         * Dropped due to no socket.
         */
        public int udps_hdrops;
        /**
         * Checksum error.
         */
        public int udps_badsum;
        /**
         * Data length larger than packet.
         */
        public int udps_badlen;
        /**
         * Total output packets.
         */
        public int udps_opackets;
        /**
         * No multicast destination.
         */
        public int udps_noportmcast;
        /**
         * Software checksummed packets received.
         */
        public int udps_rcv6_swcsum;
        /**
         * Software checksummed packets sent.
         */
        public int udps_snd6_swcsum;
    }

    /**
     * IP statistics structure for BSD.
     */
    class BsdIpstat {

        /**
         * Total packets received.
         */
        public int ips_total;
        /**
         * Checksum bad.
         */
        public int ips_badsum;
        /**
         * Packet too short.
         */
        public int ips_tooshort;
        /**
         * Not enough data.
         */
        public int ips_toosmall;
        /**
         * Bad header length.
         */
        public int ips_badhlen;
        /**
         * Bad packet length.
         */
        public int ips_badlen;
        /**
         * Delivered to upper level protocols.
         */
        public int ips_delivered;
    }

    /**
     * IPv6 statistics structure for BSD.
     */
    class BsdIp6stat {

        /**
         * Total packets received.
         */
        public long ip6s_total;
        /**
         * Total output packets.
         */
        public long ip6s_localout;
    }

    /**
     * Address information structure.
     */
    @FieldOrder({ "ai_flags", "ai_family", "ai_socktype", "ai_protocol", "ai_addrlen", "ai_addr", "ai_canonname",
            "ai_next" })
    class Addrinfo extends Structure implements AutoCloseable {

        /**
         * Input flags.
         */
        public int ai_flags;
        /**
         * Address family for socket.
         */
        public int ai_family;
        /**
         * Socket type.
         */
        public int ai_socktype;
        /**
         * Protocol for socket.
         */
        public int ai_protocol;
        /**
         * Length of socket address.
         */
        public int ai_addrlen;
        /**
         * Socket address for socket.
         */
        public Sockaddr.ByReference ai_addr;
        /**
         * Canonical name for service location.
         */
        public String ai_canonname;
        /**
         * Pointer to next in list.
         */
        public ByReference ai_next;

        /**
         * Constructs an {@code Addrinfo} object.
         */
        public Addrinfo() {
        }

        /**
         * Constructs an {@code Addrinfo} object from a pointer.
         *
         * @param p The pointer to the structure.
         */
        public Addrinfo(Pointer p) {
            super(p);
            read();
        }

        /**
         * Closes the memory associated with this structure.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

        /**
         * A reference to an {@link Addrinfo} structure.
         */
        public static class ByReference extends Addrinfo implements Structure.ByReference {
        }
    }

}
