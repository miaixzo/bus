/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * 设备信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Device extends UserAgent {

    /**
     * 未知
     */
    public static final Device UNKNOWN = new Device(Normal.UNKNOWN, null);

    /**
     * Iphone
     */
    public static final Device IPHONE = new Device("iPhone", "iphone");
    /**
     * ipod
     */
    public static final Device IPOD = new Device("iPod", "ipod");
    /**
     * ipad
     */
    public static final Device IPAD = new Device("iPad", "ipad");

    /**
     * android
     */
    public static final Device ANDROID = new Device("Android", "android");
    /**
     * harmony
     */
    public static final Device HARMONY = new Device("Harmony", "OpenHarmony");
    /**
     * googletv
     */
    public static final Device GOOGLE_TV = new Device("GoogleTV", "googletv");

    /**
     * Windows Phone
     */
    public static final Device WINDOWS_PHONE = new Device("Windows Phone", "windows (ce|phone|mobile)( os)?");

    /**
     * 支持的移动平台类型
     */
    public static final List<Device> MOBILE_DEVICE = ListKit.of(WINDOWS_PHONE, //
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
     * 支持的桌面平台类型
     */
    public static final List<Device> DESKTOP_DEVICE = ListKit.of(new Device("Windows", "windows"), //
            new Device("Mac", "(macintosh|darwin)"), //
            new Device("Linux", "linux"), //
            new Device("Wii", "wii"), //
            new Device("Playstation", "playstation"), //
            new Device("Java", "java") //
    );

    /**
     * 支持的平台类型
     */
    public static final List<Device> ALL_DEVICE = (List<Device>) CollKit.union(MOBILE_DEVICE, DESKTOP_DEVICE);

    /**
     * 构造
     *
     * @param name 平台名称
     * @param rule 关键字或表达式
     */
    public Device(final String name, final String rule) {
        super(name, rule);
    }

    /**
     * 是否为移动平台
     *
     * @return 是否为移动平台
     */
    public boolean isMobile() {
        return MOBILE_DEVICE.contains(this);
    }

    /**
     * 是否为Iphone或者iPod设备
     *
     * @return 是否为Iphone或者iPod设备
     */
    public boolean isIPhoneOrIPod() {
        return this.equals(IPHONE) || this.equals(IPOD);
    }

    /**
     * 是否为Iphone或者iPod设备
     *
     * @return 是否为Iphone或者iPod设备
     */
    public boolean isIPad() {
        return this.equals(IPAD);
    }

    /**
     * 是否为IOS平台，包括IPhone、IPod、IPad
     *
     * @return 是否为IOS平台，包括IPhone、IPod、IPad
     */
    public boolean isIos() {
        return isIPhoneOrIPod() || isIPad();
    }

    /**
     * 是否为Android平台，包括Android和Google TV
     *
     * @return 是否为Android平台，包括Android和Google TV
     */
    public boolean isAndroid() {
        return this.equals(ANDROID) || this.equals(GOOGLE_TV);
    }

    /**
     * 是否为Harmony平台
     *
     * @return 是否为Harmony平台
     */
    public boolean isHarmony() {
        return this.equals(HARMONY);
    }

}
