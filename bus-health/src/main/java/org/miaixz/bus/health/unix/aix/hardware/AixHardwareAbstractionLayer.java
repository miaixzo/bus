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
package org.miaixz.bus.health.unix.aix.hardware;

import java.util.List;
import java.util.function.Supplier;

import com.sun.jna.platform.unix.aix.Perfstat.perfstat_disk_t;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.unix.shared.hardware.CupsPrinter;
import org.miaixz.bus.health.unix.shared.hardware.UnixDisplay;
import org.miaixz.bus.health.unix.aix.driver.Lscfg;
import org.miaixz.bus.health.unix.aix.driver.perfstat.PerfstatDisk;

/**
 * AIXHardwareAbstractionLayer class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class AixHardwareAbstractionLayer extends AbstractHardwareAbstractionLayer {

    /**
     * Constructs a new AixHardwareAbstractionLayer instance.
     */
    public AixHardwareAbstractionLayer() {
        // No initialization required.
    }

    // Memoized hardware listing
    /**
     * The lscfg value.
     */
    private final Supplier<List<String>> lscfg = Memoizer.memoize(Lscfg::queryAllDevices, Memoizer.defaultExpiration());
    // Memoized disk stats to pass to disk object(s)
    /**
     * The diskStats value.
     */
    private final Supplier<perfstat_disk_t[]> diskStats = Memoizer
            .memoize(PerfstatDisk::queryDiskStats, Memoizer.defaultExpiration());

    /**
     * Creates the computer system.
     *
     * @return the create computer system result
     */
    @Override
    public ComputerSystem createComputerSystem() {
        return new AixComputerSystem(lscfg);
    }

    /**
     * Creates the memory.
     *
     * @return the create memory result
     */
    @Override
    public GlobalMemory createMemory() {
        return new AixGlobalMemory(lscfg);
    }

    /**
     * Creates the processor.
     *
     * @return the create processor result
     */
    @Override
    public CentralProcessor createProcessor() {
        return new AixCentralProcessor();
    }

    /**
     * Creates the sensors.
     *
     * @return the create sensors result
     */
    @Override
    public Sensors createSensors() {
        return new AixSensors(lscfg);
    }

    /**
     * Returns the power sources.
     *
     * @return the get power sources result
     */
    @Override
    public List<PowerSource> getPowerSources() {
        return AixPowerSource.getPowerSources();
    }

    /**
     * Returns the disk stores.
     *
     * @return the get disk stores result
     */
    @Override
    public List<HWDiskStore> getDiskStores() {
        return AixHWDiskStore.getDisks(diskStats);
    }

    /**
     * Returns the displays.
     *
     * @return the get displays result
     */
    @Override
    public List<Display> getDisplays() {
        return UnixDisplay.getDisplays();
    }

    /**
     * Returns the network i fs.
     *
     * @param includeLocalInterfaces the include local interfaces
     * @return the get network i fs result
     */
    @Override
    public List<NetworkIF> getNetworkIFs(boolean includeLocalInterfaces) {
        return AixNetworkIF.getNetworks(includeLocalInterfaces);
    }

    /**
     * Returns the usb devices.
     *
     * @param tree the tree
     * @return the get usb devices result
     */
    @Override
    public List<UsbDevice> getUsbDevices(boolean tree) {
        return AixUsbDevice.getUsbDevices(tree, lscfg);
    }

    /**
     * Returns the sound cards.
     *
     * @return the get sound cards result
     */
    @Override
    public List<SoundCard> getSoundCards() {
        return AixSoundCard.getSoundCards(lscfg);
    }

    /**
     * Returns the graphics cards.
     *
     * @return the get graphics cards result
     */
    @Override
    public List<GraphicsCard> getGraphicsCards() {
        return AixGraphicsCard.getGraphicsCards(lscfg);
    }

    /**
     * Returns the printers.
     *
     * @return the get printers result
     */
    @Override
    public List<Printer> getPrinters() {
        return CupsPrinter.getPrinters();
    }

}
