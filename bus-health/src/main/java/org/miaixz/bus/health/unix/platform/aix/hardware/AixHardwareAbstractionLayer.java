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
package org.miaixz.bus.health.unix.platform.aix.hardware;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.unix.hardware.UnixDisplay;
import org.miaixz.bus.health.unix.hardware.UnixPrinter;
import org.miaixz.bus.health.unix.platform.aix.driver.Lscfg;
import org.miaixz.bus.health.unix.platform.aix.driver.perfstat.PerfstatDisk;

import com.sun.jna.platform.unix.aix.Perfstat.perfstat_disk_t;

/**
 * AIXHardwareAbstractionLayer class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class AixHardwareAbstractionLayer extends AbstractHardwareAbstractionLayer {

    // Memoized hardware listing
    private final Supplier<List<String>> lscfg = Memoizer.memoize(Lscfg::queryAllDevices, Memoizer.defaultExpiration());
    // Memoized disk stats to pass to disk object(s)
    private final Supplier<perfstat_disk_t[]> diskStats = Memoizer
            .memoize(PerfstatDisk::queryDiskStats, Memoizer.defaultExpiration());

    @Override
    public ComputerSystem createComputerSystem() {
        return new AixComputerSystem(lscfg);
    }

    @Override
    public GlobalMemory createMemory() {
        return new AixGlobalMemory(lscfg);
    }

    @Override
    public CentralProcessor createProcessor() {
        return new AixCentralProcessor();
    }

    @Override
    public Sensors createSensors() {
        return new AixSensors(lscfg);
    }

    @Override
    public List<PowerSource> getPowerSources() {
        return AixPowerSource.getPowerSources();
    }

    @Override
    public List<HWDiskStore> getDiskStores() {
        return AixHWDiskStore.getDisks(diskStats);
    }

    @Override
    public List<Display> getDisplays() {
        return UnixDisplay.getDisplays();
    }

    @Override
    public List<NetworkIF> getNetworkIFs(boolean includeLocalInterfaces) {
        return AixNetworkIF.getNetworks(includeLocalInterfaces);
    }

    @Override
    public List<UsbDevice> getUsbDevices(boolean tree) {
        return AixUsbDevice.getUsbDevices(tree, lscfg);
    }

    @Override
    public List<SoundCard> getSoundCards() {
        return AixSoundCard.getSoundCards(lscfg);
    }

    @Override
    public List<GraphicsCard> getGraphicsCards() {
        return AixGraphicsCard.getGraphicsCards(lscfg);
    }

    @Override
    public List<Printer> getPrinters() {
        return UnixPrinter.getPrinters();
    }

}
