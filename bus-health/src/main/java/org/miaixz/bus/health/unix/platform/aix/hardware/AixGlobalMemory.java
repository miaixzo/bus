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
package org.miaixz.bus.health.unix.platform.aix.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.sun.jna.platform.unix.aix.Perfstat.perfstat_memory_total_t;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.PhysicalMemory;
import org.miaixz.bus.health.builtin.hardware.VirtualMemory;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGlobalMemory;
import org.miaixz.bus.health.unix.platform.aix.driver.perfstat.PerfstatMemory;

/**
 * Memory obtained by perfstat_memory_total_t
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class AixGlobalMemory extends AbstractGlobalMemory {

    // AIX has multiple page size units, but for purposes of "pages" in perfstat,
    // the docs specify 4KB pages so we hardcode this
    /**
     * The PAGESIZE constant.
     */
    private static final long PAGESIZE = 4096L;

    /**
     * The perfstatMem value.
     */
    private final Supplier<perfstat_memory_total_t> perfstatMem = Memoizer
            .memoize(AixGlobalMemory::queryPerfstat, Memoizer.defaultExpiration());

    /**
     * The lscfg value.
     */
    private final Supplier<List<String>> lscfg;

    /**
     * The vm value.
     */
    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    /**
     * Creates a new AixGlobalMemory instance.
     *
     * @param lscfg the lscfg
     */
    AixGlobalMemory(Supplier<List<String>> lscfg) {
        this.lscfg = lscfg;
    }

    /**
     * Queries the perfstat.
     *
     * @return the query perfstat result
     */
    private static perfstat_memory_total_t queryPerfstat() {
        return PerfstatMemory.queryMemoryTotal();
    }

    /**
     * Returns the available.
     *
     * @return the get available result
     */
    @Override
    public long getAvailable() {
        return perfstatMem.get().real_avail * PAGESIZE;
    }

    /**
     * Returns the total.
     *
     * @return the get total result
     */
    @Override
    public long getTotal() {
        return perfstatMem.get().real_total * PAGESIZE;
    }

    /**
     * Returns the page size.
     *
     * @return the get page size result
     */
    @Override
    public long getPageSize() {
        return PAGESIZE;
    }

    /**
     * Returns the virtual memory.
     *
     * @return the get virtual memory result
     */
    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    /**
     * Returns the physical memory.
     *
     * @return the get physical memory result
     */
    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        List<PhysicalMemory> pmList = new ArrayList<>();
        boolean isMemModule = false;
        boolean isMemoryDIMM = false;
        String bankLabel = Normal.UNKNOWN;
        String locator = Normal.EMPTY;
        String partNumber = Normal.UNKNOWN;
        long capacity = 0L;
        for (String line : lscfg.get()) {
            String s = line.trim();
            if (s.endsWith("memory-module")) {
                isMemModule = true;
            } else if (s.startsWith("Memory DIMM")) {
                isMemoryDIMM = true;
            } else if (isMemModule) {
                if (s.startsWith("Node:")) {
                    bankLabel = s.substring(5).trim();
                    if (bankLabel.startsWith("IBM,")) {
                        bankLabel = bankLabel.substring(4);
                    }
                } else if (s.startsWith("Physical Location:")) {
                    locator = "/" + s.substring(18).trim();
                } else if (s.startsWith("Size")) {
                    capacity = Parsing.parseLongOrDefault(Parsing.removeLeadingDots(s.substring(4).trim()), 0L) << 20;
                } else if (s.startsWith("Hardware Location Code")) {
                    // Save previous bank
                    if (capacity > 0) {
                        pmList.add(
                                new PhysicalMemory(bankLabel + locator, capacity, 0L, "IBM", Normal.UNKNOWN,
                                        Normal.UNKNOWN, Normal.UNKNOWN));
                    }
                    bankLabel = Normal.UNKNOWN;
                    locator = Normal.EMPTY;
                    capacity = 0L;
                    isMemModule = false;
                }
            } else if (isMemoryDIMM) {
                if (s.startsWith("Hardware Location Code")) {
                    locator = Parsing.removeLeadingDots(s.substring(23).trim());
                } else if (s.startsWith("Size")) {
                    capacity = Parsing.parseLongOrDefault(Parsing.removeLeadingDots(s.substring(4).trim()), 0L) << 20;
                } else if (s.startsWith("Part Number") || s.startsWith("FRU Number")) {
                    partNumber = Parsing.removeLeadingDots(s.substring(11).trim());
                } else if (s.startsWith("Physical Location:")) {
                    // Save previous bank
                    if (capacity > 0) {
                        pmList.add(
                                new PhysicalMemory(locator, capacity, 0L, "IBM", Normal.UNKNOWN, partNumber,
                                        Normal.UNKNOWN));
                    }
                    partNumber = Normal.UNKNOWN;
                    locator = Normal.EMPTY;
                    capacity = 0L;
                    isMemoryDIMM = false;
                }
            }
        }
        return pmList;
    }

    /**
     * Creates the virtual memory.
     *
     * @return the create virtual memory result
     */
    private VirtualMemory createVirtualMemory() {
        return new AixVirtualMemory(perfstatMem);
    }

}
