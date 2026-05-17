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

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;

/**
 * Common methods for platform HWDiskStore classes
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractHWDiskStore implements HWDiskStore {

    /**
     * The name value.
     */
    private final String name;

    /**
     * The model value.
     */
    private final String model;

    /**
     * The serial value.
     */
    private final String serial;

    /**
     * The size value.
     */
    private final long size;

    /**
     * The diskType value.
     */
    private final String diskType;

    /**
     * Creates a new AbstractHWDiskStore instance.
     *
     * @param name   the name
     * @param model  the model
     * @param serial the serial
     * @param size   the size
     */
    protected AbstractHWDiskStore(String name, String model, String serial, long size) {
        this(name, model, serial, size, "Unknown");
    }

    /**
     * Creates a new AbstractHWDiskStore instance.
     *
     * @param name     the name
     * @param model    the model
     * @param serial   the serial
     * @param size     the size
     * @param diskType the disk type
     */
    protected AbstractHWDiskStore(String name, String model, String serial, long size, String diskType) {
        this.name = name;
        this.model = model;
        this.serial = serial;
        this.size = size;
        this.diskType = diskType;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the model.
     *
     * @return the get model result
     */
    @Override
    public String getModel() {
        return this.model;
    }

    /**
     * Returns the serial.
     *
     * @return the get serial result
     */
    @Override
    public String getSerial() {
        return this.serial;
    }

    /**
     * Returns the size.
     *
     * @return the get size result
     */
    @Override
    public long getSize() {
        return this.size;
    }

    /**
     * Returns the disk type.
     *
     * @return the get disk type result
     */
    @Override
    public String getDiskType() {
        return this.diskType;
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        boolean readwrite = getReads() > 0 || getWrites() > 0;
        String sb = getName() + ": " + "(model: " + getModel() + " - S/N: " + getSerial() + " - type: " + getDiskType()
                + ") " + "size: " + (getSize() > 0 ? Formats.formatBytesDecimal(getSize()) : "?") + ", " + "reads: "
                + (readwrite ? getReads() : "?") + " (" + (readwrite ? Formats.formatBytes(getReadBytes()) : "?")
                + "), " + "writes: " + (readwrite ? getWrites() : "?") + " ("
                + (readwrite ? Formats.formatBytes(getWriteBytes()) : "?") + "), " + "xfer: "
                + (readwrite ? getTransferTime() : "?");
        return sb;
    }

}
