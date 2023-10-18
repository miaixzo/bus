/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.galaxy;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.image.Device;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeviceCache extends ConfigurationCache<Configuration, Device>
        implements IDeviceCache {

    public DeviceCache(Configuration conf) {
        super(conf);
    }

    @Override
    protected Device find(Configuration conf, String key) throws InternalException {
        return conf.findDevice(key);
    }

    @Override
    public Device findDevice(String deviceName) throws InternalException {
        Device device = get(deviceName);
        if (null == device)
            throw new InternalException("Unknown Device: " + deviceName);
        if (!device.isInstalled())
            throw new InternalException("Device: " + deviceName + " not installed");
        return device;
    }

}
