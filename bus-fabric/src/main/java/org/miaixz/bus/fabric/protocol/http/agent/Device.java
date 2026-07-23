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
import java.util.regex.PatternSyntaxException;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.fabric.Builder;

/**
 * Device classifier parsed from a User-Agent value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Device {

    /**
     * Validated display name and equality identity of this classifier.
     */
    private final String name;

    /**
     * Optional case-insensitive pattern used to identify this device.
     */
    private final Pattern rule;

    /**
     * Creates a device classifier.
     *
     * @param name non-blank classifier name
     * @param rule case-insensitive match regular expression, or {@code null} to disable matching
     * @throws org.miaixz.bus.core.lang.exception.ValidateException if {@code name} is blank
     * @throws PatternSyntaxException                               if {@code rule} is not a valid regular expression
     */
    public Device(final String name, final String rule) {
        this.name = AgentRules.name(name);
        this.rule = AgentRules.compile(rule);
    }

    /**
     * Returns the first matching mobile classifier, then the first matching desktop classifier.
     *
     * @param text User-Agent text to classify, or {@code null}
     * @return first matching classifier, or {@link Builder#HTTP_AGENT_DEVICE_UNKNOWN} when no rule matches
     */
    public static Device parse(final String text) {
        for (final Device device : Registry.MOBILE) {
            if (device.matches(text)) {
                return device;
            }
        }
        for (final Device device : Registry.DESKTOP) {
            if (device.matches(text)) {
                return device;
            }
        }
        return Builder.HTTP_AGENT_DEVICE_UNKNOWN;
    }

    /**
     * Appends a mobile device classifier after existing mobile rules.
     *
     * @param name non-blank classifier name
     * @param rule case-insensitive match regular expression, or {@code null} to disable matching
     * @throws org.miaixz.bus.core.lang.exception.ValidateException if {@code name} is blank
     * @throws PatternSyntaxException                               if {@code rule} is not a valid regular expression
     */
    public static void addMobileDevice(final String name, final String rule) {
        Registry.MOBILE.add(new Device(name, rule));
    }

    /**
     * Appends a desktop device classifier after existing desktop rules.
     *
     * @param name non-blank classifier name
     * @param rule case-insensitive match regular expression, or {@code null} to disable matching
     * @throws org.miaixz.bus.core.lang.exception.ValidateException if {@code name} is blank
     * @throws PatternSyntaxException                               if {@code rule} is not a valid regular expression
     */
    public static void addDesktopDevice(final String name, final String rule) {
        Registry.DESKTOP.add(new Device(name, rule));
    }

    /**
     * Returns known device classifiers.
     *
     * @return immutable snapshot of all mobile classifiers followed by all desktop classifiers
     */
    public static List<Device> devices() {
        final ArrayList<Device> devices = new ArrayList<>(Registry.MOBILE);
        devices.addAll(Registry.DESKTOP);
        return List.copyOf(devices);
    }

    /**
     * Returns the name.
     *
     * @return validated classifier name
     */
    public String name() {
        return name;
    }

    /**
     * Returns whether this device matches the text.
     *
     * @param text User-Agent text to search, or {@code null}
     * @return {@code true} when the configured rule finds a substring match
     */
    public boolean matches(final String text) {
        return AgentRules.contains(rule, text);
    }

    /**
     * Returns whether this is a mobile device.
     *
     * @return {@code true} when the mobile registry contains an equal-by-name classifier
     */
    public boolean mobile() {
        return Registry.MOBILE.contains(this);
    }

    /**
     * Returns whether this is iPhone or iPod.
     *
     * @return {@code true} when the classifier name equals the registered iPhone or iPod name
     */
    public boolean iPhoneOrIPod() {
        return equals(Builder.HTTP_AGENT_DEVICE_IPHONE) || equals(Builder.HTTP_AGENT_DEVICE_IPOD);
    }

    /**
     * Returns whether this is iPad.
     *
     * @return {@code true} when the classifier name equals the registered iPad name
     */
    public boolean iPad() {
        return equals(Builder.HTTP_AGENT_DEVICE_IPAD);
    }

    /**
     * Returns whether this is an iOS device.
     *
     * @return {@code true} for an iPhone, iPod, or iPad classifier name
     */
    public boolean ios() {
        return iPhoneOrIPod() || iPad();
    }

    /**
     * Returns whether this is an Android device.
     *
     * @return {@code true} when the classifier name equals the registered Android or Google TV name
     */
    public boolean android() {
        return equals(Builder.HTTP_AGENT_DEVICE_ANDROID) || equals(Builder.HTTP_AGENT_DEVICE_GOOGLE_TV);
    }

    /**
     * Returns whether this is HarmonyOS.
     *
     * @return {@code true} when the classifier name equals the registered HarmonyOS name
     */
    public boolean harmony() {
        return equals(Builder.HTTP_AGENT_DEVICE_HARMONY);
    }

    /**
     * Returns whether this classifier is unknown.
     *
     * @return {@code true} when the classifier name equals the shared unknown marker
     */
    public boolean unknown() {
        return Normal.UNKNOWN.equals(name);
    }

    /**
     * Compares device classifiers by name.
     *
     * @param object object compared with this classifier
     * @return {@code true} when the object is a {@code Device} with the same name
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof Device other && Objects.equals(name, other.name);
    }

    /**
     * Returns a hash code based on the device name.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the device name.
     *
     * @return device name
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Lazily initialized mobile and desktop device classifier registries.
     */
    private static final class Registry {

        /**
         * Mobile classifiers initialized only after Builder device constants are complete.
         */
        private static final List<Device> MOBILE = Instances.get(
                Device.class.getName() + ".mobileDevices",
                () -> new CopyOnWriteArrayList<>(List.of(
                        Builder.HTTP_AGENT_DEVICE_WINDOWS_PHONE,
                        Builder.HTTP_AGENT_DEVICE_IPAD,
                        Builder.HTTP_AGENT_DEVICE_IPOD,
                        Builder.HTTP_AGENT_DEVICE_IPHONE,
                        new Device("Android", "XiaoMi|MI\\s+"),
                        Builder.HTTP_AGENT_DEVICE_ANDROID,
                        Builder.HTTP_AGENT_DEVICE_HARMONY,
                        Builder.HTTP_AGENT_DEVICE_GOOGLE_TV,
                        new Device("htcFlyer", "htc_flyer"),
                        new Device("Symbian", "symbian(os)?"),
                        new Device("Blackberry", "blackberry"))));

        /**
         * Desktop classifiers evaluated after every mobile classifier.
         */
        private static final List<Device> DESKTOP = Instances.get(
                Device.class.getName() + ".desktopDevices",
                () -> new CopyOnWriteArrayList<>(List.of(
                        new Device("Windows", "windows"),
                        new Device("Mac", "(macintosh|darwin)"),
                        new Device("Linux", "linux"),
                        new Device("Wii", "wii"),
                        new Device("Playstation", "playstation"),
                        new Device("Java", "java"))));
    }

}
