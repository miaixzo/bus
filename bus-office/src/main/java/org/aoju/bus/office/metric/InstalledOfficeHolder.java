/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.metric;

import org.aoju.bus.office.Provider;

/**
 * 保存{@link OfficeManager}的唯一实例，
 * 当没有向转换器生成器提供office管理器时，
 * 创建的{@link Provider}将使用该实例.
 *
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
public final class InstalledOfficeHolder {

    private static OfficeManager instance;

    /**
     * 获取静态holder类的静态实例.
     *
     * @return 主默认的office管理器.
     */
    public static OfficeManager getInstance() {
        synchronized (InstalledOfficeHolder.class) {
            return instance;
        }
    }

    /**
     * 设置静态holder类的静态实例.
     *
     * @param manager 主默认的office管理器.
     * @return 以前安装的office管理器，如果没有安装office管理器，则为{@code null}.
     */
    public static OfficeManager setInstance(final OfficeManager manager) {
        synchronized (InstalledOfficeHolder.class) {
            final OfficeManager oldManager = instance;
            instance = manager;
            return oldManager;
        }
    }

}
