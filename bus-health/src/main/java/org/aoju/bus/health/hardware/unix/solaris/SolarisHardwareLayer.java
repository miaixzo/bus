/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.unix.solaris;

import org.aoju.bus.health.hardware.*;

/**
 * <p>
 * SolarisHardwareAbstractionLayer class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.6.1
 * @since JDK 1.8+
 */
public class SolarisHardwareLayer extends AbstractHardwareLayer {

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
    public PowerSource[] getPowerSources() {
        return SolarisPowerSource.getPowerSources();
    }

    @Override
    public HWDiskStore[] getDiskStores() {
        return new SolarisDisks().getDisks();
    }

    @Override
    public Display[] getDisplays() {
        return SolarisDisplay.getDisplays();
    }

    @Override
    public NetworkIF[] getNetworkIFs() {
        return new SolarisNetworks().getNetworks();
    }

    @Override
    public UsbDevice[] getUsbDevices(boolean tree) {
        return SolarisUsbDevice.getUsbDevices(tree);
    }

    @Override
    public SoundCard[] getSoundCards() {
        return SolarisSoundCard.getSoundCards().toArray(new SoundCard[0]);
    }

}
