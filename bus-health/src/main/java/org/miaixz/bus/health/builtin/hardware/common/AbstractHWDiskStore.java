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

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;

/**
 * Common methods for platform HWDiskStore classes
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public abstract class AbstractHWDiskStore implements HWDiskStore {

    private final String name;
    private final String model;
    private final String serial;
    private final long size;

    protected AbstractHWDiskStore(String name, String model, String serial, long size) {
        this.name = name;
        this.model = model;
        this.serial = serial;
        this.size = size;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getModel() {
        return this.model;
    }

    @Override
    public String getSerial() {
        return this.serial;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public String toString() {
        boolean readwrite = getReads() > 0 || getWrites() > 0;
        String sb = getName() + ": " +
                "(model: " + getModel() +
                " - S/N: " + getSerial() + ") " +
                "size: " + (getSize() > 0 ? Formats.formatBytesDecimal(getSize()) : "?") + ", " +
                "reads: " + (readwrite ? getReads() : "?") +
                " (" + (readwrite ? Formats.formatBytes(getReadBytes()) : "?") + "), " +
                "writes: " + (readwrite ? getWrites() : "?") +
                " (" + (readwrite ? Formats.formatBytes(getWriteBytes()) : "?") + "), " +
                "xfer: " + (readwrite ? getTransferTime() : "?");
        return sb;
    }
}
