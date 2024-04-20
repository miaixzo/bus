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
package org.miaixz.bus.setting.metric;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.setting.magic.PopSetting;

/**
 * 全局的Profile配置中心
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GlobalProfile {

    private GlobalProfile() {
    }

    /**
     * 设置全局环境
     *
     * @param profile 环境
     * @return {@link Profile}
     */
    public static Profile setProfile(String profile) {
        return Instances.singletion(Profile.class, profile);
    }

    /**
     * 获得全局的当前环境下对应的配置文件
     *
     * @param settingName 配置文件名,可以忽略默认后者(.setting)
     * @return {@link PopSetting}
     */
    public static PopSetting getSetting(String settingName) {
        return Instances.singletion(Profile.class).getSetting(settingName);
    }

}
