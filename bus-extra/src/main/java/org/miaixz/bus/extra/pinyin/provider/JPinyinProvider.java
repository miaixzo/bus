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
package org.miaixz.bus.extra.pinyin.provider;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.ArrayKit;

/**
 * Jpinyin 引擎
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JPinyinProvider extends AbstractPinyinProvider {

    /**
     * 设置汉子拼音输出的格式
     */
    PinyinFormat format;

    public JPinyinProvider() {
        this(null);
    }

    public JPinyinProvider(PinyinFormat format) {
        init(format);
    }

    public void init(PinyinFormat format) {
        if (null == format) {
            // 不加声调
            format = PinyinFormat.WITHOUT_TONE;
        }
        this.format = format;
    }

    @Override
    public String getPinyin(char c) {
        String[] results = PinyinHelper.convertToPinyinArray(c, format);
        return ArrayKit.isEmpty(results) ? String.valueOf(c) : results[0];
    }

    @Override
    public String getPinyin(String text, String separator) {
        try {
            return PinyinHelper.convertToPinyinString(text, separator, format);
        } catch (PinyinException e) {
            throw new InternalException(e);
        }
    }

}
