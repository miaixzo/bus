/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.setting.metric;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.setting.magic.PopSetting;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Profile可以让我们定义一系列的配置信息,然后指定其激活条件
 * 此类中我们规范一套规则如下：
 * 默认的,我们读取${classpath}/default下的配置文件(*.setting文件),当调用setProfile方法时,指定一个profile,即可读取其目录下的配置文件
 * 比如我们定义几个profile：test,develop,production,分别代表测试环境、开发环境和线上环境,我希望读取数据库配置文件db.setting,那么：
 * <ol>
 * <li>test =  ${classpath}/test/db.setting</li>
 * <li>develop =  ${classpath}/develop/db.setting</li>
 * <li>production =  ${classpath}/production/db.setting</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Profile implements Serializable {

    /**
     * 默认环境
     */
    public static final String DEFAULT_PROFILE = "default";
    private static final long serialVersionUID = 1L;
    /**
     * 条件
     */
    private String profile;
    /**
     * 编码
     */
    private java.nio.charset.Charset charset;
    /**
     * 是否使用变量
     */
    private boolean useVar;
    /**
     * 配置文件缓存
     */
    private Map<String, PopSetting> settingMap = new ConcurrentHashMap<>();

    /**
     * 默认构造,环境使用默认的：default,编码UTF-8,不使用变量
     */
    public Profile() {
        this(DEFAULT_PROFILE);
    }

    /**
     * 构造,编码UTF-8,不使用变量
     *
     * @param profile 环境
     */
    public Profile(String profile) {
        this(profile, Charset.UTF_8, false);
    }

    /**
     * 构造
     *
     * @param profile 环境
     * @param charset 编码
     * @param useVar  是否使用变量
     */
    public Profile(String profile, java.nio.charset.Charset charset, boolean useVar) {
        this.profile = profile;
        this.charset = charset;
        this.useVar = useVar;
    }

    /**
     * 获取当前环境下的配置文件
     *
     * @param name 文件名,如果没有扩展名,默认为.setting
     * @return 当前环境下配置文件
     */
    public PopSetting getSetting(String name) {
        String nameForProfile = fixNameForProfile(name);
        PopSetting popSetting = settingMap.get(nameForProfile);
        if (null == popSetting) {
            popSetting = new PopSetting(nameForProfile, charset, useVar);
            settingMap.put(nameForProfile, popSetting);
        }
        return popSetting;
    }

    /**
     * 设置环境
     *
     * @param profile 环境
     * @return 自身
     */
    public Profile setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    /**
     * 设置编码
     *
     * @param charset 编码
     * @return 自身
     */
    public Profile setCharset(java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 设置是否使用变量
     *
     * @param useVar 变量
     * @return 自身
     */
    public Profile setUseVar(boolean useVar) {
        this.useVar = useVar;
        return this;
    }

    /**
     * 清空所有环境的配置文件
     *
     * @return 自身
     */
    public Profile clear() {
        this.settingMap.clear();
        return this;
    }

    /**
     * 修正文件名
     *
     * @param name 文件名
     * @return 修正后的文件名
     */
    private String fixNameForProfile(String name) {
        final String actralProfile = null == this.profile ? Normal.EMPTY : this.profile;
        if (StringKit.isNotBlank(name) && false == name.contains(Symbol.DOT)) {
            return StringKit.format("{}/{}.setting", actralProfile, name);
        }
        return StringKit.format("{}/{}", actralProfile);
    }

}
