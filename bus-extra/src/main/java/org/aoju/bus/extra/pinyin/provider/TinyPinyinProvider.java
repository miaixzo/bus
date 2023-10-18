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
package org.aoju.bus.extra.pinyin.provider;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * TinyPinyin 引擎
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TinyPinyinProvider extends AbstractPinyinProvider {

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
    public TinyPinyinProvider(Pinyin.Config config) {
        Pinyin.init(config);
    }

    @Override
    public String getPinyin(char c) {
        if (false == Pinyin.isChinese(c)) {
            return String.valueOf(c);
        }
        return Pinyin.toPinyin(c).toLowerCase();
    }

    @Override
    public String getPinyin(String text, String separator) {
        return Pinyin.toPinyin(text, separator).toLowerCase();
    }

}
