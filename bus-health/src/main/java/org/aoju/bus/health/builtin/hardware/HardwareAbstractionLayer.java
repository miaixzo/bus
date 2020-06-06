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
package org.aoju.bus.health.builtin.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;

import java.util.List;

/**
 * A hardware abstraction layer. Provides access to hardware items such as
 * processors, memory, battery, and disks.
 *
 * @author Kimi Liu
 * @version 5.9.8
 * @since JDK 1.8+
 */
@ThreadSafe
public interface HardwareAbstractionLayer {

    /**
     * Instantiates a {@link ComputerSystem} object. This represents
     * the physical hardware, including components such as BIOS/Firmware and a
     * motherboard, logic board, etc.
     *
     * @return a {@link ComputerSystem} object.
     */
    ComputerSystem getComputerSystem();

    /**
     * Instantiates a {@link CentralProcessor} object. This represents
     * one or more Logical CPUs.
     *
     * @return A {@link CentralProcessor} object.
     */
    CentralProcessor getProcessor();

    /**
     * Instantiates a {@link GlobalMemory} object.
     *
     * @return A memory object.
     */
    GlobalMemory getMemory();

    /**
     * Instantiates an array of {@link PowerSource} objects,
     * representing batteries, etc.
     *
     * @return An array of PowerSource objects or an empty array if none are
     * present.
     */
    PowerSource[] getPowerSources();

    /**
     * Instantiates an {@code UnmodifiableList} of {@link HWDiskStore}
     * objects, representing physical hard disks or other similar storage devices
     *
     * @return An {@code UnmodifiableList} of HWDiskStore objects or an empty list
     * if none are present.
     */
    List<HWDiskStore> getDiskStores();

    /**
     * Gets a list of {@link NetworkIF} objects, representing a network interface
     *
     * @return An {@code UnmodifiableList} of {@link NetworkIF} objects representing
     * the interfaces
     */
    List<NetworkIF> getNetworkIFs();

    /**
     * Instantiates an array of {@link Display} objects, representing
     * monitors or other video output devices.
     *
     * @return An array of Display objects or an empty array if none are present.
     */
    Display[] getDisplays();

    /**
     * Instantiates a {@link Sensors} object, representing CPU
     * temperature and fan speed
     *
     * @return A Sensors object
     */
    Sensors getSensors();

    /**
     * Instantiates an array of {@link UsbDevice} objects,
     * representing devices connected via a usb port (including internal devices).
     * <p>
     * If the value of tree is true, the top level devices returned from this method
     * are the USB Controllers; connected hubs and devices in its device tree share
     * that controller's bandwidth. If the value of tree is false, USB devices (not
     * controllers) are listed in a single flat array.
     *
     * @param tree Whether to display devices in a nested tree format from their
     *             controllers
     * @return An array of UsbDevice objects representing (optionally) the USB
     * Controllers and devices connected to them, or an empty array if none
     * are present
     */
    UsbDevice[] getUsbDevices(boolean tree);

    /**
     * Instantiates an array of {@link SoundCard} objects,
     * representing the Sound cards.
     *
     * @return An array of SoundCard objects or an empty array if none are present.
     */
    SoundCard[] getSoundCards();

    /**
     * Instantiates an array of {@link GraphicsCard} objects,
     * representing the Graphics cards.
     *
     * @return An array of GraphicsCard objects or an empty array if none are
     * present.
     */
    GraphicsCard[] getGraphicsCards();

}
