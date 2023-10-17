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
package org.aoju.bus.proxy.provider;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.proxy.Builder;
import org.aoju.bus.proxy.Provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 仅对给定的{@link Cloneable}对象调用clone()(反射性地)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CloningProvider implements Provider {

    private final Cloneable cloneable;
    private Method cloneMethod;

    public CloningProvider(Cloneable cloneable) {
        this.cloneable = cloneable;
    }

    private synchronized Method getCloneMethod() {
        if (null == cloneMethod) {
            try {
                cloneMethod = cloneable.getClass().getMethod("clone", Builder.EMPTY_ARGUMENT_TYPES);
            } catch (NoSuchMethodException e) {
                throw new InternalException(
                        "Class " + cloneable.getClass().getName() + " does not have a public clone() method.");
            }
        }
        return cloneMethod;
    }

    public Object getObject() {
        try {
            return getCloneMethod().invoke(cloneable, Builder.EMPTY_ARGUMENTS);
        } catch (IllegalAccessException e) {
            throw new InternalException(
                    "Class " + cloneable.getClass().getName() + " does not have a public clone() method.", e);
        } catch (InvocationTargetException e) {
            throw new InternalException(
                    "Attempt to clone object of type " + cloneable.getClass().getName() + " threw an exception.", e);
        }
    }

}
