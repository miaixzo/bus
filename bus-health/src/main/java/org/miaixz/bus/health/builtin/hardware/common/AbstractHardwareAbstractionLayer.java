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
package org.miaixz.bus.health.builtin.hardware.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.*;

/**
 * Common fields or methods used by platform-specific implementations of HardwareAbstractionLayer
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractHardwareAbstractionLayer implements HardwareAbstractionLayer {

    /**
     * Constructs a new AbstractHardwareAbstractionLayer instance.
     */
    public AbstractHardwareAbstractionLayer() {
        // No initialization required.
    }

    /**
     * The computerSystem value.
     */
    private final Supplier<ComputerSystem> computerSystem = Memoizer.memoize(this::createComputerSystem);

    /**
     * The processor value.
     */
    private final Supplier<CentralProcessor> processor = Memoizer.memoize(this::createProcessor);

    /**
     * The memory value.
     */
    private final Supplier<GlobalMemory> memory = Memoizer.memoize(this::createMemory);

    /**
     * The sensors value.
     */
    private final Supplier<Sensors> sensors = Memoizer.memoize(this::createSensors);

    /**
     * The display list supplier.
     */
    private final Supplier<List<Display>> displays = Memoizer.memoize(this::createDisplays, Memoizer.slowExpiration());

    /**
     * The sound card list supplier.
     */
    private final Supplier<List<SoundCard>> soundCards = Memoizer.memoize(this::createSoundCards,
            Memoizer.slowExpiration());

    /**
     * The graphics card list supplier.
     */
    private final Supplier<List<GraphicsCard>> graphicsCards = Memoizer.memoize(this::createGraphicsCards,
            Memoizer.slowExpiration());

    /**
     * The USB device tree supplier.
     */
    private final Supplier<List<UsbDevice>> usbDevicesTree = Memoizer.memoize(this::createUsbDevices,
            Memoizer.slowExpiration());

    /**
     * Returns the computer system.
     *
     * @return the get computer system result
     */
    @Override
    public ComputerSystem getComputerSystem() {
        return computerSystem.get();
    }

    /**
     * Instantiates the platform-specific {@link ComputerSystem} object
     *
     * @return platform-specific {@link ComputerSystem} object
     */
    protected abstract ComputerSystem createComputerSystem();

    /**
     * Returns the processor.
     *
     * @return the get processor result
     */
    @Override
    public CentralProcessor getProcessor() {
        return processor.get();
    }

    /**
     * Instantiates the platform-specific {@link CentralProcessor} object
     *
     * @return platform-specific {@link CentralProcessor} object
     */
    protected abstract CentralProcessor createProcessor();

    /**
     * Returns the memory.
     *
     * @return the get memory result
     */
    @Override
    public GlobalMemory getMemory() {
        return memory.get();
    }

    /**
     * Instantiates the platform-specific {@link GlobalMemory} object
     *
     * @return platform-specific {@link GlobalMemory} object
     */
    protected abstract GlobalMemory createMemory();

    /**
     * Returns the sensors.
     *
     * @return the get sensors result
     */
    @Override
    public Sensors getSensors() {
        return sensors.get();
    }

    /**
     * Instantiates the platform-specific {@link Sensors} object
     *
     * @return platform-specific {@link Sensors} object
     */
    protected abstract Sensors createSensors();

    /**
     * Returns the displays.
     *
     * @return the display list
     */
    @Override
    public List<Display> getDisplays() {
        return displays.get();
    }

    /**
     * Instantiates the platform-specific display list.
     *
     * @return platform-specific display list
     */
    protected abstract List<Display> createDisplays();

    /**
     * Returns the sound cards.
     *
     * @return the sound card list
     */
    @Override
    public List<SoundCard> getSoundCards() {
        return soundCards.get();
    }

    /**
     * Instantiates the platform-specific sound card list.
     *
     * @return platform-specific sound card list
     */
    protected abstract List<SoundCard> createSoundCards();

    /**
     * Returns the graphics cards.
     *
     * @return the graphics card list
     */
    @Override
    public List<GraphicsCard> getGraphicsCards() {
        return graphicsCards.get();
    }

    /**
     * Instantiates the platform-specific graphics card list.
     *
     * @return platform-specific graphics card list
     */
    protected abstract List<GraphicsCard> createGraphicsCards();

    /**
     * Returns the USB devices.
     *
     * @param tree Whether to preserve the USB device tree.
     * @return the USB device list
     */
    @Override
    public List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = usbDevicesTree.get();
        if (tree) {
            return devices;
        }
        List<UsbDevice> deviceList = new ArrayList<>();
        for (UsbDevice device : devices) {
            AbstractUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList;
    }

    /**
     * Instantiates the platform-specific USB device tree.
     *
     * @return platform-specific USB device tree
     */
    protected abstract List<UsbDevice> createUsbDevices();

    /**
     * Returns the network i fs.
     *
     * @return the get network i fs result
     */
    @Override
    public List<NetworkIF> getNetworkIFs() {
        return getNetworkIFs(false);
    }

}
