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
package org.miaixz.bus.health.unix.aix.driver;

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
import org.miaixz.bus.health.unix.shared.driver.ProcAddressSpaceReader;
import org.miaixz.bus.health.unix.shared.jna.AixLibc;
import org.miaixz.bus.logger.Logger;

/**
 * Queries /proc/psinfo
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class PsInfo {

    /**
     * The LIBC constant.
     */
    private static final AixLibc LIBC = AixLibc.INSTANCE;

    // AIX has multiple page size units, but for purposes of "pages" in perfstat,
    // the docs specify 4KB pages so we hardcode this
    /**
     * The PAGE_SIZE constant.
     */
    private static final long PAGE_SIZE = 4096L;

    /**
     * Creates a new PsInfo instance.
     */
    private PsInfo() {
        // No initialization required.
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
            Logger.trace(false, "Health", "Failed argc sanity check: argc={}", argc);
            return null;
        }
        Logger.trace(false, "Health", "Failed to read psinfo file for pid: {} ", pid);
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
            try (ProcAddressSpaceReader reader = ProcAddressSpaceReader.open(LIBC, pid, PAGE_SIZE)) {
                if (reader == null) {
                    return Pair.of(args, env);
                }
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

                // Read the pointers to the arg strings.
                long[] argPtr = new long[argc];
                long argp = reader.readPointer(argv, increment);
                if (argp > 0) {
                    for (int i = 0; i < argc; i++) {
                        argPtr[i] = reader.readPointer(argp + i * increment, increment);
                    }
                }

                // Also read the pointers to the env strings.
                List<Long> envPtrList = new ArrayList<>();
                long addr = reader.readPointer(envp, increment);
                int limit = 500; // sane max env strings to stop at
                long offset = addr;
                while (addr != 0 && --limit > 0) {
                    long envPtr = reader.readPointer(offset, increment);
                    if (envPtr == 0) {
                        break;
                    }
                    envPtrList.add(envPtr);
                    offset += increment;
                }

                // Now read the arg strings.
                for (int i = 0; i < argPtr.length && argPtr[i] != 0; i++) {
                    String argStr = reader.readString(argPtr[i]);
                    if (!argStr.isEmpty()) {
                        args.add(argStr);
                    }
                }

                // And now read the env strings.
                for (Long envPtr : envPtrList) {
                    String envStr = reader.readString(envPtr);
                    int idx = envStr.indexOf(Symbol.C_EQUAL);
                    if (idx > 0) {
                        env.put(envStr.substring(0, idx), envStr.substring(idx + 1));
                    }
                }
            }
        }
        return Pair.of(args, env);
    }

}
