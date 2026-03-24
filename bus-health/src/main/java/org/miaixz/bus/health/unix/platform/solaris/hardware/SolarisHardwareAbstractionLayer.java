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
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.unix.hardware.UnixDisplay;
import org.miaixz.bus.health.unix.hardware.UnixPrinter;

/**
 * SolarisHardwareAbstractionLayer class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class SolarisHardwareAbstractionLayer extends AbstractHardwareAbstractionLayer {

    @Override
    public ComputerSystem createComputerSystem() {
        return new SolarisComputerSystem();
    }

    @Override
    public GlobalMemory createMemory() {
        return new SolarisGlobalMemory();
    }

    @Override
    public CentralProcessor createProcessor() {
        return new SolarisCentralProcessor();
    }

    @Override
    public Sensors createSensors() {
        return new SolarisSensors();
    }

    @Override
    public List<PowerSource> getPowerSources() {
        return SolarisPowerSource.getPowerSources();
    }

    @Override
    public List<HWDiskStore> getDiskStores() {
        return SolarisHWDiskStore.getDisks();
    }

    @Override
    public List<Display> getDisplays() {
        return UnixDisplay.getDisplays();
    }

    @Override
    public List<NetworkIF> getNetworkIFs(boolean includeLocalInterfaces) {
        return SolarisNetworkIF.getNetworks(includeLocalInterfaces);
    }

    @Override
    public List<UsbDevice> getUsbDevices(boolean tree) {
        return SolarisUsbDevice.getUsbDevices(tree);
    }

    @Override
    public List<SoundCard> getSoundCards() {
        return SolarisSoundCard.getSoundCards();
    }

    @Override
    public List<GraphicsCard> getGraphicsCards() {
        return SolarisGraphicsCard.getGraphicsCards();
    }

    @Override
    public List<Printer> getPrinters() {
        return UnixPrinter.getPrinters();
    }

}
