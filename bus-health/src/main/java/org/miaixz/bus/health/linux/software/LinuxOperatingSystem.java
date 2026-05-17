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
package org.miaixz.bus.health.linux.software;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Supplier;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import com.sun.jna.platform.linux.Udev;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.*;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.builtin.software.*;
import org.miaixz.bus.health.builtin.software.common.AbstractOperatingSystem;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.driver.Who;
import org.miaixz.bus.health.linux.driver.proc.Auxv;
import org.miaixz.bus.health.linux.driver.proc.CpuStat;
import org.miaixz.bus.health.linux.driver.proc.ProcessStat;
import org.miaixz.bus.health.linux.driver.proc.UpTime;
import org.miaixz.bus.health.linux.jna.LinuxLibc;
import org.miaixz.bus.logger.Logger;

/**
 * Linux is a family of open source Unix-like operating systems based on the Linux kernel, an operating system kernel
 * first released on September 17, 1991, by Linus Torvalds. Linux is typically packaged in a Linux distribution.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class LinuxOperatingSystem extends AbstractOperatingSystem {

    /**
     * This static field identifies if the udev library can be loaded.
     */
    public static final boolean HAS_UDEV;

    /**
     * This static field identifies if the gettid function is in the c library.
     */
    public static final boolean HAS_GETTID;

    /**
     * This static field identifies if the syscall for gettid returns sane results.
     */
    public static final boolean HAS_SYSCALL_GETTID;
    // Package private for access from LinuxOSProcess
    /**
     * The BOOTTIME constant.
     */
    static final long BOOTTIME;

    /**
     * The OS_RELEASE_LOG constant.
     */
    private static final String OS_RELEASE_LOG = "os-release: {}";

    /**
     * The LSB_RELEASE_A_LOG constant.
     */
    private static final String LSB_RELEASE_A_LOG = "lsb_release -a: {}";

    /**
     * The LSB_RELEASE_LOG constant.
     */
    private static final String LSB_RELEASE_LOG = "lsb-release: {}";

    /**
     * The RELEASE_DELIM constant.
     */
    private static final String RELEASE_DELIM = " release ";

    /**
     * The DOUBLE_QUOTES constant.
     */
    private static final String DOUBLE_QUOTES = "(?:^\")|(?:\"$)";

    /**
     * Jiffies per second, used for process time counters.
     */
    private static final long USER_HZ;

    /**
     * The PAGE_SIZE constant.
     */
    private static final long PAGE_SIZE;

    /**
     * OS Name for manufacturer
     */
    private static final String OS_NAME = Executor.getFirstAnswer("uname -o");
    // PPID is 4th numeric value in proc pid stat; subtract 1 for 0-index
    /**
     * The PPID_INDEX constant.
     */
    private static final int[] PPID_INDEX = { 3 };

    /**
     * The installedAppsSupplier value.
     */
    private final Supplier<List<ApplicationInfo>> installedAppsSupplier = Memoizer
            .memoize(LinuxInstalledApps::queryInstalledApps, Memoizer.installedAppsExpiration());

    /**
     * The cgroupInfoSupplier value.
     */
    private final Supplier<CgroupInfo> cgroupInfoSupplier = Memoizer.memoize(LinuxCgroupInfo::new);

    static {
        boolean hasUdev = false;
        boolean hasGettid = false;
        boolean hasSyscallGettid = false;
        try {
            if (Config.get(Config._LINUX_ALLOWUDEV, true)) {
                try {
                    Udev lib = Udev.INSTANCE;
                    hasUdev = true;
                } catch (UnsatisfiedLinkError e) {
                    Logger.warn(
                            false,
                            "Health",
                            "Did not find udev library in operating system. Some features may not work.");
                }
            } else {
                Logger.info(
                        false,
                        "Health",
                        "Loading of udev not allowed by configuration. Some features may not work.");
            }

            try {
                LinuxLibc.INSTANCE.gettid();
                hasGettid = true;
            } catch (UnsatisfiedLinkError e) {
                Logger.debug(false, "Health", "Did not find gettid function in operating system. Using fallbacks.");
            }

            hasSyscallGettid = hasGettid;
            if (!hasGettid) {
                try {
                    hasSyscallGettid = LinuxLibc.INSTANCE.syscall(LinuxLibc.SYS_GETTID).intValue() > 0;
                } catch (UnsatisfiedLinkError e) {
                    Logger.debug(
                            false,
                            "Health",
                            "Did not find working syscall gettid function in operating system. Using procfs");
                }
            }
        } catch (NoClassDefFoundError e) {
            Logger.error(
                    false,
                    "Health",
                    "Did not JNA classes. Investigate incompatible version or missing native dll.");
        }
        HAS_UDEV = hasUdev;
        HAS_GETTID = hasGettid;
        HAS_SYSCALL_GETTID = hasSyscallGettid;
    }

    static {
        long tempBT = CpuStat.getBootTime();
        // If above fails, current time minus uptime.
        if (tempBT == 0) {
            tempBT = System.currentTimeMillis() / 1000L - (long) UpTime.getSystemUptimeSeconds();
        }
        BOOTTIME = tempBT;
    }

    static {
        Map<Integer, Long> auxv = Auxv.queryAuxv();
        long hz = auxv.getOrDefault(Auxv.AT_CLKTCK, 0L);
        if (hz > 0) {
            USER_HZ = hz;
        } else {
            USER_HZ = Parsing.parseLongOrDefault(Executor.getFirstAnswer("getconf CLK_TCK"), 100L);
        }
        long pagesz = Auxv.queryAuxv().getOrDefault(Auxv.AT_PAGESZ, 0L);
        if (pagesz > 0) {
            PAGE_SIZE = pagesz;
        } else {
            PAGE_SIZE = Parsing.parseLongOrDefault(Executor.getFirstAnswer("getconf PAGE_SIZE"), 4096L);
        }
    }

    /**
     * <p>
     * Constructor for LinuxOperatingSystem.
     * </p>
     */
    public LinuxOperatingSystem() {
        super.getVersionInfo();
    }

    /**
     * Returns the parent pids from proc files.
     *
     * @param pidFiles the pid files
     * @return the get parent pids from proc files result
     */
    private static Map<Integer, Integer> getParentPidsFromProcFiles(File[] pidFiles) {
        Map<Integer, Integer> parentPidMap = new HashMap<>();
        for (File procFile : pidFiles) {
            int pid = Parsing.parseIntOrDefault(procFile.getName(), 0);
            parentPidMap.put(pid, getParentPidFromProcFile(pid));
        }
        return parentPidMap;
    }

    /**
     * Returns the parent pid from proc file.
     *
     * @param pid the pid
     * @return the get parent pid from proc file result
     */
    private static int getParentPidFromProcFile(int pid) {
        return getParentPidFromStat(Builder.getStringFromFile(String.format(Locale.ROOT, ProcPath.PID_STAT, pid)));
    }

    /**
     * Parse the parent PID from a {@code /proc/[pid]/stat} line.
     *
     * @param stat contents of {@code /proc/[pid]/stat}
     * @return the parent PID, or 0 if unparseable
     */
    static int getParentPidFromStat(String stat) {
        // A race condition may leave us with an empty string
        if (stat.isEmpty()) {
            return 0;
        }
        // Grab PPID
        long[] statArray = Parsing
                .parseStringToLongArray(stat, PPID_INDEX, ProcessStat.PROC_PID_STAT_LENGTH, Symbol.C_SPACE);
        return (int) statArray[0];
    }

    /**
     * Queries the family version codename from release files.
     *
     * @return the query family version codename from release files result
     */
    private static Triplet<String, String, String> queryFamilyVersionCodenameFromReleaseFiles() {
        Triplet<String, String, String> familyVersionCodename;
        // There are two competing options for family/version information.
        // Newer systems are adopting a standard /etc/os-release file:
        // https://www.freedesktop.org/software/systemd/man/os-release.html
        //
        // Some systems are still using the lsb standard which parses a
        // variety of /etc/*-release files and is most easily accessed via
        // the commandline lsb_release -a, see here:
        // https://linux.die.net/man/1/lsb_release
        // In this case, the /etc/lsb-release file (if it exists) has
        // optional overrides to the information in the /etc/distrib-release
        // files, which show: "Distributor release x.x (Codename)"

        // Attempt to read /etc/system-release which has more details than
        // os-release on (CentOS and Fedora)
        if ((familyVersionCodename = readDistribRelease("/etc/system-release")) != null) {
            // If successful, we're done. this.family has been set and
            // possibly the versionID and codeName
            return familyVersionCodename;
        }

        // Attempt to read /etc/os-release file.
        if ((familyVersionCodename = readOsRelease()) != null) {
            // If successful, we're done. this.family has been set and
            // possibly the versionID and codeName
            return familyVersionCodename;
        }

        // Attempt to execute the `lsb_release` command
        if ((familyVersionCodename = execLsbRelease()) != null) {
            // If successful, we're done. this.family has been set and
            // possibly the versionID and codeName
            return familyVersionCodename;
        }

        // The above two options should hopefully work on most
        // distributions. If not, we keep having fun.
        // Attempt to read /etc/lsb-release file
        if ((familyVersionCodename = readLsbRelease()) != null) {
            // If successful, we're done. this.family has been set and
            // possibly the versionID and codeName
            return familyVersionCodename;
        }

        // If we're still looking, we search for any /etc/*-release (or
        // similar) filename, for which the first line should be of the
        // "Distributor release x.x (Codename)" format or possibly a
        // "Distributor VERSION x.x (Codename)" format
        String etcDistribRelease = getReleaseFilename();
        if ((familyVersionCodename = readDistribRelease(etcDistribRelease)) != null) {
            // If successful, we're done. this.family has been set and
            // possibly the versionID and codeName
            return familyVersionCodename;
        }
        // If we've gotten this far with no match, use the distrib-release
        // filename (defaults will eventually give "Unknown")
        String family = filenameToFamily(
                etcDistribRelease.replace("/etc/", Normal.EMPTY).replace("release", Normal.EMPTY)
                        .replace("version", Normal.EMPTY).replace(Symbol.MINUS, Normal.EMPTY)
                        .replace(Symbol.UNDERLINE, Normal.EMPTY));
        return Triplet.of(family, Normal.UNKNOWN, Normal.UNKNOWN);
    }

    /**
     * Attempts to read /etc/os-release
     *
     * @return a triplet with the parsed family, versionID and codeName if file successfully read and NAME= found, null
     *         otherwise
     */
    private static Triplet<String, String, String> readOsRelease() {
        return readOsRelease(Builder.readFile("/etc/os-release"));
    }

    /**
     * Parse {@code /etc/os-release} content.
     *
     * @param osRelease lines from {@code /etc/os-release}
     * @return a triplet with the parsed family, versionID and codeName if NAME= found, null otherwise
     */
    static Triplet<String, String, String> readOsRelease(List<String> osRelease) {
        String family = null;
        String versionId = Normal.UNKNOWN;
        String codeName = Normal.UNKNOWN;
        // Search for NAME=
        for (String line : osRelease) {
            if (line.startsWith("VERSION=")) {
                Logger.debug(false, "Health", OS_RELEASE_LOG, line);
                // remove beginning and ending '"' characters, etc from
                // VERSION="14.04.4 LTS, Trusty Tahr" (Ubuntu style)
                // or VERSION="17 (Beefy Miracle)" (os-release doc style)
                line = line.replace("VERSION=", Normal.EMPTY).replaceAll(DOUBLE_QUOTES, Normal.EMPTY).trim();
                String[] split = line.split("[()]");
                if (split.length <= 1) {
                    // If no parentheses, check for Ubuntu's comma format
                    split = line.split(", ");
                }
                if (split.length > 0) {
                    versionId = split[0].trim();
                }
                if (split.length > 1) {
                    codeName = split[1].trim();
                }
            } else if (line.startsWith("NAME=") && family == null) {
                Logger.debug(false, "Health", OS_RELEASE_LOG, line);
                // remove beginning and ending '"' characters, etc from
                // NAME="Ubuntu"
                family = line.replace("NAME=", Normal.EMPTY).replaceAll(DOUBLE_QUOTES, Normal.EMPTY).trim();
            } else if (line.startsWith("VERSION_ID=") && versionId.equals(Normal.UNKNOWN)) {
                Logger.debug(false, "Health", OS_RELEASE_LOG, line);
                // remove beginning and ending '"' characters, etc from
                // VERSION_ID="14.04"
                versionId = line.replace("VERSION_ID=", Normal.EMPTY).replaceAll(DOUBLE_QUOTES, Normal.EMPTY).trim();
            }
        }
        return family == null ? null : Triplet.of(family, versionId, codeName);
    }

    /**
     * Attempts to execute `lsb_release -a`
     *
     * @return a triplet with the parsed family, versionID and codeName if the command successfully executed and
     *         Distributor ID: or Description: found, null otherwise
     */
    private static Triplet<String, String, String> execLsbRelease() {
        return execLsbRelease(Executor.runNative("lsb_release -a"));
    }

    /**
     * Parse {@code lsb_release -a} output.
     *
     * @param lines output of {@code lsb_release -a}
     * @return a triplet with the parsed family, versionID and codeName if Distributor ID: or Description: found, null
     *         otherwise
     */
    static Triplet<String, String, String> execLsbRelease(List<String> lines) {
        String family = null;
        String versionId = Normal.UNKNOWN;
        String codeName = Normal.UNKNOWN;
        // If description is of the format Distrib release x.x (Codename)
        // that is primary, otherwise use Distributor ID: which returns the
        // distribution concatenated, e.g., RedHat instead of Red Hat
        for (String line : lines) {
            if (line.startsWith("Description:")) {
                Logger.debug(false, "Health", LSB_RELEASE_A_LOG, line);
                line = line.replace("Description:", Normal.EMPTY).trim();
                if (line.contains(RELEASE_DELIM)) {
                    Triplet<String, String, String> triplet = parseRelease(line, RELEASE_DELIM);
                    family = triplet.getLeft();
                    if (versionId.equals(Normal.UNKNOWN)) {
                        versionId = triplet.getMiddle();
                    }
                    if (codeName.equals(Normal.UNKNOWN)) {
                        codeName = triplet.getRight();
                    }
                }
            } else if (line.startsWith("Distributor ID:") && family == null) {
                Logger.debug(false, "Health", LSB_RELEASE_A_LOG, line);
                family = line.replace("Distributor ID:", Normal.EMPTY).trim();
            } else if (line.startsWith("Release:") && versionId.equals(Normal.UNKNOWN)) {
                Logger.debug(false, "Health", LSB_RELEASE_A_LOG, line);
                versionId = line.replace("Release:", Normal.EMPTY).trim();
            } else if (line.startsWith("Codename:") && codeName.equals(Normal.UNKNOWN)) {
                Logger.debug(false, "Health", LSB_RELEASE_A_LOG, line);
                codeName = line.replace("Codename:", Normal.EMPTY).trim();
            }
        }
        return family == null ? null : Triplet.of(family, versionId, codeName);
    }

    /**
     * Attempts to read /etc/lsb-release
     *
     * @return a triplet with the parsed family, versionID and codeName if file successfully read and and DISTRIB_ID or
     *         DISTRIB_DESCRIPTION, null otherwise
     */
    private static Triplet<String, String, String> readLsbRelease() {
        return readLsbRelease(Builder.readFile("/etc/lsb-release"));
    }

    /**
     * Parse {@code /etc/lsb-release} content.
     *
     * @param lines lines from {@code /etc/lsb-release}
     * @return a triplet with the parsed family, versionID and codeName if DISTRIB_ID or DISTRIB_DESCRIPTION found, null
     *         otherwise
     */
    static Triplet<String, String, String> readLsbRelease(List<String> lines) {
        String family = null;
        String versionId = Normal.UNKNOWN;
        String codeName = Normal.UNKNOWN;
        // Search for NAME=
        for (String line : lines) {
            if (line.startsWith("DISTRIB_DESCRIPTION=")) {
                Logger.debug(false, "Health", LSB_RELEASE_LOG, line);
                line = line.replace("DISTRIB_DESCRIPTION=", Normal.EMPTY).replaceAll(DOUBLE_QUOTES, Normal.EMPTY)
                        .trim();
                if (line.contains(RELEASE_DELIM)) {
                    Triplet<String, String, String> triplet = parseRelease(line, RELEASE_DELIM);
                    family = triplet.getLeft();
                    if (versionId.equals(Normal.UNKNOWN)) {
                        versionId = triplet.getMiddle();
                    }
                    if (codeName.equals(Normal.UNKNOWN)) {
                        codeName = triplet.getRight();
                    }
                }
            } else if (line.startsWith("DISTRIB_ID=") && family == null) {
                Logger.debug(false, "Health", LSB_RELEASE_LOG, line);
                family = line.replace("DISTRIB_ID=", Normal.EMPTY).replaceAll(DOUBLE_QUOTES, Normal.EMPTY).trim();
            } else if (line.startsWith("DISTRIB_RELEASE=") && versionId.equals(Normal.UNKNOWN)) {
                Logger.debug(false, "Health", LSB_RELEASE_LOG, line);
                versionId = line.replace("DISTRIB_RELEASE=", Normal.EMPTY).replaceAll(DOUBLE_QUOTES, Normal.EMPTY)
                        .trim();
            } else if (line.startsWith("DISTRIB_CODENAME=") && codeName.equals(Normal.UNKNOWN)) {
                Logger.debug(false, "Health", LSB_RELEASE_LOG, line);
                codeName = line.replace("DISTRIB_CODENAME=", Normal.EMPTY).replaceAll(DOUBLE_QUOTES, Normal.EMPTY)
                        .trim();
            }
        }
        return family == null ? null : Triplet.of(family, versionId, codeName);
    }

    /**
     * Attempts to read /etc/distrib-release (for some value of distrib)
     *
     * @param filename The /etc/distrib-release file
     * @return a triplet with the parsed family, versionID and codeName if file successfully read and " release " or "
     *         VERSION " found, null otherwise
     */
    private static Triplet<String, String, String> readDistribRelease(String filename) {
        if (new File(filename).exists()) {
            List<String> osRelease = Builder.readFile(filename);
            for (String line : osRelease) {
                Logger.debug(false, "Health", "{}: {}", filename, line);
            }
            return readDistribRelease(osRelease);
        }
        return null;
    }

    /**
     * Parse distrib-release file content.
     *
     * @param lines lines from a distrib-release file
     * @return a triplet with the parsed family, versionID and codeName if " release " or " VERSION " found, null
     *         otherwise
     */
    static Triplet<String, String, String> readDistribRelease(List<String> lines) {
        for (String line : lines) {
            if (line.contains(RELEASE_DELIM)) {
                // If this parses properly we're done
                return parseRelease(line, RELEASE_DELIM);
            } else if (line.contains(" VERSION ")) {
                // If this parses properly we're done
                return parseRelease(line, " VERSION ");
            }
        }
        return null;
    }

    /**
     * Helper method to parse version description line style
     *
     * @param line      a String of the form "Distributor release x.x (Codename)"
     * @param splitLine A regex to split on, e.g. " release "
     * @return a triplet with the parsed family, versionID and codeName
     */
    static Triplet<String, String, String> parseRelease(String line, String splitLine) {
        String[] split = line.split(splitLine);
        String family = split[0].trim();
        String versionId = Normal.UNKNOWN;
        String codeName = Normal.UNKNOWN;
        if (split.length > 1) {
            split = split[1].split("[()]");
            if (split.length > 0) {
                versionId = split[0].trim();
            }
            if (split.length > 1) {
                codeName = split[1].trim();
            }
        }
        return Triplet.of(family, versionId, codeName);
    }

    /**
     * Converts a portion of a filename (e.g. the 'redhat' in /etc/redhat-release) to a mixed case string representing
     * the family (e.g., Red Hat)
     *
     * @param name Stripped version of filename after removing /etc and -release
     * @return Mixed case family
     */
    static String filenameToFamily(String name) {

        if (name.isEmpty()) {
            return "Solaris";
        } else if ("issue".equalsIgnoreCase(name)) {
            // /etc/issue will end up here
            return "Unknown";
        } else {
            Properties filenameProps = Config.readProperties(Config._LINUX_FILENAME_PROPERTIES);
            String family = filenameProps.getProperty(name.toLowerCase(Locale.ROOT));
            return family != null ? family : name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        }
    }

    /**
     * Gets Jiffies per second, useful for converting ticks to milliseconds and vice versa.
     *
     * @return Jiffies per second.
     */
    public static long getHz() {
        return USER_HZ;
    }

    /**
     * Gets Page Size, for converting memory stats from pages to bytes
     *
     * @return Page Size
     */
    public static long getPageSize() {
        return PAGE_SIZE;
    }

    /**
     * Looks for a collection of possible distrib-release filenames
     *
     * @return The first valid matching filename
     */
    protected static String getReleaseFilename() {
        // Look for any /etc/*-release, *-version, and variants
        File etc = new File("/etc");
        // Find any *_input files in that path
        File[] matchingFiles = etc.listFiles(//
                f -> (f.getName().endsWith("-release") || //
                        f.getName().endsWith("-version") || //
                        f.getName().endsWith("_release") || //
                        f.getName().endsWith("_version")) //
                        && !(f.getName().endsWith("os-release") || //
                                f.getName().endsWith("lsb-release") || //
                                f.getName().endsWith("system-release")));
        if (matchingFiles != null && matchingFiles.length > 0) {
            return matchingFiles[0].getPath();
        }
        if (new File("/etc/release").exists()) {
            return "/etc/release";
        }
        // If all else fails, try this
        return "/etc/issue";
    }

    /**
     * Queries the manufacturer.
     *
     * @return the query manufacturer result
     */
    @Override
    public String queryManufacturer() {
        return OS_NAME;
    }

    /**
     * Queries the bitness.
     *
     * @param jvmBitness the jvm bitness
     * @return the query bitness result
     */
    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness < 64 && !Executor.getFirstAnswer("uname -m").contains("64")) {
            return jvmBitness;
        }
        return 64;
    }

    /**
     * Returns the file system.
     *
     * @return the get file system result
     */
    @Override
    public FileSystem getFileSystem() {
        return new LinuxFileSystem();
    }

    /**
     * Returns the internet protocol stats.
     *
     * @return the get internet protocol stats result
     */
    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new LinuxInternetProtocolStats();
    }

    /**
     * Returns the sessions.
     *
     * @return the get sessions result
     */
    @Override
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    /**
     * Returns the process.
     *
     * @param pid the pid
     * @return the get process result
     */
    @Override
    public OSProcess getProcess(int pid) {
        OSProcess proc = new LinuxOSProcess(pid, this);
        if (!proc.getState().equals(OSProcess.State.INVALID)) {
            return proc;
        }
        return null;
    }

    /**
     * Queries the all processes.
     *
     * @return the query all processes result
     */
    @Override
    public List<OSProcess> queryAllProcesses() {
        return queryChildProcesses(-1);
    }

    /**
     * Queries the family version info.
     *
     * @return the query family version info result
     */
    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        Triplet<String, String, String> familyVersionCodename = queryFamilyVersionCodenameFromReleaseFiles();
        String buildNumber = null;
        List<String> procVersion = Builder.readFile(ProcPath.VERSION);
        if (!procVersion.isEmpty()) {
            String[] split = Pattern.SPACES_PATTERN.split(procVersion.get(0));
            for (String s : split) {
                if (!"Linux".equals(s) && !"version".equals(s)) {
                    buildNumber = s;
                    break;
                }
            }
        }
        OperatingSystem.OSVersionInfo versionInfo = new OperatingSystem.OSVersionInfo(familyVersionCodename.getMiddle(),
                familyVersionCodename.getRight(), buildNumber);
        return Pair.of(familyVersionCodename.getLeft(), versionInfo);
    }

    /**
     * Queries the descendant processes.
     *
     * @param parentPid the parent pid
     * @return the query descendant processes result
     */
    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        File[] pidFiles = ProcessStat.getPidFiles();
        return queryProcessList(getChildrenOrDescendants(getParentPidsFromProcFiles(pidFiles), parentPid, true));
    }

    /**
     * Queries the child processes.
     *
     * @param parentPid the parent pid
     * @return the query child processes result
     */
    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        File[] pidFiles = ProcessStat.getPidFiles();
        if (parentPid >= 0) {
            // Only return descendants
            return queryProcessList(getChildrenOrDescendants(getParentPidsFromProcFiles(pidFiles), parentPid, false));
        }
        Set<Integer> descendantPids = new HashSet<>();
        // Put everything in the "descendant" set
        for (File procFile : pidFiles) {
            int pid = Parsing.parseIntOrDefault(procFile.getName(), -2);
            if (pid != -2) {
                descendantPids.add(pid);
            }
        }
        return queryProcessList(descendantPids);
    }

    /**
     * Returns the process id.
     *
     * @return the get process id result
     */
    @Override
    public int getProcessId() {
        return LinuxLibc.INSTANCE.getpid();
    }

    /**
     * Returns the process count.
     *
     * @return the get process count result
     */
    @Override
    public int getProcessCount() {
        return ProcessStat.getPidFiles().length;
    }

    /**
     * Queries the process list.
     *
     * @param descendantPids the descendant pids
     * @return the query process list result
     */
    private List<OSProcess> queryProcessList(Set<Integer> descendantPids) {
        List<OSProcess> procs = new ArrayList<>();
        for (int pid : descendantPids) {
            OSProcess proc = new LinuxOSProcess(pid, this);
            if (!proc.getState().equals(OSProcess.State.INVALID)) {
                procs.add(proc);
            }
        }
        return procs;
    }

    /**
     * Returns the current thread.
     *
     * @return the get current thread result
     */
    @Override
    public OSThread getCurrentThread() {
        return new LinuxOSThread(getProcessId(), getThreadId());
    }

    /**
     * Returns the thread count.
     *
     * @return the get thread count result
     */
    @Override
    public int getThreadCount() {
        try (Struct.CloseableSysinfo info = new Struct.CloseableSysinfo()) {
            if (0 != LibC.INSTANCE.sysinfo(info)) {
                Logger.error(
                        false,
                        "Health",
                        "Failed to get process thread count. Error code: {}",
                        Native.getLastError());
                return 0;
            }
            return Short.toUnsignedInt(info.procs);
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            Logger.error(false, "Health", "Failed to get procs from sysinfo. {}", e.getClass().getSimpleName());
        }
        return 0;
    }

    /**
     * Returns the thread id.
     *
     * @return the get thread id result
     */
    @Override
    public int getThreadId() {
        if (HAS_SYSCALL_GETTID) {
            return HAS_GETTID ? LinuxLibc.INSTANCE.gettid()
                    : LinuxLibc.INSTANCE.syscall(LinuxLibc.SYS_GETTID).intValue();
        }
        try {
            return Parsing.parseIntOrDefault(
                    Files.readSymbolicLink(new File(ProcPath.THREAD_SELF).toPath()).getFileName().toString(),
                    0);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Returns the system uptime.
     *
     * @return the get system uptime result
     */
    @Override
    public long getSystemUptime() {
        return (long) UpTime.getSystemUptimeSeconds();
    }

    /**
     * Returns the system boot time.
     *
     * @return the get system boot time result
     */
    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    /**
     * Returns the network params.
     *
     * @return the get network params result
     */
    @Override
    public NetworkParams getNetworkParams() {
        return new LinuxNetworkParams();
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
        for (OSProcess p : getChildProcesses(
                1,
                OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.PID_ASC,
                0)) {
            OSService s = new OSService(p.getName(), p.getProcessID(), OSService.State.RUNNING);
            services.add(s);
            running.add(p.getName());
        }
        boolean systemctlFound = false;
        List<String> systemctl = Executor.runNative("systemctl list-unit-files");
        for (String text : systemctl) {
            String[] split = Pattern.SPACES_PATTERN.split(text);
            if (split.length >= 2 && split[0].endsWith(".service") && Normal.ENABLED.equals(split[1])) {
                // remove .service extension
                String name = split[0].substring(0, split[0].length() - 8);
                int index = name.lastIndexOf('.');
                String shortName = (index < 0 || index > name.length() - 2) ? name : name.substring(index + 1);
                if (!running.contains(name) && !running.contains(shortName)) {
                    OSService s = new OSService(name, 0, OSService.State.STOPPED);
                    services.add(s);
                    systemctlFound = true;
                }
            }
        }
        if (!systemctlFound) {
            // Get Directories for stopped services
            File dir = new File("/etc/init");
            if (dir.exists() && dir.isDirectory()) {
                for (File f : dir.listFiles((f, name) -> name.toLowerCase(Locale.ROOT).endsWith(".conf"))) {
                    // remove .conf extension
                    String name = f.getName().substring(0, f.getName().length() - 5);
                    int index = name.lastIndexOf('.');
                    String shortName = (index < 0 || index > name.length() - 2) ? name : name.substring(index + 1);
                    if (!running.contains(name) && !running.contains(shortName)) {
                        OSService s = new OSService(name, 0, OSService.State.STOPPED);
                        services.add(s);
                    }
                }
            } else {
                Logger.error(false, "Health", "Directory: /etc/init does not exist");
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

    /**
     * Description inherited from parent class or interface.
     *
     * @return the Linux cgroup information
     */
    @Override
    public CgroupInfo getCgroupInfo() {
        return cgroupInfoSupplier.get();
    }

}
