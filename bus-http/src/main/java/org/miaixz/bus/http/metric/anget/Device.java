/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.metric.anget;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;

import java.util.List;

/**
 * Represents a device type, such as a phone, tablet, or desktop, parsed from a User-Agent string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Device extends UserAgent {

    /**
     * Represents an unknown device.
     */
    public static final Device UNKNOWN = new Device(Normal.UNKNOWN, null);

    /**
     * Represents an iPhone device.
     */
    public static final Device IPHONE = new Device("iPhone", "iphone");
    /**
     * Represents an iPod device.
     */
    public static final Device IPOD = new Device("iPod", "ipod");
    /**
     * Represents an iPad device.
     */
    public static final Device IPAD = new Device("iPad", "ipad");

    /**
     * Represents an Android device.
     */
    public static final Device ANDROID = new Device("Android", "android");
    /**
     * Represents a HarmonyOS device.
     */
    public static final Device HARMONY = new Device("Harmony", "OpenHarmony");
    /**
     * Represents a Google TV device.
     */
    public static final Device GOOGLE_TV = new Device("GoogleTV", "googletv");

    /**
     * Represents a Windows Phone device.
     */
    public static final Device WINDOWS_PHONE = new Device("Windows Phone", "windows (ce|phone|mobile)( os)?");

    /**
     * A list of supported mobile device types.
     */
    public static final List<Device> MOBILE_DEVICE = ListKit.of(
            WINDOWS_PHONE, //
            IPAD, //
            IPOD, //
            IPHONE, //
            new Device("Android", "XiaoMi|MI\\s+"), //
            ANDROID, //
            HARMONY, //
            GOOGLE_TV, //
            new Device("htcFlyer", "htc_flyer"), //
            new Device("Symbian", "symbian(os)?"), //
            new Device("Blackberry", "blackberry") //
    );
    /**
     * A list of supported desktop device types.
     */
    public static final List<Device> DESKTOP_DEVICE = ListKit.of(
            new Device("Windows", "windows"), //
            new Device("Mac", "(macintosh|darwin)"), //
            new Device("Linux", "linux"), //
            new Device("Wii", "wii"), //
            new Device("Playstation", "playstation"), //
            new Device("Java", "java") //
    );

    /**
     * A combined list of all supported device types.
     */
    public static final List<Device> ALL_DEVICE = (List<Device>) CollKit.union(MOBILE_DEVICE, DESKTOP_DEVICE);

    /**
     * Constructs a new {@code Device} instance.
     *
     * @param name The name of the device.
     * @param rule The keyword or expression to match in the User-Agent string.
     */
    public Device(final String name, final String rule) {
        super(name, rule);
    }

    /**
     * Returns whether this device is a mobile device.
     *
     * @return {@code true} if this is a mobile device, {@code false} otherwise.
     */
    public boolean isMobile() {
        return MOBILE_DEVICE.contains(this);
    }

    /**
     * Returns whether this device is an iPhone or iPod.
     *
     * @return {@code true} if this is an iPhone or iPod, {@code false} otherwise.
     */
    public boolean isIPhoneOrIPod() {
        return this.equals(IPHONE) || this.equals(IPOD);
    }

    /**
     * Returns whether this device is an iPad.
     *
     * @return {@code true} if this is an iPad, {@code false} otherwise.
     */
    public boolean isIPad() {
        return this.equals(IPAD);
    }

    /**
     * Returns whether this device is an iOS device (iPhone, iPod, or iPad).
     *
     * @return {@code true} if this is an iOS device, {@code false} otherwise.
     */
    public boolean isIos() {
        return isIPhoneOrIPod() || isIPad();
    }

    /**
     * Returns whether this device is an Android device (including Google TV).
     *
     * @return {@code true} if this is an Android device, {@code false} otherwise.
     */
    public boolean isAndroid() {
        return this.equals(ANDROID) || this.equals(GOOGLE_TV);
    }

    /**
     * Returns whether this device is a HarmonyOS device.
     *
     * @return {@code true} if this is a HarmonyOS device, {@code false} otherwise.
     */
    public boolean isHarmony() {
        return this.equals(HARMONY);
    }

}
