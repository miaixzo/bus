/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.health.software.unix.freebsd;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.common.unix.CLibrary;
import org.aoju.bus.health.common.unix.freebsd.BsdSysctlUtils;
import org.aoju.bus.health.common.unix.freebsd.FreeBsdLibc;
import org.aoju.bus.health.software.*;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.util.*;

/**
 * <p>
 * FreeBsdOperatingSystem class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.8.5
 * @since JDK 1.8+
 */
public class FreeBsdOS extends AbstractOS {

    private static final long BOOTTIME = querySystemBootTime();

    /**
     * <p>
     * Constructor for FreeBsdOperatingSystem.
     * </p>
     */
    public FreeBsdOS() {

    }

    private static long querySystemBootTime() {
        CLibrary.Timeval tv = new CLibrary.Timeval();
        if (!BsdSysctlUtils.sysctl("kern.boottime", tv) || tv.tv_sec == 0) {
            // Usually this works. If it doesn't, fall back to text parsing.
            // Boot time will be the first consecutive string of digits.
            return Builder.parseLongOrDefault(
                    Command.getFirstAnswer("sysctl -n kern.boottime").split(Symbol.COMMA)[0].replaceAll("\\D", ""),
                    System.currentTimeMillis() / 1000);
        }
        // tv now points to a 128-bit timeval structure for boot time.
        // First 8 bytes are seconds, second 8 bytes are microseconds (we ignore)
        return tv.tv_sec;
    }

    @Override
    public String queryManufacturer() {
        return "Unix/BSD";
    }

