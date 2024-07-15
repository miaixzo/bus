/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.builtin.hardware.common;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.ComputerSystem;
import org.miaixz.bus.health.builtin.hardware.Firmware;

import java.util.function.Supplier;

/**
 * Computer System data.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public abstract class AbstractComputerSystem implements ComputerSystem {

    private final Supplier<Firmware> firmware = Memoizer.memoize(this::createFirmware);

    private final Supplier<Baseboard> baseboard = Memoizer.memoize(this::createBaseboard);

    @Override
    public Firmware getFirmware() {
        return firmware.get();
    }

    /**
     * Instantiates the platform-specific {@link Firmware} object
     *
     * @return platform-specific {@link Firmware} object
     */
    protected abstract Firmware createFirmware();

    @Override
    public Baseboard getBaseboard() {
        return baseboard.get();
    }

    /**
     * Instantiates the platform-specific {@link Baseboard} object
     *
     * @return platform-specific {@link Baseboard} object
     */
    protected abstract Baseboard createBaseboard();

    @Override
    public String toString() {
        String sb = "manufacturer=" + getManufacturer() + ", " +
                "model=" + getModel() + ", " +
                "serial number=" + getSerialNumber() + ", " +
                "uuid=" + getHardwareUUID();
        return sb;
    }

}
