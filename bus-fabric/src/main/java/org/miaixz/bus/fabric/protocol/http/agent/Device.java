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
package org.miaixz.bus.fabric.protocol.http.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;

/**
 * Device classifier parsed from a User-Agent value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Device {

    /**
     * Unknown device.
     */
    public static final Device UNKNOWN = Instances
            .get(Device.class.getName() + ".unknown", () -> new Device(Normal.UNKNOWN, null));

    /**
     * iPhone.
     */
    public static final Device IPHONE = Instances
            .get(Device.class.getName() + ".iphone", () -> new Device("iPhone", "iphone"));

    /**
     * iPod.
     */
    public static final Device IPOD = Instances.get(Device.class.getName() + ".ipod", () -> new Device("iPod", "ipod"));

    /**
     * iPad.
     */
    public static final Device IPAD = Instances.get(Device.class.getName() + ".ipad", () -> new Device("iPad", "ipad"));

    /**
     * Android.
     */
    public static final Device ANDROID = Instances
            .get(Device.class.getName() + ".android", () -> new Device("Android", "android"));

    /**
     * HarmonyOS.
     */
    public static final Device HARMONY = Instances
            .get(Device.class.getName() + ".harmony", () -> new Device("Harmony", "OpenHarmony"));

    /**
     * Google TV.
     */
    public static final Device GOOGLE_TV = Instances
            .get(Device.class.getName() + ".googleTv", () -> new Device("GoogleTV", "googletv"));

    /**
     * Windows Phone.
     */
    public static final Device WINDOWS_PHONE = Instances.get(
            Device.class.getName() + ".windowsPhone",
            () -> new Device("Windows Phone", "windows (ce|phone|mobile)( os)?"));

    /**
     * Mobile classifiers.
     */
    private static final List<Device> MOBILE_DEVICES = Instances.get(
            Device.class.getName() + ".mobileDevices",
            () -> new CopyOnWriteArrayList<>(List.of(
                    WINDOWS_PHONE,
                    IPAD,
                    IPOD,
                    IPHONE,
                    new Device("Android", "XiaoMi|MI\\s+"),
                    ANDROID,
                    HARMONY,
                    GOOGLE_TV,
                    new Device("htcFlyer", "htc_flyer"),
                    new Device("Symbian", "symbian(os)?"),
                    new Device("Blackberry", "blackberry"))));

    /**
     * Desktop classifiers.
     */
    private static final List<Device> DESKTOP_DEVICES = Instances.get(
            Device.class.getName() + ".desktopDevices",
            () -> new CopyOnWriteArrayList<>(List.of(
                    new Device("Windows", "windows"),
                    new Device("Mac", "(macintosh|darwin)"),
                    new Device("Linux", "linux"),
                    new Device("Wii", "wii"),
                    new Device("Playstation", "playstation"),
                    new Device("Java", "java"))));

    /**
     * Device name.
     */
    private final String name;

    /**
     * Match rule.
     */
    private final Pattern rule;

    /**
     * Creates a device classifier.
     *
     * @param name name
     * @param rule match rule
     */
    public Device(final String name, final String rule) {
        this.name = AgentRules.name(name);
        this.rule = AgentRules.compile(rule);
    }

    /**
     * Parses a device.
     *
     * @param text User-Agent text
     * @return device
     */
    public static Device parse(final String text) {
        for (final Device device : devices()) {
            if (device.matches(text)) {
                return device;
            }
        }
        return UNKNOWN;
    }

    /**
     * Adds a mobile device classifier.
     *
     * @param name name
     * @param rule match rule
     */
    public static void addMobileDevice(final String name, final String rule) {
        MOBILE_DEVICES.add(new Device(name, rule));
    }

    /**
     * Adds a desktop device classifier.
     *
     * @param name name
     * @param rule match rule
     */
    public static void addDesktopDevice(final String name, final String rule) {
        DESKTOP_DEVICES.add(new Device(name, rule));
    }

    /**
     * Returns known device classifiers.
     *
     * @return devices
     */
    public static List<Device> devices() {
        final ArrayList<Device> devices = new ArrayList<>(MOBILE_DEVICES);
        devices.addAll(DESKTOP_DEVICES);
        return List.copyOf(devices);
    }

    /**
     * Returns the name.
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Returns whether this device matches the text.
     *
     * @param text User-Agent text
     * @return true when matched
     */
    public boolean matches(final String text) {
        return AgentRules.contains(rule, text);
    }

    /**
     * Returns whether this is a mobile device.
     *
     * @return true when mobile
     */
    public boolean mobile() {
        return MOBILE_DEVICES.contains(this);
    }

    /**
     * Returns whether this is iPhone or iPod.
     *
     * @return true when iPhone or iPod
     */
    public boolean iPhoneOrIPod() {
        return equals(IPHONE) || equals(IPOD);
    }

    /**
     * Returns whether this is iPad.
     *
     * @return true when iPad
     */
    public boolean iPad() {
        return equals(IPAD);
    }

    /**
     * Returns whether this is an iOS device.
     *
     * @return true when iOS
     */
    public boolean ios() {
        return iPhoneOrIPod() || iPad();
    }

    /**
     * Returns whether this is an Android device.
     *
     * @return true when Android
     */
    public boolean android() {
        return equals(ANDROID) || equals(GOOGLE_TV);
    }

    /**
     * Returns whether this is HarmonyOS.
     *
     * @return true when HarmonyOS
     */
    public boolean harmony() {
        return equals(HARMONY);
    }

    /**
     * Returns whether this classifier is unknown.
     *
     * @return true when unknown
     */
    public boolean unknown() {
        return Normal.UNKNOWN.equals(name);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Device other && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

}
