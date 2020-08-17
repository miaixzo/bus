/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.http.metric.anget;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.CollKit;

import java.util.List;

/**
 * 网络操作系统
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8+
 */
public class NOS extends UserAgent {

    /**
     * 未知
     */
    public static final NOS UNKNOWN = new NOS(Normal.UNKNOWN, null);

    /**
     * 支持的引擎类型
     */
    public static final List<NOS> OSES = CollKit.newArrayList(
            new NOS("Windows 10 or Windows Server 2016", "windows nt 10\\.0"),
            new NOS("Windows 8.1 or Winsows Server 2012R2", "windows nt 6\\.3"),
            new NOS("Windows 8 or Winsows Server 2012", "windows nt 6\\.2"),
            new NOS("Windows Vista", "windows nt 6\\.0"),
            new NOS("Windows 7 or Windows Server 2008R2", "windows nt 6\\.1"),
            new NOS("Windows 2003", "windows nt 5\\.2"),
            new NOS("Windows XP", "windows nt 5\\.1"),
            new NOS("Windows 2000", "windows nt 5\\.0"),
            new NOS("Windows Phone", "windows (ce|phone|mobile)( os)?"),
            new NOS("Windows", "windows"),
            new NOS("OSX", "os x (\\d+)[._](\\d+)"),
            new NOS("Android", "Android"),
            new NOS("Linux", "linux"),
            new NOS("Wii", "wii"),
            new NOS("PS3", "playstation 3"),
            new NOS("PSP", "playstation portable"),
            new NOS("iPad", "\\(iPad.*os (\\d+)[._](\\d+)"),
            new NOS("iPhone", "\\(iPhone.*os (\\d+)[._](\\d+)"),
            new NOS("YPod", "iPod touch[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)"),
            new NOS("YPad", "iPad[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)"),
            new NOS("YPhone", "iPhone[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)"),
            new NOS("Symbian", "symbian(os)?"),
            new NOS("Darwin", "Darwin\\/([\\d\\w\\.\\-]+)"),
            new NOS("Adobe Air", "AdobeAir\\/([\\d\\w\\.\\-]+)"),
            new NOS("Java", "Java[\\s]+([\\d\\w\\.\\-]+)")
    );

    /**
     * 构造
     *
     * @param name  系统名称
     * @param regex 关键字或表达式
     */
    public NOS(String name, String regex) {
        super(name, regex);
    }

}
