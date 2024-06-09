/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.platform.solaris.software;

import com.sun.jna.platform.unix.solaris.Kstat2;
import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.*;
import org.miaixz.bus.health.builtin.software.common.AbstractOperatingSystem;
import org.miaixz.bus.health.linux.driver.proc.ProcessStat;
import org.miaixz.bus.health.unix.jna.SolarisLibc;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit.KstatChain;
import org.miaixz.bus.health.unix.platform.solaris.driver.Who;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Solaris is a non-free Unix operating system originally developed by Sun Microsystems. It superseded the company's
 * earlier SunOS in 1993. In 2010, after the Sun acquisition by Oracle, it was renamed Oracle Solaris.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class SolarisOperatingSystem extends AbstractOperatingSystem {

    /**
     * This static field identifies if the kstat2 library (available in Solaris 11.4 or greater) can be loaded.
     */
    public static final boolean HAS_KSTAT2;
    private static final String VERSION;
    private static final String BUILD_NUMBER;
    private static final boolean ALLOW_KSTAT2 = Config.get(Config._UNIX_SOLARIS_ALLOWKSTAT2, true);
    private static final Supplier<Pair<Long, Long>> BOOT_UPTIME = Memoizer
            .memoize(SolarisOperatingSystem::queryBootAndUptime, Memoizer.defaultExpiration());
    private static final long BOOTTIME = querySystemBootTime();

    static {
        String[] split = Pattern.SPACES_PATTERN.split(Executor.getFirstAnswer("uname -rv"));
        VERSION = split[0];
        BUILD_NUMBER = split.length > 1 ? split[1] : Normal.EMPTY;
    }

    static {
        Kstat2 lib = null;
        try {
            if (ALLOW_KSTAT2) {
                lib = Kstat2.INSTANCE;
            }
        } catch (UnsatisfiedLinkError e) {
            // 11.3 or earlier, no kstat2
        }
        HAS_KSTAT2 = lib != null;
    }

    private static long querySystemUptime() {
        if (HAS_KSTAT2) {
            // Use Kstat2 implementation
            return BOOT_UPTIME.get().getRight();
        }
        try (KstatChain kc = KstatKit.openChain()) {
            Kstat ksp = kc.lookup("unix", 0, "system_misc");
            if (ksp != null && kc.read(ksp)) {
                // Snap Time is in nanoseconds; divide for seconds
                return ksp.ks_snaptime / 1_000_000_000L;
            }
        }
        return 0L;
    }

    private static long querySystemBootTime() {
        if (HAS_KSTAT2) {
            // Use Kstat2 implementation
            return BOOT_UPTIME.get().getLeft();
        }
        try (KstatChain kc = KstatKit.openChain()) {
            Kstat ksp = kc.lookup("unix", 0, "system_misc");
            if (ksp != null && kc.read(ksp)) {
                return KstatKit.dataLookupLong(ksp, "boot_time");
            }
        }
        return System.currentTimeMillis() / 1000L - querySystemUptime();
    }

    private static Pair<Long, Long> queryBootAndUptime() {
        Object[] results = KstatKit.queryKstat2("/misc/unix/system_misc", "boot_time", "snaptime");

        long boot = results[0] == null ? System.currentTimeMillis() : (long) results[0];
        // Snap Time is in nanoseconds; divide for seconds
        long snap = results[1] == null ? 0L : (long) results[1] / 1_000_000_000L;

        return Pair.of(boot, snap);
    }

    @Override
    public FileSystem getFileSystem() {
        return new SolarisFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new SolarisInternetProtocolStats();
    }

    @Override
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    @Override
    public OSProcess getProcess(int pid) {
        List<OSProcess> procs = getProcessListFromProcfs(pid);
        if (procs.isEmpty()) {
            return null;
        }
        return procs.get(0);
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        return queryAllProcessesFromPrStat();
    }

    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        List<OSProcess> allProcs = queryAllProcessesFromPrStat();
        Set<Integer> descendantPids = getChildrenOrDescendants(allProcs, parentPid, false);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        List<OSProcess> allProcs = queryAllProcessesFromPrStat();
        Set<Integer> descendantPids = getChildrenOrDescendants(allProcs, parentPid, true);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public String queryManufacturer() {
        return "Oracle";
    }

    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        return Pair.of("SunOS", new OperatingSystem.OSVersionInfo(VERSION, "Solaris", BUILD_NUMBER));
    }

    @Override
    public int getProcessId() {
        return SolarisLibc.INSTANCE.getpid();
    }

    @Override
    public int getProcessCount() {
        return ProcessStat.getPidFiles().length;
    }

    @Override
    public int getThreadId() {
        return SolarisLibc.INSTANCE.thr_self();
    }

    @Override
    public OSThread getCurrentThread() {
        return new SolarisOSThread(getProcessId(), getThreadId());
    }

    @Override
    public int getThreadCount() {
        List<String> threadList = Executor.runNative("ps -eLo pid");
        if (!threadList.isEmpty()) {
            // Subtract 1 for header
            return threadList.size() - 1;
        }
        return getProcessCount();
    }

    @Override
    public long getSystemUptime() {
        return querySystemUptime();
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness == 64) {
            return 64;
        }
        return Parsing.parseIntOrDefault(Executor.getFirstAnswer("isainfo -b"), 32);
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    private List<OSProcess> queryAllProcessesFromPrStat() {
        return getProcessListFromProcfs(-1);
    }

    private List<OSProcess> getProcessListFromProcfs(int pid) {
        List<OSProcess> procs = new ArrayList<>();

        File[] numericFiles = null;
        if (pid < 0) {
            // If no pid, get process files in proc
            File directory = new File("/proc");
            numericFiles = directory.listFiles(file -> Pattern.NUMBERS_PATTERN.matcher(file.getName()).matches());
        } else {
            // If pid specified just find that file
            File pidFile = new File("/proc/" + pid);
            if (pidFile.exists()) {
                numericFiles = new File[1];
                numericFiles[0] = pidFile;
            }
        }
        if (numericFiles == null) {
            return procs;
        }

        // Iterate files
        for (File pidFile : numericFiles) {
            int pidNum = Parsing.parseIntOrDefault(pidFile.getName(), 0);
            OSProcess proc = new SolarisOSProcess(pidNum, this);
            if (proc.getState() != OSProcess.State.INVALID) {
                procs.add(proc);
            }
        }
        return procs;
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new SolarisNetworkParams();
    }

    @Override
    public List<OSService> getServices() {
        List<OSService> services = new ArrayList<>();
        // Get legacy RC service name possibilities
        List<String> legacySvcs = new ArrayList<>();
        File dir = new File("/etc/init.d");
        File[] listFiles;
        if (dir.exists() && dir.isDirectory() && (listFiles = dir.listFiles()) != null) {
            for (File f : listFiles) {
                legacySvcs.add(f.getName());
            }
        }
        // Iterate service list
        List<String> svcs = Executor.runNative("svcs -p");
        /*-
         Output:
         STATE          STIME    FRMI
         legacy_run     23:56:49 lrc:/etc/rc2_d/S47pppd
         legacy_run     23:56:49 lrc:/etc/rc2_d/S81dodatadm_udaplt
         legacy_run     23:56:49 lrc:/etc/rc2_d/S89PRESERVE
         online         23:56:25 svc:/system/early-manifest-import:default
         online         23:56:25 svc:/system/svc/restarter:default
                        23:56:24       13 svc.startd
                        ...
         */
        for (String line : svcs) {
            if (line.startsWith("online")) {
                int delim = line.lastIndexOf(":/");
                if (delim > 0) {
                    String name = line.substring(delim + 1);
                    if (name.endsWith(":default")) {
                        name = name.substring(0, name.length() - 8);
                    }
                    services.add(new OSService(name, 0, OSService.State.STOPPED));
                }
            } else if (line.startsWith(Symbol.SPACE)) {
                String[] split = Pattern.SPACES_PATTERN.split(line.trim());
                if (split.length == 3) {
                    services.add(new OSService(split[2], Parsing.parseIntOrDefault(split[1], 0), OSService.State.RUNNING));
                }
            } else if (line.startsWith("legacy_run")) {
                for (String svc : legacySvcs) {
                    if (line.endsWith(svc)) {
                        services.add(new OSService(svc, 0, OSService.State.STOPPED));
                        break;
                    }
                }
            }
        }
        return services;
    }

}
