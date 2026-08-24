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
package org.miaixz.bus.health.unix.solaris.driver;

import java.util.*;

import com.sun.jna.Pointer;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.unix.shared.driver.ProcAddressSpaceReader;
import org.miaixz.bus.health.unix.shared.jna.SolarisLibc;
import org.miaixz.bus.logger.Logger;

/**
 * Queries /proc/psinfo
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class PsInfo {

    /**
     * The LIBC constant.
     */
    private static final SolarisLibc LIBC = SolarisLibc.INSTANCE;

    /**
     * The PAGE_SIZE constant.
     */
    private static final long PAGE_SIZE = Parsing.parseLongOrDefault(Executor.getFirstAnswer("pagesize"), 4096L);

    /**
     * Keeps Solaris process-information queries on the static API.
     */
    public PsInfo() {
        // No initialization required.
    }

    /**
     * Reads /proc/pid/psinfo and returns data in a structure
     *
     * @param pid The process ID
     * @return A structure containing information for the requested process
     */
    public static SolarisLibc.SolarisPsInfo queryPsInfo(int pid) {
        return new SolarisLibc.SolarisPsInfo(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/psinfo", pid)));
    }

    /**
     * Reads /proc/pid/lwp/tid/lwpsinfo and returns data in a structure
     *
     * @param pid The process ID
     * @param tid The thread ID (lwpid)
     * @return A structure containing information for the requested thread
     */
    public static SolarisLibc.SolarisLwpsInfo queryLwpsInfo(int pid, int tid) {
        return new SolarisLibc.SolarisLwpsInfo(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/lwp/%d/lwpsinfo", pid, tid)));
    }

    /**
     * Reads /proc/pid/usage and returns data in a structure
     *
     * @param pid The process ID
     * @return A structure containing information for the requested process
     */
    public static SolarisLibc.SolarisPrUsage queryPrUsage(int pid) {
        return new SolarisLibc.SolarisPrUsage(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/usage", pid)));
    }

    /**
     * Reads /proc/pid/lwp/tid/usage and returns data in a structure
     *
     * @param pid The process ID
     * @param tid The thread ID (lwpid)
     * @return A structure containing information for the requested thread
     */
    public static SolarisLibc.SolarisPrUsage queryPrUsage(int pid, int tid) {
        return new SolarisLibc.SolarisPrUsage(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/lwp/%d/usage", pid, tid)));
    }

    /**
     * Reads the pr_argc, pr_argv, pr_envp, and pr_dmodel fields from /proc/pid/psinfo
     *
     * @param pid    The process ID
     * @param psinfo A populated {@link SolarisLibc.SolarisPsInfo} structure containing the offset pointers for these
     *               fields
     * @return A tuple containing the argc, argv, envp and dmodel values, or null if unable to read
     */
    public static Tuple queryArgsEnvAddrs(int pid, SolarisLibc.SolarisPsInfo psinfo) {
        if (psinfo != null) {
            int argc = psinfo.pr_argc;
            // Must have at least one argc (the command itself) so failure here means exit
            if (argc > 0) {
                long argv = Pointer.nativeValue(psinfo.pr_argv);
                long envp = Pointer.nativeValue(psinfo.pr_envp);
                // Process data model 1 = 32 bit, 2 = 64 bit
                byte dmodel = psinfo.pr_dmodel;
                // Sanity check
                if (dmodel * 4 == (envp - argv) / (argc + 1)) {
                    return new Tuple(argc, argv, envp, dmodel);
                }
                Logger.trace(
                        false,
                        "Health",
                        "Failed data model and offset increment sanity check: dm={} diff={}",
                        dmodel,
                        envp - argv);
                return null;
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
     * @param psinfo A populated {@link SolarisLibc.SolarisPsInfo} structure containing the offset pointers for these
     *               fields
     * @return A pair containing a list of the arguments and a map of environment variables
     */
    public static Pair<List<String>, Map<String, String>> queryArgsEnv(int pid, SolarisLibc.SolarisPsInfo psinfo) {
        List<String> args = new ArrayList<>();
        Map<String, String> env = new LinkedHashMap<>();

        // Get the arg count and list of env vars
        Tuple addrs = queryArgsEnvAddrs(pid, psinfo);
        if (addrs != null) {
            try (ProcAddressSpaceReader reader = ProcAddressSpaceReader.open(LIBC, pid, PAGE_SIZE)) {
                if (reader == null) {
                    return Pair.of(args, env);
                }
                // Non-null addrs means argc > 0
                int argc = addrs.get(0);
                long argv = addrs.get(1);
                long envp = addrs.get(2);
                long increment = ((byte) addrs.get(3)) * 4L;

                // Read the pointers to the arg strings.
                long[] argp = new long[argc];
                long offset = argv;
                for (int i = 0; i < argc; i++) {
                    argp[i] = reader.readPointer(offset, increment);
                    offset += increment;
                }

                // Also read the pointers to the env strings.
                List<Long> envPtrList = new ArrayList<>();
                offset = envp;
                long addr;
                int limit = 500; // sane max env strings to stop at
                do {
                    addr = reader.readPointer(offset, increment);
                    if (addr != 0) {
                        envPtrList.add(addr);
                    }
                    offset += increment;
                } while (addr != 0 && --limit > 0);

                // Now read the arg strings.
                for (int i = 0; i < argp.length && argp[i] != 0; i++) {
                    String argStr = reader.readString(argp[i]);
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
