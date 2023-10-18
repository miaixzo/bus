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
package org.aoju.bus.limiter.resource;

import java.util.Collection;
import java.util.Observable;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractLimitedResource extends Observable implements LimitedResource {

    protected String key;

    protected Collection<String> argumentInjectors;

    protected String fallback;

    protected String errorHandler;

    protected String limiter;

    public AbstractLimitedResource(String key, Collection<String> argumentInjectors, String fallback, String errorHandler, String limiter) {
        this.key = key;
        this.argumentInjectors = argumentInjectors;
        this.fallback = fallback;
        this.errorHandler = errorHandler;
        this.limiter = limiter;
    }


    @Override
    public String getKey() {
        return key;
    }


    @Override
    public String getLimiter() {
        return limiter;
    }

    @Override
    public String getFallback() {
        return fallback;
    }

    @Override
    public String getErrorHandler() {
        return errorHandler;
    }

    @Override
    public Collection<String> getArgumentInjectors() {
        return argumentInjectors;
    }

}
