/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.linux.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.builtin.hardware.*;

import java.util.List;

/**
 * LinuxHardwareAbstractionLayer class.
 *
 * @author Kimi Liu
 * @version 5.9.5
 * @since JDK 1.8+
 */
@ThreadSafe
public final class LinuxHardwareAbstractionLayer extends AbstractHardwareAbstractionLayer {

    @Override
    public ComputerSystem createComputerSystem() {
        return new LinuxComputerSystem();
    }

    @Override
    public GlobalMemory createMemory() {
        return new LinuxGlobalMemory();
    }

    @Override
    public CentralProcessor createProcessor() {
        return new LinuxCentralProcessor();
    }

    @Override
    public Sensors createSensors() {
        return new LinuxSensors();
    }

    @Override
    public PowerSource[] getPowerSources() {
        return LinuxPowerSource.getPowerSources();
    }

    @Override
    public List<HWDiskStore> getDiskStores() {
        return LinuxHWDiskStore.getDisks();
    }

    @Override
    public Display[] getDisplays() {
        return LinuxDisplay.getDisplays();
    }

    @Override
    public List<NetworkIF> getNetworkIFs() {
        return LinuxNetworkIF.getNetworks();
    }

    @Override
    public UsbDevice[] getUsbDevices(boolean tree) {
        return LinuxUsbDevice.getUsbDevices(tree);
    }

    @Override
    public SoundCard[] getSoundCards() {
        return LinuxSoundCard.getSoundCards().toArray(new SoundCard[0]);
    }

    @Override
    public GraphicsCard[] getGraphicsCards() {
        return LinuxGraphicsCard.getGraphicsCards().toArray(new GraphicsCard[0]);
    }

}
