/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac.hardware;

import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;

/**
 * <p>
 * MacHardwareAbstractionLayer class.
 * </p>
 * Hardware Abstraction Layer for macOS.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class MacHardwareAbstractionLayer extends AbstractHardwareAbstractionLayer {

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public ComputerSystem createComputerSystem() {
        return new MacComputerSystem();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public GlobalMemory createMemory() {
        return new MacGlobalMemory();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public CentralProcessor createProcessor() {
        return new MacCentralProcessor();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Sensors createSensors() {
        return new MacSensors();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<PowerSource> getPowerSources() {
        return MacPowerSource.getPowerSources();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<HWDiskStore> getDiskStores() {
        return MacHWDiskStore.getDisks();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        return MacLogicalVolumeGroup.getLogicalVolumeGroups();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<Display> getDisplays() {
        return MacDisplay.getDisplays();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<NetworkIF> getNetworkIFs(boolean includeLocalInterfaces) {
        return MacNetworkIF.getNetworks(includeLocalInterfaces);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<UsbDevice> getUsbDevices(boolean tree) {
        return MacUsbDevice.getUsbDevices(tree);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<SoundCard> getSoundCards() {
        return MacSoundCard.getSoundCards();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<GraphicsCard> getGraphicsCards() {
        return MacGraphicsCard.getGraphicsCards();
    }

}
