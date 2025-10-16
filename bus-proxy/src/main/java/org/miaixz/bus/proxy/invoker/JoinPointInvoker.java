/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.proxy.invoker;

import java.lang.reflect.Method;

/**
 * An implementation of {@link ProxyChain} that adapts a generic {@link Invocation}. This class acts as a bridge,
 * allowing different invocation contexts to be used within the proxy chain framework.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JoinPointInvoker implements ProxyChain {

    /**
     * The target object of the invocation.
     */
    private final Object target;

    /**
     * The underlying invocation context.
     */
    private final Invocation invocation;

    /**
     * Constructs a new JoinPointInvoker.
     *
     * @param target     The target object.
     * @param invocation The invocation context.
     */
    public JoinPointInvoker(Object target, Invocation invocation) {
        this.target = target;
        this.invocation = invocation;
    }

    @Override
    public Object getProxy() {
        return invocation.getProxy();
    }

    @Override
    public Method getMethod() {
        return invocation.getMethod();
    }

    @Override
    public Object[] getArguments() {
        return invocation.getArguments();
    }

    @Override
    public Object proceed() throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object[] getNames() {
        // This implementation returns the argument values, not their names.
        return getArguments();
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return invocation.getMethod().invoke(target, args);
    }

}