    @Override
    public FamilyVersionInfo queryFamilyVersionInfo() {
        String family = BsdSysctlUtils.sysctl("kern.ostype", "FreeBSD");

        String version = BsdSysctlUtils.sysctl("kern.osrelease", "");
        String versionInfo = BsdSysctlUtils.sysctl("kern.version", "");
        String buildNumber = versionInfo.split(Symbol.COLON)[0].replace(family, "").replace(version, "").trim();

        return new FamilyVersionInfo(family, new OSVersionInfo(version, null, buildNumber));
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness < 64 && Command.getFirstAnswer("uname -m").indexOf("64") == -1) {
            return jvmBitness;
        }
        return 64;
    }

    @Override
    protected boolean queryElevated() {
        return System.getenv("SUDO_COMMAND") != null;
    }

    @Override
    public FileSystem getFileSystem() {
        return new FreeBsdFileSystem();
    }

    @Override
    public OSProcess[] getProcesses(int limit, ProcessSort sort, boolean slowFields) {
        List<OSProcess> procs = getProcessListFromPS(
                "ps -awwxo state,pid,ppid,user,uid,group,gid,nlwp,pri,vsz,rss,etimes,systime,time,comm,args", -1,
                slowFields);
        List<OSProcess> sorted = processSort(procs, limit, sort);
        return sorted.toArray(new OSProcess[0]);
    }

    @Override
    public OSProcess getProcess(int pid, boolean slowFields) {
        List<OSProcess> procs = getProcessListFromPS(
                "ps -awwxo state,pid,ppid,user,uid,group,gid,nlwp,pri,vsz,rss,etimes,systime,time,comm,args -p ", pid,
                slowFields);
        if (procs.isEmpty()) {
            return null;
        }
        return procs.get(0);
    }

    @Override
    public OSProcess[] getChildProcesses(int parentPid, int limit, ProcessSort sort) {
        List<OSProcess> procs = new ArrayList<>();
        for (OSProcess p : getProcesses(0, null)) {
            if (p.getParentProcessID() == parentPid) {
                procs.add(p);
            }
        }
        List<OSProcess> sorted = processSort(procs, limit, sort);
        return sorted.toArray(new OSProcess[0]);
    }

    private List<OSProcess> getProcessListFromPS(String psCommand, int pid, boolean slowFields) {
        Map<Integer, String> cwdMap = Builder.getCwdMap(pid);
        List<OSProcess> procs = new ArrayList<>();
        List<String> procList = Command.runNative(psCommand + (pid < 0 ? "" : pid));
        if (procList.isEmpty() || procList.size() < 2) {
            return procs;
        }
        // remove header row
        procList.remove(0);
        // Fill list
        for (String proc : procList) {
            String[] split = Builder.whitespaces.split(proc.trim(), 16);
            // Elements should match ps command order
            if (split.length < 16) {
                continue;
            }
            long now = System.currentTimeMillis();
            OSProcess fproc = new OSProcess(this);
            switch (split[0].charAt(0)) {
                case 'R':
                    fproc.setState(OSProcess.State.RUNNING);
                    break;
                case 'I':
                case 'S':
                    fproc.setState(OSProcess.State.SLEEPING);
                    break;
                case 'D':
                case 'L':
                case 'U':
                    fproc.setState(OSProcess.State.WAITING);
                    break;
                case 'Z':
                    fproc.setState(OSProcess.State.ZOMBIE);
                    break;
                case 'T':
                    fproc.setState(OSProcess.State.STOPPED);
                    break;
                default:
                    fproc.setState(OSProcess.State.OTHER);
                    break;
            }
            fproc.setProcessID(Builder.parseIntOrDefault(split[1], 0));
            fproc.setParentProcessID(Builder.parseIntOrDefault(split[2], 0));
            fproc.setUser(split[3]);
            fproc.setUserID(split[4]);
            fproc.setGroup(split[5]);
            fproc.setGroupID(split[6]);
            fproc.setThreadCount(Builder.parseIntOrDefault(split[7], 0));
            fproc.setPriority(Builder.parseIntOrDefault(split[8], 0));
            // These are in KB, multiply
            fproc.setVirtualSize(Builder.parseLongOrDefault(split[9], 0) * 1024);
            fproc.setResidentSetSize(Builder.parseLongOrDefault(split[10], 0) * 1024);
            // Avoid divide by zero for processes up less than a second
            long elapsedTime = Builder.parseDHMSOrDefault(split[11], 0L);
            fproc.setUpTime(elapsedTime < 1L ? 1L : elapsedTime);
            fproc.setStartTime(now - fproc.getUpTime());
            fproc.setKernelTime(Builder.parseDHMSOrDefault(split[12], 0L));
            fproc.setUserTime(Builder.parseDHMSOrDefault(split[13], 0L) - fproc.getKernelTime());
            fproc.setPath(split[14]);
            fproc.setName(fproc.getPath().substring(fproc.getPath().lastIndexOf(Symbol.C_SLASH) + 1));
            fproc.setCommandLine(split[15]);
            fproc.setCurrentWorkingDirectory(cwdMap.getOrDefault(fproc.getProcessID(), ""));

            if (slowFields) {
                List<String> openFilesList = Command.runNative(String.format("lsof -p %d", pid));
                fproc.setOpenFiles(openFilesList.size() - 1L);

                // Get process abi vector
                int[] mib = new int[4];
                mib[0] = 1; // CTL_KERN
                mib[1] = 14; // KERN_PROC
                mib[2] = 9; // KERN_PROC_SV_NAME
                mib[3] = pid;
                // Allocate memory for arguments
                Pointer abi = new Memory(32);
                IntByReference size = new IntByReference(32);
                // Fetch abi vector
                if (0 == FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, abi, size, null, 0)) {
                    String elf = abi.getString(0);
                    if (elf.contains("ELF32")) {
                        fproc.setBitness(32);
                    } else if (elf.contains("ELF64")) {
                        fproc.setBitness(64);
                    }
                }
            }
            procs.add(fproc);
        }
        return procs;
    }

    @Override
    public long getProcessAffinityMask(int processId) {
        long bitMask = 0L;
        // Would prefer to use native cpuset_getaffinity call but variable sizing is
        // kernel-dependent and requires C macros, so we use commandline instead.
        String cpuset = Command.getFirstAnswer("cpuset -gp " + processId);
        // Sample output:
        // pid 8 mask: 0, 1
        // cpuset: getaffinity: No such process
        String[] split = cpuset.split(Symbol.COLON);
        if (split.length > 1) {
            String[] bits = split[1].split(Symbol.COMMA);
            for (String bit : bits) {
                int bitToSet = Builder.parseIntOrDefault(bit.trim(), -1);
                if (bitToSet >= 0) {
                    bitMask |= 1L << bitToSet;
                }
            }
        }
        return bitMask;
    }

    @Override
    public int getProcessId() {
        return FreeBsdLibc.INSTANCE.getpid();
    }

    @Override
    public int getProcessCount() {
        List<String> procList = Command.runNative("ps -axo pid");
        if (!procList.isEmpty()) {
            // Subtract 1 for header
            return procList.size() - 1;
        }
        return 0;
    }

    @Override
    public int getThreadCount() {
        int threads = 0;
        for (String proc : Command.runNative("ps -axo nlwp")) {
            threads += Builder.parseIntOrDefault(proc.trim(), 0);
        }
        return threads;
    }

    @Override
    public long getSystemUptime() {
        return System.currentTimeMillis() / 1000 - BOOTTIME;
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new FreeBsdNetwork();
    }

    @Override
    public OSService[] getServices() {
        // Get running services
        List<OSService> services = new ArrayList<>();
        Set<String> running = new HashSet<>();
        for (OSProcess p : getChildProcesses(1, 0, ProcessSort.PID)) {
            OSService s = new OSService(p.getName(), p.getProcessID(), OSService.State.RUNNING);
            services.add(s);
            running.add(p.getName());
        }
        // Get Directories for stopped services
        File dir = new File("/etc/rc.d");
        File[] listFiles;
        if (dir.exists() && dir.isDirectory() && (listFiles = dir.listFiles()) != null) {
            for (File f : listFiles) {
                String name = f.getName();
                if (!running.contains(name)) {
                    OSService s = new OSService(name, 0, OSService.State.STOPPED);
                    services.add(s);
                }
            }
        } else {
            Logger.error("Directory: /etc/init does not exist");
        }
        return services.toArray(new OSService[0]);
    }
}
