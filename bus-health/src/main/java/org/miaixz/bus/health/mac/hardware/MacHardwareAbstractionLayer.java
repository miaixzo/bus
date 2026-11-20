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
package org.miaixz.bus.health.mac.hardware;

import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.unix.shared.hardware.CupsPrinter;

/**
 * <p>
 * MacHardwareAbstractionLayer class.
 * </p>
 * Hardware Abstraction Layer for macOS.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class MacHardwareAbstractionLayer extends AbstractHardwareAbstractionLayer {

    /**
     * Constructs a new {@code MacHardwareAbstractionLayer} instance.
     */
    public MacHardwareAbstractionLayer() {
        // No initialization required.
    }

    /**
     * Instantiates the platform-specific {@link ComputerSystem} object
     *
     * @return platform-specific {@link ComputerSystem} object
     */
    @Override
    public ComputerSystem createComputerSystem() {
        return new MacComputerSystem();
    }

    /**
     * Instantiates the platform-specific {@link GlobalMemory} object
     *
     * @return platform-specific {@link GlobalMemory} object
     */
    @Override
    public GlobalMemory createMemory() {
        return new MacGlobalMemory();
    }

    /**
     * Instantiates the platform-specific {@link CentralProcessor} object
     *
     * @return platform-specific {@link CentralProcessor} object
     */
    @Override
    public CentralProcessor createProcessor() {
        return new MacCentralProcessor();
    }

    /**
     * Instantiates the platform-specific {@link Sensors} object
     *
     * @return platform-specific {@link Sensors} object
     */
    @Override
    public Sensors createSensors() {
        return new MacSensors();
    }

    /**
     * Instantiates a list of {@link PowerSource} objects, representing batteries, etc.
     *
     * @return A list of PowerSource objects or an empty list if none are present.
     */
    @Override
    public List<PowerSource> getPowerSources() {
        return MacPowerSource.getPowerSources();
    }

    /**
     * Instantiates a list of {@link HWDiskStore} objects, representing physical hard disks or other similar storage
     * devices.
     *
     * @return A list of HWDiskStore objects or an empty list if none are present.
     */
    @Override
    public List<HWDiskStore> getDiskStores() {
        return MacHWDiskStore.getDisks();
    }

    /**
     * Instantiates a list of {@link LogicalVolumeGroup} objects, representing a storage pool or group of devices,
     * partitions, volumes, or other implementation specific means of file storage.
     * <p>
     * If not yet implemented or if logical volume groups do not exist, returns an empty list.
     * <p>
     * Currently implemented for Linux (LVM2), macOS (Core Storage), and Windows (Storage Spaces).
     *
     * @return A list of {@link LogicalVolumeGroup} objects or an empty list if none are present.
     */
    @Override
    public List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        return MacLogicalVolumeGroup.getLogicalVolumeGroups();
    }

    /**
     * Instantiates the platform-specific display list.
     *
     * @return platform-specific display list
     */
    @Override
    protected List<Display> createDisplays() {
        return MacDisplay.getDisplays();
    }

    /**
     * Gets a list {@link NetworkIF} objects, representing a network interface.
     *
     * @param includeLocalInterfaces whether to include local interfaces (loopback or no hardware address) in the result
     * @return A list of {@link NetworkIF} objects representing the interfaces
     */
    @Override
    public List<NetworkIF> getNetworkIFs(boolean includeLocalInterfaces) {
        return MacNetworkIF.getNetworks(includeLocalInterfaces);
    }

    /**
     * Instantiates the platform-specific USB device tree.
     *
     * @return platform-specific USB device tree
     */
    @Override
    protected List<UsbDevice> createUsbDevices() {
        return MacUsbDevice.getUsbDevices(true);
    }

    /**
     * Instantiates the platform-specific sound card list.
     *
     * @return platform-specific sound card list
     */
    @Override
    protected List<SoundCard> createSoundCards() {
        return MacSoundCard.getSoundCards();
    }

    /**
     * Returns the bluetooth devices.
     *
     * @return the get bluetooth devices result
     */
    @Override
    public List<BluetoothDevice> getBluetoothDevices() {
        return MacBluetoothDevice.getBluetoothDevices();
    }

    /**
     * Instantiates the platform-specific graphics card list.
     *
     * @return platform-specific graphics card list
     */
    @Override
    protected List<GraphicsCard> createGraphicsCards() {
        return MacGraphicsCard.getGraphicsCards();
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
