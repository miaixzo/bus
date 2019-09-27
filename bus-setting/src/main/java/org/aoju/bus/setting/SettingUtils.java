/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.setting;

import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Setting工具类
 * 提供静态方法获取配置文件
 *
 * @author Kimi Liu
 * @version 3.6.0
 * @since JDK 1.8
 */
public class SettingUtils {

    /**
     * 配置文件缓存
     */
    private static Map<String, Setting> settingMap = new ConcurrentHashMap<>();
    private static Object lock = new Object();

    /**
     * 获取当前环境下的配置文件
     * name可以为不包括扩展名的文件名（默认.setting为结尾），也可以是文件名全称
     *
     * @param name 文件名，如果没有扩展名，默认为.setting
     * @return 当前环境下配置文件
     */
    public static Setting get(String name) {
        Setting setting = settingMap.get(name);
        if (null == setting) {
            synchronized (lock) {
                setting = settingMap.get(name);
                if (null == setting) {
                    String filePath = name;
                    String extName = FileUtils.extName(filePath);
                    if (StringUtils.isEmpty(extName)) {
                        filePath = filePath + ".setting";
                    }
                    setting = new Setting(filePath, true);
                    settingMap.put(name, setting);
                }
            }
        }
        return setting;
    }

}
