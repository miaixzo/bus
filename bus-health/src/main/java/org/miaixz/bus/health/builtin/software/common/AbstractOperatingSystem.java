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
package org.miaixz.bus.health.builtin.software.common;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Platform;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OperatingSystem;

/**
 * Common methods for OperatingSystem implementations
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractOperatingSystem implements OperatingSystem {

    /**
     * Constructs a new AbstractOperatingSystem instance.
     */
    public AbstractOperatingSystem() {
        // No initialization required.
    }

    /**
     * The USE_WHO_COMMAND constant.
     */
    protected static final boolean USE_WHO_COMMAND = Config.get(Config._UNIX_WHOCOMMAND, false);

    /**
     * The manufacturer value.
     */
    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);

    /**
     * The familyVersionInfo value.
     */
    private final Supplier<Pair<String, OSVersionInfo>> familyVersionInfo = Memoizer
            .memoize(this::queryFamilyVersionInfo);

    /**
     * The bitness value.
     */
    private final Supplier<Integer> bitness = Memoizer.memoize(this::queryPlatformBitness);

    /**
     * Utility method for subclasses to take a full process list as input and return the children or descendants of a
     * particular process. The process itself is also returned to more efficiently extract its start time for filtering
     *
     * @param allProcs       A collection of all processes
     * @param parentPid      The process ID whose children or descendants to return
     * @param allDescendants If false, only gets immediate children of this process. If true, gets all descendants.
     * @return Set of children or descendants of parentPid
     */
    protected static Set<Integer> getChildrenOrDescendants(
            Collection<OSProcess> allProcs,
            int parentPid,
            boolean allDescendants) {
        Map<Integer, Integer> parentPidMap = allProcs.stream()
                .collect(Collectors.toMap(OSProcess::getProcessID, OSProcess::getParentProcessID));
        return getChildrenOrDescendants(parentPidMap, parentPid, allDescendants);
    }

    /**
     * Utility method for subclasses to take a map of pid to parent as input and return the children or descendants of a
     * particular process.
     *
     * @param parentPidMap   a map of all processes with processID as key and parentProcessID as value
     * @param parentPid      The process ID whose children or descendants to return
     * @param allDescendants If false, only gets immediate children of this process. If true, gets all descendants.
     * @return Set of children or descendants of parentPid, including the parent
     */
    protected static Set<Integer> getChildrenOrDescendants(
            Map<Integer, Integer> parentPidMap,
            int parentPid,
            boolean allDescendants) {
        // Set to hold results
        Set<Integer> descendantPids = new HashSet<>();
        descendantPids.add(parentPid);
        // Queue for BFS algorithm
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(parentPid);
        // Add children, repeating if recursive
        do {
            for (int pid : getChildren(parentPidMap, queue.poll())) {
                if (!descendantPids.contains(pid)) {
                    descendantPids.add(pid);
                    queue.add(pid);
                }
            }
        } while (allDescendants && !queue.isEmpty());
        return descendantPids;
    }

    /**
     * Returns the children.
     *
     * @param parentPidMap the parent pid map
     * @param parentPid    the parent pid
     * @return the get children result
     */
    private static Set<Integer> getChildren(Map<Integer, Integer> parentPidMap, int parentPid) {
        return parentPidMap.entrySet().stream()
                .filter(e -> e.getValue().equals(parentPid) && !e.getKey().equals(parentPid)).map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the manufacturer.
     *
     * @return the get manufacturer result
     */
    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    /**
     * Queries the manufacturer.
     *
     * @return the query manufacturer result
     */
    protected abstract String queryManufacturer();

    /**
     * Returns the family.
     *
     * @return the get family result
     */
    @Override
    public String getFamily() {
        return familyVersionInfo.get().getLeft();
    }

    /**
     * Returns the version info.
     *
     * @return the get version info result
     */
    @Override
    public OSVersionInfo getVersionInfo() {
        return familyVersionInfo.get().getRight();
    }

    /**
     * Queries the family version info.
     *
     * @return the query family version info result
     */
    protected abstract Pair<String, OSVersionInfo> queryFamilyVersionInfo();

    /**
     * Returns the bitness.
     *
     * @return the get bitness result
     */
    @Override
    public int getBitness() {
        return bitness.get();
    }

    /**
     * Queries the platform bitness.
     *
     * @return the query platform bitness result
     */
    private int queryPlatformBitness() {
        if (Platform.is64Bit()) {
            return 64;
        }
        // Initialize based on JVM Bitness. Individual OS implementations will test
        // if 32-bit JVM running on 64-bit OS
        int jvmBitness = System.getProperty("os.arch").contains("64") ? 64 : 32;
        return queryBitness(jvmBitness);
    }

    /**
     * Backup OS-specific query to determine bitness if previous checks fail
     *
     * @param jvmBitness The bitness of the JVM
     * @return The operating system bitness
     */
    protected abstract int queryBitness(int jvmBitness);

    /**
     * Returns the processes.
     *
     * @param filter the filter
     * @param sort   the sort
     * @param limit  the limit
     * @return the get processes result
     */
    @Override
    public List<OSProcess> getProcesses(Predicate<OSProcess> filter, Comparator<OSProcess> sort, int limit) {
        return queryAllProcesses().stream().filter(filter == null ? ProcessFiltering.ALL_PROCESSES : filter)
                .sorted(sort == null ? ProcessSorting.NO_SORTING : sort).limit(limit > 0 ? limit : Long.MAX_VALUE)
                .collect(Collectors.toList());
    }

    /**
     * Queries the all processes.
     *
     * @return the query all processes result
     */
    protected abstract List<OSProcess> queryAllProcesses();

    /**
     * Returns the child processes.
     *
     * @param parentPid the parent pid
     * @param filter    the filter
     * @param sort      the sort
     * @param limit     the limit
     * @return the get child processes result
     */
    @Override
    public List<OSProcess> getChildProcesses(
            int parentPid,
            Predicate<OSProcess> filter,
            Comparator<OSProcess> sort,
            int limit) {
        // Get this pid and its children
        List<OSProcess> childProcs = queryChildProcesses(parentPid);
        // Extract the parent from the list
        OSProcess parent = childProcs.stream().filter(p -> p.getProcessID() == parentPid).findAny().orElse(null);
        // Get the parent's start time
        long parentStartTime = parent == null ? 0 : parent.getStartTime();
        // Get children after parent
        return queryChildProcesses(parentPid).stream().filter(filter == null ? ProcessFiltering.ALL_PROCESSES : filter)
                .filter(p -> p.getProcessID() != parentPid && p.getStartTime() >= parentStartTime)
                .sorted(sort == null ? ProcessSorting.NO_SORTING : sort).limit(limit > 0 ? limit : Long.MAX_VALUE)
                .collect(Collectors.toList());
    }

    /**
     * Queries the child processes.
     *
     * @param parentPid the parent pid
     * @return the query child processes result
     */
    protected abstract List<OSProcess> queryChildProcesses(int parentPid);

    /**
     * Returns the descendant processes.
     *
     * @param parentPid the parent pid
     * @param filter    the filter
     * @param sort      the sort
     * @param limit     the limit
     * @return the get descendant processes result
     */
    @Override
    public List<OSProcess> getDescendantProcesses(
            int parentPid,
            Predicate<OSProcess> filter,
            Comparator<OSProcess> sort,
            int limit) {
        // Get this pid and its descendants
        List<OSProcess> descendantProcs = queryDescendantProcesses(parentPid);
        // Extract the parent from the list
        OSProcess parent = descendantProcs.stream().filter(p -> p.getProcessID() == parentPid).findAny().orElse(null);
        // Get the parent's start time
        long parentStartTime = parent == null ? 0 : parent.getStartTime();
        // Get descendants after parent
        return queryDescendantProcesses(parentPid).stream()
                .filter(filter == null ? ProcessFiltering.ALL_PROCESSES : filter)
                .filter(p -> p.getProcessID() != parentPid && p.getStartTime() >= parentStartTime)
                .sorted(sort == null ? ProcessSorting.NO_SORTING : sort).limit(limit > 0 ? limit : Long.MAX_VALUE)
                .collect(Collectors.toList());
    }

    /**
     * Queries the descendant processes.
     *
     * @param parentPid the parent pid
     * @return the query descendant processes result
     */
    protected abstract List<OSProcess> queryDescendantProcesses(int parentPid);

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getManufacturer()).append(Symbol.C_SPACE).append(getFamily()).append(Symbol.C_SPACE)
                .append(getVersionInfo());
        return sb.toString();
    }

}
