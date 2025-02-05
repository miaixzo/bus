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
package org.aopalliance.intercept;

/**
 * Intercepts the construction of a new object. The user should implement the {@link #construct(ConstructorInvocation)}
 * method to modify the original behavior. E.g. the following class implements a singleton interceptor (allows only one
 * unique instance for the intercepted class):
 *
 * <pre class=code>
 * class DebuggingInterceptor implements ConstructorInterceptor {
 *     Object instance = null;
 *
 *     Object construct(ConstructorInvocation i) throws Throwable {
 *         if (instance == null) {
 *             return instance = i.proceed();
 *         } else {
 *             throw new Exception("singleton does not allow multiple instance");
 *         }
 *     }
 * }
 * </pre>
 */
public interface ConstructorInterceptor extends Interceptor {

    /**
     * Implement this method to perform extra treatments before and after the construction of a new object. Polite
     * implementations would certainly like to invoke {@link Joinpoint#proceed()}.
     *
     * @param invocation the construction joinpoint
     * @return the newly created object, which is also the result of the call to {@link Joinpoint#proceed()}; might be
     *         replaced by the interceptor
     * @throws Throwable if the interceptors or the target object throws an exception
     */
    Object construct(ConstructorInvocation invocation) throws Throwable;

}
