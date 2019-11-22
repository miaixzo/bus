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
package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.lang.exception.InstrumentException;

/**
 * 强转转换器
 *
 * @param <T> 强制转换到的类型
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class CastConverter<T> extends AbstractConverter<T> {

    private Class<T> targetType;

    @Override
    protected T convertInternal(Object value) {
        // 由于在AbstractConverter中已经有类型判断并强制转换,因此当在上一步强制转换失败时直接抛出异常
        throw new InstrumentException("Can not cast value to [{}]", this.targetType);
    }

    @Override
    public Class<T> getTargetType() {
        return this.targetType;
    }
}
