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
package org.miaixz.bus.setting.metric.setting;

import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.Map;

/**
 * Setting工具类
 * 提供静态方法获取配置文件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Setting {

    /**
     * 配置文件缓存
     */
    private static final Map<String, org.miaixz.bus.setting.Setting> CACHE_SETTING = new SafeConcurrentHashMap<>();

    /**
     * 获取当前环境下的配置文件
     * name可以为不包括扩展名的文件名（默认.setting为结尾），也可以是文件名全称
     *
     * @param name 文件名，如果没有扩展名，默认为.setting
     * @return 当前环境下配置文件
     */
    public static org.miaixz.bus.setting.Setting get(final String name) {
        return CACHE_SETTING.computeIfAbsent(name, (filePath) -> {
            final String extName = FileName.extName(filePath);
            if (StringKit.isEmpty(extName)) {
                filePath = filePath + "." + org.miaixz.bus.setting.Setting.EXT_NAME;
            }
            return new org.miaixz.bus.setting.Setting(filePath, true);
        });
    }

    /**
     * 获取给定路径找到的第一个配置文件
     * name可以为不包括扩展名的文件名（默认.setting为结尾），也可以是文件名全称
     *
     * @param names 文件名，如果没有扩展名，默认为.setting
     * @return 当前环境下配置文件
     */
    public static org.miaixz.bus.setting.Setting getFirstFound(final String... names) {
        for (final String name : names) {
            try {
                return get(name);
            } catch (final InternalException e) {
                //ignore
            }
        }
        return null;
    }

}
