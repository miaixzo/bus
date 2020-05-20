/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.health.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * 与Udev对话的接口
 *
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
public interface Udev extends Library {

    /**
     * Constant <code>INSTANCE</code>
     */
    Udev INSTANCE = Native.load("udev", Udev.class);

    /**
     * <p>
     * udev_new.
     * </p>
     *
     * @return a {@link Udev.UdevHandle} object.
     */
    Udev.UdevHandle udev_new();

    /**
     * <p>
     * udev_unref.
     * </p>
     *
     * @param udev a {@link Udev.UdevHandle} object.
     */
    void udev_unref(Udev.UdevHandle udev);

    /**
     * <p>
     * udev_device_unref.
     * </p>
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     */
    void udev_device_unref(Udev.UdevDevice udev_device);

    /**
     * <p>
     * udev_enumerate_unref.
     * </p>
     *
     * @param udev_enumerate a {@link Udev.UdevEnumerate} object.
     */
    void udev_enumerate_unref(Udev.UdevEnumerate udev_enumerate);

    /**
     * <p>
     * udev_enumerate_new.
     * </p>
     *
     * @param udev a {@link Udev.UdevHandle} object.
     * @return a {@link Udev.UdevEnumerate} object.
     */
    Udev.UdevEnumerate udev_enumerate_new(Udev.UdevHandle udev);

    /**
     * <p>
     * udev_device_get_parent_with_subsystem_devtype.
     * </p>
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @param subsystem   a {@link java.lang.String} object.
     * @param devtype     a {@link java.lang.String} object.
     * @return a {@link Udev.UdevDevice} object.
     */
    Udev.UdevDevice udev_device_get_parent_with_subsystem_devtype(Udev.UdevDevice udev_device, String subsystem,
                                                                  String devtype);

    /**
     * <p>
     * udev_device_new_from_syspath.
     * </p>
     *
     * @param udev    a {@link Udev.UdevHandle} object.
     * @param syspath a {@link java.lang.String} object.
     * @return a {@link Udev.UdevDevice} object.
     */
    Udev.UdevDevice udev_device_new_from_syspath(Udev.UdevHandle udev, String syspath);

    /**
     * <p>
     * udev_list_entry_get_next.
     * </p>
     *
     * @param list_entry a {@link Udev.UdevListEntry} object.
     * @return a {@link Udev.UdevListEntry} object.
     */
    Udev.UdevListEntry udev_list_entry_get_next(Udev.UdevListEntry list_entry);

    /**
     * <p>
     * udev_device_get_sysattr_value.
     * </p>
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @param sysattr     a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_sysattr_value(Udev.UdevDevice udev_device, String sysattr);

    /**
     * <p>
     * udev_enumerate_add_match_subsystem.
     * </p>
     *
     * @param udev_enumerate a {@link Udev.UdevEnumerate} object.
     * @param subsystem      a {@link java.lang.String} object.
     * @return a int.
     */
    int udev_enumerate_add_match_subsystem(Udev.UdevEnumerate udev_enumerate, String subsystem);

    /**
     * <p>
     * udev_enumerate_scan_devices.
     * </p>
     *
     * @param udev_enumerate a {@link Udev.UdevEnumerate} object.
     * @return a int.
     */
    int udev_enumerate_scan_devices(Udev.UdevEnumerate udev_enumerate);

    /**
     * <p>
     * udev_enumerate_get_list_entry.
     * </p>
     *
     * @param udev_enumerate a {@link Udev.UdevEnumerate} object.
     * @return a {@link Udev.UdevListEntry} object.
     */
    Udev.UdevListEntry udev_enumerate_get_list_entry(Udev.UdevEnumerate udev_enumerate);

    /**
     * <p>
     * udev_list_entry_get_name.
     * </p>
     *
     * @param list_entry a {@link Udev.UdevListEntry} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_list_entry_get_name(Udev.UdevListEntry list_entry);

    /**
     * <p>
     * udev_device_get_devtype.
     * </p>
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_devtype(Udev.UdevDevice udev_device);

    /**
     * <p>
     * udev_device_get_devnode.
     * </p>
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_devnode(Udev.UdevDevice udev_device);

    /**
     * <p>
     * udev_device_get_syspath.
     * </p>
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_syspath(Udev.UdevDevice udev_device);

    /**
     * <p>
     * udev_device_get_property_value.
     * </p>
     *
     * @param udev_device a {@link Udev.UdevDevice} object.
     * @param key         a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String udev_device_get_property_value(Udev.UdevDevice udev_device, String key);

    /**
     * <p>
     * udev_device_get_sysname.
     * </p>
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
