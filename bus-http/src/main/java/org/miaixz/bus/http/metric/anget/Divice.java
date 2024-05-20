/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http.metric.anget;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ListKit;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Divice extends UserAgent {

    /**
     * 未知
     */
    public static final Divice UNKNOWN = new Divice(Normal.UNKNOWN, null);

    /**
     * iPhone
     */
    public static final Divice IPHONE = new Divice("iPhone", "iphone");
    /**
     * iPod
     */
    public static final Divice IPOD = new Divice("iPod", "ipod");
    /**
     * iPad
     */
    public static final Divice IPAD = new Divice("iPad", "ipad");

    /**
     * Android
     */
    public static final Divice ANDROID = new Divice("Android", "android");
    /**
     * Android
     */
    public static final Divice GOOGLE_TV = new Divice("GoogleTV", "googletv");

    /**
     * Windows Phone
     */
    public static final Divice WINDOWS_PHONE = new Divice("Windows Phone", "windows (ce|phone|mobile)( os)?");

    /**
     * 支持的移动平台类型
     */
    public static final List<Divice> MOBILE_DIVICES = ListKit.of(
            WINDOWS_PHONE,
            IPAD,
            IPOD,
            IPHONE,
            ANDROID,
            GOOGLE_TV,
            new Divice("htcFlyer", "htc_flyer"),
            new Divice("Symbian", "symbian(os)?"),
            new Divice("Blackberry", "blackberry"),
            new Divice("Android", "XiaoMi|MI\\s+")
    );

    /**
     * 支持的桌面平台类型
     */
    public static final List<Divice> DESKTOP_DIVICES = ListKit.of(
            new Divice("Windows", "windows"),
            new Divice("Mac", "(macintosh|darwin)"),
            new Divice("Linux", "linux"),
            new Divice("Wii", "wii"),
            new Divice("Playstation", "playstation"),
            new Divice("Java", "java")
    );

    /**
     * 支持的平台类型
     */
    public static final List<Divice> DIVICES;

    static {
        DIVICES = new ArrayList<>(13);
        DIVICES.addAll(MOBILE_DIVICES);
        DIVICES.addAll(DESKTOP_DIVICES);
    }

    /**
     * 构造
     *
     * @param name  平台名称
     * @param regex 关键字或表达式
     */
    public Divice(String name, String regex) {
        super(name, regex);
    }

    /**
     * 是否为移动平台
     *
     * @return 是否为移动平台
     */
    public boolean isMobile() {
        return MOBILE_DIVICES.contains(this);
    }

    /**
     * 是否为Iphone或者iPod设备
     *
     * @return 是否为Iphone或者iPod设备
     */
    public boolean isIPhoneOrIPod() {
        return IPHONE.equals(this) || IPOD.equals(this);
    }

    /**
     * 是否为Iphone或者iPod设备
     *
     * @return 是否为Iphone或者iPod设备
     */
    public boolean isIPad() {
        return IPAD.equals(this);
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
        return ANDROID.equals(this) || GOOGLE_TV.equals(this);
    }

}
