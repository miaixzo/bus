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
package org.aoju.bus.limiter.intercept;

import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.limiter.resource.LimitedResourceSource;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 切点抽象定义
 *
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
abstract class LimitedResourceSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

    public LimitedResourceSourcePointcut() {
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        LimitedResourceSource limitedResourceSource = this.getLimitedResourceSource();
        boolean matched = limitedResourceSource != null && !CollUtils.isEmpty(limitedResourceSource.getLimitedResource(aClass, method));
        if (matched == true) {
            return matched;
        }
        return matched;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof LimitedResourceSourcePointcut)) {
            return false;
        } else {
            LimitedResourceSourcePointcut otherPc = (LimitedResourceSourcePointcut) other;
            return ObjectUtils.nullSafeEquals(this.getLimitedResourceSource(), otherPc.getLimitedResourceSource());
        }
    }

    public int hashCode() {
        return LimitedResourceSourcePointcut.class.hashCode();
    }

    public String toString() {
        return this.getClass().getName() + ": " + this.getLimitedResourceSource();
    }

    protected abstract LimitedResourceSource getLimitedResourceSource();
}
