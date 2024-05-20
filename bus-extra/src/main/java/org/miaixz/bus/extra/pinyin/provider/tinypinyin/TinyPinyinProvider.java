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
package org.miaixz.bus.extra.pinyin.provider.tinypinyin;

import com.github.promeg.pinyinhelper.Pinyin;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.pinyin.PinyinProvider;

/**
 * 封装了TinyPinyin的引擎。
 *
 * <p>
 * TinyPinyin(https://github.com/promeG/TinyPinyin)提供者未提交Maven中央库，
 * 因此使用
 * https://github.com/biezhi/TinyPinyin打包的版本
 * </p>
 *
 * <p>
 * 引入：
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;io.github.biezhi&lt;/groupId&gt;
 *     &lt;artifactId&gt;TinyPinyin&lt;/artifactId&gt;
 *     &lt;version&gt;2.0.3.RELEASE&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TinyPinyinProvider implements PinyinProvider {

    /**
     * 构造
     */
    public TinyPinyinProvider() {
        this(null);
    }

    /**
     * 构造
     *
     * @param config 配置
     */
    public TinyPinyinProvider(final Pinyin.Config config) {
        Pinyin.init(config);
    }

    @Override
    public String getPinyin(final char c) {
        if (!Pinyin.isChinese(c)) {
            return String.valueOf(c);
        }
        return Pinyin.toPinyin(c).toLowerCase();
    }

    @Override
    public String getPinyin(final String text, final String separator) {
        final String pinyin = Pinyin.toPinyin(text, separator);
        return StringKit.isEmpty(pinyin) ? pinyin : pinyin.toLowerCase();
    }

}
