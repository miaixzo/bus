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
package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 中文名称脱敏策略：
 * <p>
 * 0. 少于等于1个字 直接返回
 * 1. 两个字 隐藏姓
 * 2. 三个及其以上 只保留第一个和最后一个 其他用星号代替
 *
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
public class NameStrategy implements StrategyProvider {

    /**
     * 脱敏中文名称
     *
     * @param chineseName 中文名称
     * @return 脱敏后的结果
     */
    public static String name(final String chineseName) {
        if (StringUtils.isEmpty(chineseName)) {
            return chineseName;
        }

        final int nameLength = chineseName.length();
        if (1 == nameLength) {
            return chineseName;
        }

        if (2 == nameLength) {
            return Symbol.STAR + chineseName.charAt(1);
        }

        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(chineseName.charAt(0));
        for (int i = 0; i < nameLength - 2; i++) {
            stringBuffer.append(Symbol.STAR);
        }
        stringBuffer.append(chineseName.charAt(nameLength - 1));
        return stringBuffer.toString();
    }

    @Override
    public Object build(Object object, Context context) {
        return this.name(ObjectUtils.isNull(object) ? Normal.EMPTY : object.toString());
    }

}
