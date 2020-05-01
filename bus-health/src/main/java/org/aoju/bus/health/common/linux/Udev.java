/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.health.common.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * 与Udev对话的接口
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public interface Udev extends Library {

    /**
     * 常量实例 <code>INSTANCE</code>
     */
    Udev INSTANCE = Native.load("udev", Udev.class);

    /**
     * udev_new.
     *
     * @return a {@link Udev.UdevHandle} object.
     */
    Udev.UdevHandle udev_new();

    /**
     * udev_unref.
     *
     * @param udev a {@link Udev.UdevHandle} object.
     */
    void udev_unref(Udev.UdevHandle udev);

    /**
     * udev_device_unref.
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     */
    void udev_device_unref(Udev.UdevDevice udev_device);

    /**
     * udev_enumerate_unref.
     *
     * @param udev_enumerate a {@link Udev.UdevEnumerate} object.
     */
    void udev_enumerate_unref(Udev.UdevEnumerate udev_enumerate);

    /**
     * udev_enumerate_new.
     *
     * @param udev a {@link Udev.UdevHandle} object.
     * @return a {@link Udev.UdevEnumerate} object.
     */
    Udev.UdevEnumerate udev_enumerate_new(Udev.UdevHandle udev);

    /**
     * udev_device_get_parent_with_subsystem_devtype.
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @param subsystem   a {@link java.lang.String} object.
     * @param devtype     a {@link java.lang.String} object.
     * @return a {@link Udev.UdevDevice} object.
     */
    Udev.UdevDevice udev_device_get_parent_with_subsystem_devtype(Udev.UdevDevice udev_device, String subsystem,
                                                                  String devtype);

    /**
     * udev_device_new_from_syspath.
     *
     * @param udev    a {@link Udev.UdevHandle} object.
     * @param syspath a {@link java.lang.String} object.
     * @return a {@link Udev.UdevDevice} object.
     */
    Udev.UdevDevice udev_device_new_from_syspath(Udev.UdevHandle udev, String syspath);

    /**
     * udev_list_entry_get_next.
     *
     * @param list_entry a {@link Udev.UdevListEntry} object.
     * @return a {@link Udev.UdevListEntry} object.
     */
    Udev.UdevListEntry udev_list_entry_get_next(Udev.UdevListEntry list_entry);

    /**
     * udev_device_get_sysattr_value.
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @param sysattr     a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_sysattr_value(Udev.UdevDevice udev_device, String sysattr);

    /**
     * udev_enumerate_add_match_subsystem.
     *
     * @param udev_enumerate a {@link Udev.UdevEnumerate} object.
     * @param subsystem      a {@link java.lang.String} object.
     * @return a int.
     */
    int udev_enumerate_add_match_subsystem(Udev.UdevEnumerate udev_enumerate, String subsystem);

    /**
     * udev_enumerate_scan_devices.
     *
     * @param udev_enumerate a {@link Udev.UdevEnumerate} object.
     * @return a int.
     */
    int udev_enumerate_scan_devices(Udev.UdevEnumerate udev_enumerate);

    /**
     * udev_enumerate_get_list_entry.
     *
     * @param udev_enumerate a {@link Udev.UdevEnumerate} object.
     * @return a {@link Udev.UdevListEntry} object.
     */
    Udev.UdevListEntry udev_enumerate_get_list_entry(Udev.UdevEnumerate udev_enumerate);

    /**
     * udev_list_entry_get_name.
     *
     * @param list_entry a {@link Udev.UdevListEntry} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_list_entry_get_name(Udev.UdevListEntry list_entry);

    /**
     * udev_device_get_devtype.
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_devtype(Udev.UdevDevice udev_device);

    /**
     * udev_device_get_devnode.
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_devnode(Udev.UdevDevice udev_device);

    /**
     * udev_device_get_syspath.
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_syspath(Udev.UdevDevice udev_device);

    /**
     * udev_device_get_property_value.
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @param key         a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_property_value(Udev.UdevDevice udev_device, String key);

    /**
     * udev_device_get_sysname.
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_sysname(UdevDevice udev_device);

    final class UdevHandle extends PointerType {

        public UdevHandle(Pointer address) {
            super(address);
        }

        public UdevHandle() {
            super();
        }
    }

    final class UdevDevice extends PointerType {

        public UdevDevice(Pointer address) {
            super(address);
        }

        public UdevDevice() {
            super();
        }
    }

    final class UdevEnumerate extends PointerType {

        public UdevEnumerate(Pointer address) {
            super(address);
        }

        public UdevEnumerate() {
            super();
        }
    }

    final class UdevListEntry extends PointerType {

        public UdevListEntry(Pointer address) {
            super(address);
        }

        public UdevListEntry() {
            super();
        }
    }

}
