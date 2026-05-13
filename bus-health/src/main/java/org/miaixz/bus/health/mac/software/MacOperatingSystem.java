/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.mac.software;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.ApplicationInfo;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OSService;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOperatingSystem;
import org.miaixz.bus.logger.Logger;

/**
 * macOS, previously Mac OS X and later OS X) is a series of proprietary graphical operating systems developed and
 * marketed by Apple Inc. since 2001. It is the primary operating system for Apple's Mac computers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class MacOperatingSystem extends AbstractOperatingSystem {

    /**
     * The SYSTEM_LIBRARY_LAUNCH_AGENTS constant.
     */
    private static final String SYSTEM_LIBRARY_LAUNCH_AGENTS = "/System/Library/LaunchAgents";

    /**
     * The SYSTEM_LIBRARY_LAUNCH_DAEMONS constant.
     */
    private static final String SYSTEM_LIBRARY_LAUNCH_DAEMONS = "/System/Library/LaunchDaemons";

    /**
     * The maxProc value.
     */
    protected final int maxProc;

    /**
     * The osXVersion value.
     */
    protected final String osXVersion;

    /**
     * The major value.
     */
    protected final int major;

    /**
     * The minor value.
     */
    protected final int minor;

    /**
     * The installedAppsSupplier value.
     */
    private final Supplier<List<ApplicationInfo>> installedAppsSupplier = Memoizer
            .memoize(MacInstalledApps::queryInstalledApps, Memoizer.installedAppsExpiration());

    /**
     * Creates a new MacOperatingSystem instance.
     *
     * @param maxproc the maxproc
     */
    protected MacOperatingSystem(int maxproc) {
        String version = System.getProperty("os.version");
        int verMajor = Parsing.getFirstIntValue(version);
        int verMinor = Parsing.getNthIntValue(version, 2);
        // Big Sur (11.x) may return 10.16
        if (verMajor == 10 && verMinor > 15) {
            String swVers = Executor.getFirstAnswer("sw_vers -productVersion");
            version = resolveVersion(version, swVers);
            verMajor = Parsing.getFirstIntValue(version);
            verMinor = Parsing.getNthIntValue(version, 2);
        }
        this.osXVersion = version;
        this.major = verMajor;
        this.minor = verMinor;
        this.maxProc = maxproc;
    }

    /**
     * Resolves the macOS version by preferring the sw_vers command output when available.
     *
     * @param osVersion    The version reported by the JVM system property.
     * @param swVersOutput The version reported by sw_vers.
     * @return The resolved macOS version.
     */
    static String resolveVersion(String osVersion, String swVersOutput) {
        if (!swVersOutput.isEmpty()) {
            return swVersOutput;
        }
        return osVersion;
    }

    /**
     * Queries the manufacturer.
     *
     * @return the query manufacturer result
     */
    @Override
    public String queryManufacturer() {
        return "Apple";
    }

    /**
     * Parses the code name.
     *
     * @return the parse code name result
     */
    protected String parseCodeName() {
        Properties verProps = Config.readProperties(Config._MACOS_VERSIONS_PROPERTIES);
        String codeName = null;
        if (this.major > 10) {
            codeName = verProps.getProperty(Integer.toString(this.major));
        } else if (this.major == 10) {
            codeName = verProps.getProperty(this.major + "." + this.minor);
        }
        if (StringKit.isBlank(codeName)) {
            Logger.warn(false, "Health", "Unable to parse version {}.{} to a codename.", this.major, this.minor);
        }
        return codeName;
    }

    /**
     * Queries the bitness.
     *
     * @param jvmBitness the jvm bitness
     * @return the query bitness result
     */
    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness == 64 || (this.major == 10 && this.minor > 6)) {
            return 64;
        }
        return Parsing.parseIntOrDefault(Executor.getFirstAnswer("getconf LONG_BIT"), 32);
    }

    /**
     * Queries the child processes.
     *
     * @param parentPid the parent pid
     * @return the query child processes result
     */
    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        List<OSProcess> allProcs = queryAllProcesses();
        Set<Integer> descendantPids = getChildrenOrDescendants(allProcs, parentPid, false);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    /**
     * Queries the descendant processes.
     *
     * @param parentPid the parent pid
     * @return the query descendant processes result
     */
    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        List<OSProcess> allProcs = queryAllProcesses();
        Set<Integer> descendantPids = getChildrenOrDescendants(allProcs, parentPid, true);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    /**
     * Returns the thread id.
     *
     * @return the get thread id result
     */
    @Override
    public int getThreadId() {
        OSThread thread = getCurrentThread();
        if (thread == null) {
            return 0;
        }
        return thread.getThreadId();
    }

    /**
     * Returns the current thread.
     *
     * @return the get current thread result
     */
    @Override
    public OSThread getCurrentThread() {
        // Get oldest thread
        return getCurrentProcess().getThreadDetails().stream().sorted(Comparator.comparingLong(OSThread::getStartTime))
                .findFirst().orElse(new MacOSThread(getProcessId()));
    }

    /**
     * Returns the system uptime.
     *
     * @return the get system uptime result
     */
    @Override
    public long getSystemUptime() {
        return System.currentTimeMillis() / 1000 - getSystemBootTime();
    }

    /**
     * Returns the services.
     *
     * @return the get services result
     */
    @Override
    public List<OSService> getServices() {
        // Get running services
        List<OSService> services = new ArrayList<>();
        Set<String> running = new HashSet<>();
        for (OSProcess p : getChildProcesses(1, ProcessFiltering.ALL_PROCESSES, ProcessSorting.PID_ASC, 0)) {
            OSService s = new OSService(p.getName(), p.getProcessID(), OSService.State.RUNNING);
            services.add(s);
            running.add(p.getName());
        }
        // Get Directories for stopped services
        ArrayList<File> files = new ArrayList<>();
        File dir = new File(SYSTEM_LIBRARY_LAUNCH_AGENTS);
        if (dir.exists() && dir.isDirectory()) {
            files.addAll(Arrays.asList(dir.listFiles((f, name) -> name.toLowerCase(Locale.ROOT).endsWith(".plist"))));
        } else {
            Logger.error(false, "Health", "Directory: /System/Library/LaunchAgents does not exist");
        }
        dir = new File(SYSTEM_LIBRARY_LAUNCH_DAEMONS);
        if (dir.exists() && dir.isDirectory()) {
            files.addAll(Arrays.asList(dir.listFiles((f, name) -> name.toLowerCase(Locale.ROOT).endsWith(".plist"))));
        } else {
            Logger.error(false, "Health", "Directory: /System/Library/LaunchDaemons does not exist");
        }
        for (File f : files) {
            // remove .plist extension
            String name = f.getName().substring(0, f.getName().length() - 6);
            int index = name.lastIndexOf('.');
            String shortName = (index < 0 || index > name.length() - 2) ? name : name.substring(index + 1);
            if (!running.contains(name) && !running.contains(shortName)) {
                OSService s = new OSService(name, 0, OSService.State.STOPPED);
                services.add(s);
            }
        }
        return services;
    }

    /**
     * Returns the installed applications.
     *
     * @return the get installed applications result
     */
    @Override
    public List<ApplicationInfo> getInstalledApplications() {
        return installedAppsSupplier.get();
    }

}
