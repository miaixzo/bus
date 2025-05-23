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
package org.miaixz.bus.health.unix.platform.aix.driver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.unix.jna.AixLibc;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.LibCAPI.ssize_t;

/**
 * Utility to query /proc/psinfo
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class PsInfo {

    private static final AixLibc LIBC = AixLibc.INSTANCE;

    // AIX has multiple page size units, but for purposes of "pages" in perfstat,
    // the docs specify 4KB pages so we hardcode this
    private static final long PAGE_SIZE = 4096L;

    private PsInfo() {
    }

    /**
     * Reads /proc/pid/psinfo and returns data in a structure
     *
     * @param pid The process ID
     * @return A structure containing information for the requested process
     */
    public static AixLibc.AixPsInfo queryPsInfo(int pid) {
        return new AixLibc.AixPsInfo(Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/psinfo", pid)));
    }

    /**
     * Reads /proc/pid/lwp/tid/lwpsinfo and returns data in a structure
     *
     * @param pid The process ID
     * @param tid The thread ID (lwpid)
     * @return A structure containing information for the requested thread
     */
    public static AixLibc.AixLwpsInfo queryLwpsInfo(int pid, int tid) {
        return new AixLibc.AixLwpsInfo(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/lwp/%d/lwpsinfo", pid, tid)));
    }

    /**
     * Reads the pr_argc, pr_argv, and pr_envp fields from /proc/pid/psinfo
     *
     * @param pid    The process ID
     * @param psinfo A populated {@link AixLibc.AixPsInfo} structure containing the offset pointers for these fields
     * @return A triplet containing the argc, argv, and envp values, or null if unable to read
     */
    public static Triplet<Integer, Long, Long> queryArgsEnvAddrs(int pid, AixLibc.AixPsInfo psinfo) {
        if (psinfo != null) {
            int argc = psinfo.pr_argc;
            // Must have at least one argc (the command itself) so failure here means exit
            if (argc > 0) {
                long argv = psinfo.pr_argv;
                long envp = psinfo.pr_envp;
                return Triplet.of(argc, argv, envp);
            }
            Logger.trace("Failed argc sanity check: argc={}", argc);
            return null;
        }
        Logger.trace("Failed to read psinfo file for pid: {} ", pid);
        return null;
    }

    /**
     * Read the argument and environment strings from process address space
     *
     * @param pid    the process id
     * @param psinfo A populated {@link AixLibc.AixPsInfo} structure containing the offset pointers for these fields
     * @return A pair containing a list of the arguments and a map of environment variables
     */
    public static Pair<List<String>, Map<String, String>> queryArgsEnv(int pid, AixLibc.AixPsInfo psinfo) {
        List<String> args = new ArrayList<>();
        Map<String, String> env = new LinkedHashMap<>();

        // Get the arg count and list of env vars
        Triplet<Integer, Long, Long> addrs = queryArgsEnvAddrs(pid, psinfo);
        if (addrs != null) {
            // Open a file descriptor to the address space
            String procas = "/proc/" + pid + "/as";
            int fd = LIBC.open(procas, 0);
            if (fd < 0) {
                Logger.trace("No permission to read file: {} ", procas);
                return Pair.of(args, env);
            }
            try {
                // Non-null addrs means argc > 0
                int argc = addrs.getLeft();
                long argv = addrs.getMiddle();
                long envp = addrs.getRight();

                // We need to determine if the process is 32-bit or 64-bit data model.
                long increment;
                Path p = Paths.get("/proc/" + pid + "/status");
                try {
                    byte[] status = Files.readAllBytes(p);
                    if (status[17] == 1) {
                        increment = 8;
                    } else {
                        increment = 4;
                    }
                } catch (IOException e) {
                    return Pair.of(args, env);
                }

                // Reusable buffer
                try (Memory buffer = new Memory(PAGE_SIZE * 2)) {
                    size_t bufSize = new size_t(buffer.size());

                    // Read the pointers to the arg strings
                    long bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, 0, argv);
                    long[] argPtr = new long[argc];
                    long argp = bufStart == 0 ? 0 : getOffsetFromBuffer(buffer, argv - bufStart, increment);
                    if (argp > 0) {
                        for (int i = 0; i < argc; i++) {
                            long offset = argp + i * increment;
                            bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, offset);
                            argPtr[i] = bufStart == 0 ? 0 : getOffsetFromBuffer(buffer, offset - bufStart, increment);
                        }
                    }

                    // Also read the pointers to the env strings
                    // We don't know how many, so stop when we get to null pointer
                    bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, envp);
                    List<Long> envPtrList = new ArrayList<>();
                    long addr = bufStart == 0 ? 0 : getOffsetFromBuffer(buffer, envp - bufStart, increment);
                    int limit = 500; // sane max env strings to stop at
                    long offset = addr;
                    while (addr != 0 && --limit > 0) {
                        bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, offset);
                        long envPtr = bufStart == 0 ? 0 : getOffsetFromBuffer(buffer, offset - bufStart, increment);
                        if (envPtr != 0) {
                            envPtrList.add(envPtr);
                        }
                        offset += increment;
                    }

                    // Now read the arg strings from the buffer
                    for (int i = 0; i < argPtr.length && argPtr[i] != 0; i++) {
                        bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, argPtr[i]);
                        if (bufStart != 0) {
                            String argStr = buffer.getString(argPtr[i] - bufStart);
                            if (!argStr.isEmpty()) {
                                args.add(argStr);
                            }
                        }
                    }

                    // And now read the env strings from the buffer
                    for (Long envPtr : envPtrList) {
                        bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, envPtr);
                        if (bufStart != 0) {
                            String envStr = buffer.getString(envPtr - bufStart);
                            int idx = envStr.indexOf(Symbol.C_EQUAL);
                            if (idx > 0) {
                                env.put(envStr.substring(0, idx), envStr.substring(idx + 1));
                            }
                        }
                    }
                }
            } finally {
                LIBC.close(fd);
            }
        }
        return Pair.of(args, env);
    }

    /**
     * Reads the page containing addr into buffer, unless the buffer already contains that page (as indicated by the
     * bufStart address), in which case nothing is changed.
     *
     * @param fd       The file descriptor for the address space
     * @param buffer   An allocated buffer, possibly with data reread from bufStart
     * @param bufSize  The size of the buffer
     * @param bufStart The start of data currently in bufStart, or 0 if uninitialized
     * @param addr     THe address whose page to read into the buffer
     * @return The new starting pointer for the buffer
     */
    private static long conditionallyReadBufferFromStartOfPage(int fd, Memory buffer, size_t bufSize, long bufStart,
            long addr) {
        // If we don't have the right buffer, update it
        if (addr < bufStart || addr - bufStart > PAGE_SIZE) {
            long newStart = Math.floorDiv(addr, PAGE_SIZE) * PAGE_SIZE;
            ssize_t result = LIBC.pread(fd, buffer, bufSize, new NativeLong(newStart));
            // May return less than asked but should be at least a full page
            if (result.longValue() < PAGE_SIZE) {
                Logger.debug("Failed to read page from address space: {} bytes read", result.longValue());
                return 0;
            }
            return newStart;
        }
        return bufStart;
    }

    private static long getOffsetFromBuffer(Memory buffer, long offset, long increment) {
        return increment == 8 ? buffer.getLong(offset) : buffer.getInt(offset);
    }

}
