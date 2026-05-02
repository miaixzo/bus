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
package org.miaixz.bus.health.windows.hardware;

import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;

/**
 * WindowsHardwareAbstractionLayer class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class WindowsHardwareAbstractionLayer extends AbstractHardwareAbstractionLayer {

    /**
     * Creates the computer system.
     *
     * @return the create computer system result
     */
    @Override
    public ComputerSystem createComputerSystem() {
        return new WindowsComputerSystem();
    }

    /**
     * Creates the memory.
     *
     * @return the create memory result
     */
    @Override
    public GlobalMemory createMemory() {
        return new WindowsGlobalMemory();
    }

    /**
     * Creates the processor.
     *
     * @return the create processor result
     */
    @Override
    public CentralProcessor createProcessor() {
        return new WindowsCentralProcessor();
    }

    /**
     * Creates the sensors.
     *
     * @return the create sensors result
     */
    @Override
    public Sensors createSensors() {
        return new WindowsSensors();
    }

    /**
     * Returns the power sources.
     *
     * @return the get power sources result
     */
    @Override
    public List<PowerSource> getPowerSources() {
        return WindowsPowerSource.getPowerSources();
    }

    /**
     * Returns the disk stores.
     *
     * @return the get disk stores result
     */
    @Override
    public List<HWDiskStore> getDiskStores() {
        return WindowsHWDiskStore.getDisks();
    }

    /**
     * Returns the logical volume groups.
     *
     * @return the get logical volume groups result
     */
    @Override
    public List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        return WindowsLogicalVolumeGroup.getLogicalVolumeGroups();
    }

    /**
     * Returns the displays.
     *
     * @return the get displays result
     */
    @Override
    public List<Display> getDisplays() {
        return WindowsDisplay.getDisplays();
    }

    /**
     * Returns the network i fs.
     *
     * @param includeLocalInterfaces the include local interfaces
     * @return the get network i fs result
     */
    @Override
    public List<NetworkIF> getNetworkIFs(boolean includeLocalInterfaces) {
        return WindowsNetworkIF.getNetworks(includeLocalInterfaces);
    }

    /**
     * Returns the usb devices.
     *
     * @param tree the tree
     * @return the get usb devices result
     */
    @Override
    public List<UsbDevice> getUsbDevices(boolean tree) {
        return WindowsUsbDevice.getUsbDevices(tree);
    }

    /**
     * Returns the sound cards.
     *
     * @return the get sound cards result
     */
    @Override
    public List<SoundCard> getSoundCards() {
        return WindowsSoundCard.getSoundCards();
    }

    /**
     * Returns the graphics cards.
     *
     * @return the get graphics cards result
     */
    @Override
    public List<GraphicsCard> getGraphicsCards() {
        return WindowsGraphicsCard.getGraphicsCards();
    }

    /**
     * Returns the printers.
     *
     * @return the get printers result
     */
    @Override
    public List<Printer> getPrinters() {
        return WindowsPrinter.getPrinters();
    }

}
