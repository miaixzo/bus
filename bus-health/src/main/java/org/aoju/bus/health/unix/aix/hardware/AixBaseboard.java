/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org OSHI and other contributors.                 *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.unix.aix.hardware;

import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Triple;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.builtin.hardware.AbstractBaseboard;
import org.aoju.bus.health.unix.aix.drivers.Lscfg;

import java.util.List;
import java.util.function.Supplier;

/**
 * Baseboard data obtained by lscfg
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class AixBaseboard extends AbstractBaseboard {

    private static final String IBM = "IBM";
    private final String model;
    private final String serialNumber;
    private final String version;

    AixBaseboard(Supplier<List<String>> lscfg) {
        Triple<String, String, String> msv = Lscfg.queryBackplaneModelSerialVersion(lscfg.get());
        this.model = StringKit.isBlank(msv.getLeft()) ? Normal.UNKNOWN : msv.getLeft();
        this.serialNumber = StringKit.isBlank(msv.getMiddle()) ? Normal.UNKNOWN : msv.getMiddle();
        this.version = StringKit.isBlank(msv.getRight()) ? Normal.UNKNOWN : msv.getRight();
    }

    @Override
    public String getManufacturer() {
        return IBM;
    }

    @Override
    public String getModel() {
        return this.model;
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

}
