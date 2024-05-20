/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
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
package org.miaixz.bus.oauth;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.metric.AuthorizeProvider;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.Arrays;
import java.util.function.Function;

/**
 * 快捷的构建 Authorize
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Authorize {

    private String source;
    private Context context;
    private ExtendCache cache;
    private Complex[] complex;

    private Authorize() {

    }

    public static Authorize builder() {
        return new Authorize();
    }

    public Authorize source(String source) {
        this.source = source;
        return this;
    }

    public Authorize context(Context context) {
        this.context = context;
        return this;
    }

    public Authorize context(Function<String, Context> context) {
        this.context = context.apply(this.source);
        return this;
    }

    public Authorize cache(ExtendCache cache) {
        this.cache = cache;
        return this;
    }

    public Authorize complex(Complex... complex) {
        this.complex = complex;
        return this;
    }

    public AuthorizeProvider build() {
        if (StringKit.isEmpty(this.source) || null == this.context) {
            throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
        }
        // 默认的 Registry 和 开发者自定义的 Complex
        Complex[] complexes = this.concat(Registry.values(), this.complex);
        // 筛选符合条件的 Complex
        Complex complex = Arrays.stream(complexes).distinct()
                .filter(authSource -> authSource.getName().equalsIgnoreCase(this.source))
                .findAny()
                .orElseThrow(() -> new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode()));

        Class<? extends DefaultProvider> targetClass = complex.getTargetClass();
        if (null == targetClass) {
            throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
        }
        try {
            if (this.cache == null) {
                return targetClass.getDeclaredConstructor(Context.class).newInstance(this.context);
            } else {
                return targetClass.getDeclaredConstructor(Context.class, ExtendCache.class).newInstance(this.context, this.complex);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
        }
    }

    private Complex[] concat(Complex[] first, Complex[] second) {
        if (null == second || second.length == 0) {
            return first;
        }
        Complex[] result = new Complex[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
