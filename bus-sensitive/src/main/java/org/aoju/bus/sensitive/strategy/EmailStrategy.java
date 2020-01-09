/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.annotation.Shield;
import org.aoju.bus.sensitive.provider.AbstractProvider;

/**
 * 邮箱脱敏策略
 * 脱敏规则：
 * 保留前三位,中间隐藏4位 其他正常显示
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public class EmailStrategy extends AbstractProvider {

    /**
     * 脱敏邮箱
     *
     * @param email 邮箱
     * @return 结果
     */
    private static String email(final String email, final String shadow) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }

        final int prefixLength = 3;

        final int atIndex = email.indexOf(Symbol.AT);
        String middle = StringUtils.fill(4, shadow);

        if (atIndex > 0) {
            int middleLength = atIndex - prefixLength;
            middle = StringUtils.repeat(shadow, middleLength);
        }
        return StringUtils.buildString(email, middle, prefixLength);
    }

    @Override
    public Object build(Object object, Context context) {
        if (ObjectUtils.isEmpty(object)) {
            return null;
        }
        final Shield shield = context.getShield();
        return this.email(ObjectUtils.isNull(object) ? Normal.EMPTY : object.toString(), shield.shadow());
    }

}
