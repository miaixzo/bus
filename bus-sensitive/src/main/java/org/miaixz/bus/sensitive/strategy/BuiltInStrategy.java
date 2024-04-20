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
package org.miaixz.bus.sensitive.strategy;

import org.miaixz.bus.sensitive.Context;
import org.miaixz.bus.sensitive.provider.AbstractProvider;

/**
 * 用于标识为系统内置的注解实现方式
 * <p>
 * 这个类的实现并不重要,只是为了尽可能降低 annotation 对于实现的依赖
 * 注意：如果不是系统内置的注解,请勿使用这个标识,否则无法找到对应实现
 * 在 hibernate-validator 中使用的是数组,然后默认指定 {},但是缺陷也很明显,
 * 明明是数组,实现却只能是一个
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BuiltInStrategy extends AbstractProvider {

    @Override
    public Object build(Object object, Context context) {
        return null;
    }

}
