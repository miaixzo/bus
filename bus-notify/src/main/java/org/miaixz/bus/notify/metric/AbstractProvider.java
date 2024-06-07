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
package org.miaixz.bus.notify.metric;

import lombok.AllArgsConstructor;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.Provider;
import org.miaixz.bus.notify.magic.Message;
import org.miaixz.bus.notify.magic.Property;

import java.util.List;

/**
 * 抽象类
 *
 * @author Justubborn
 * @since Java 17+
 */
@AllArgsConstructor
public abstract class AbstractProvider<T extends Property, K extends Context> implements Provider<T> {

    protected K context;

    @Override
    public Message send(T entity) {
        return null;
    }

    @Override
    public Message send(T entity, List<String> mobile) {
        return null;
    }

    protected String getUrl(T property) {
        return getUrl(this.context, property);
    }

    protected String getUrl(K context, T entity) {
        return ObjectKit.defaultIfNull(context.getEndpoint(), entity.getUrl());
    }

}
