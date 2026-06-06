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
package org.miaixz.bus.health.linux.hardware;

import java.util.List;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.linux.driver.DrmEdid;
import org.miaixz.bus.health.unix.shared.hardware.CupsPrinter;
import org.miaixz.bus.health.unix.shared.hardware.UnixDisplay;

/**
 * LinuxHardwareAbstractionLayer class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class LinuxHardwareAbstractionLayer extends AbstractHardwareAbstractionLayer {

    /**
     * Constructs a new {@code LinuxHardwareAbstractionLayer} instance.
     */
    public LinuxHardwareAbstractionLayer() {
        // No initialization required.
    }

    /**
     * Creates the computer system.
     *
     * @return the create computer system result
     */
    @Override
    public ComputerSystem createComputerSystem() {
        return new LinuxComputerSystem();
    }

    /**
     * Creates the memory.
     *
     * @return the create memory result
     */
    @Override
    public GlobalMemory createMemory() {
        return new LinuxGlobalMemory();
    }

    /**
     * Creates the processor.
     *
     * @return the create processor result
     */
    @Override
    public CentralProcessor createProcessor() {
        return new LinuxCentralProcessor();
    }

    /**
     * Creates the sensors.
     *
     * @return the create sensors result
     */
    @Override
    public Sensors createSensors() {
        return new LinuxSensors();
    }

    /**
     * Returns the power sources.
     *
     * @return the get power sources result
     */
    @Override
    public List<PowerSource> getPowerSources() {
        return LinuxPowerSource.getPowerSources();
    }

    /**
     * Returns the disk stores.
     *
     * @return the get disk stores result
     */
    @Override
    public List<HWDiskStore> getDiskStores() {
        return LinuxHWDiskStore.getDisks();
    }

    /**
     * Returns the logical volume groups.
     *
     * @return the get logical volume groups result
     */
    @Override
    public List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        return LinuxLogicalVolumeGroup.getLogicalVolumeGroups();
    }

    /**
     * Returns the displays.
     *
     * @return the get displays result
     */
    @Override
    public List<Display> getDisplays() {
        List<byte[]> edids = DrmEdid.getEdidArrays();
        if (!edids.isEmpty()) {
            return edids.stream().map(UnixDisplay::new).collect(Collectors.toList());
        }
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
        return LinuxNetworkIF.getNetworks(includeLocalInterfaces);
    }

    /**
     * Returns the usb devices.
     *
     * @param tree the tree
     * @return the get usb devices result
     */
    @Override
    public List<UsbDevice> getUsbDevices(boolean tree) {
        return LinuxUsbDevice.getUsbDevices(tree);
    }

    /**
     * Returns the sound cards.
     *
     * @return the get sound cards result
     */
    @Override
    public List<SoundCard> getSoundCards() {
        return LinuxSoundCard.getSoundCards();
    }

    /**
     * Returns the bluetooth devices.
     *
     * @return the get bluetooth devices result
     */
    @Override
    public List<BluetoothDevice> getBluetoothDevices() {
        return LinuxBluetoothDevice.getBluetoothDevices();
    }

    /**
     * Returns the graphics cards.
     *
     * @return the get graphics cards result
     */
    @Override
    public List<GraphicsCard> getGraphicsCards() {
        return LinuxGraphicsCard.getGraphicsCards();
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
