/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.lang.reflect.AccessibleObject;

/**
 * This interface represents a generic runtime joinpoint (in the AOP terminology). A runtime joinpoint is an
 * <i>event</i> that occurs on a static joinpoint (i.e. a location in a program). For instance, an invocation is the
 * runtime joinpoint on a method (static joinpoint). The static part of a given joinpoint can be generically retrieved
 * using the {@link #getStaticPart()} method. In the context of an interception framework, a runtime joinpoint is then
 * the reification of an access to an accessible object (a method, a constructor, a field), i.e. the static part of the
 * joinpoint. It is passed to the interceptors that are installed on the static joinpoint.
 *
 * @see Interceptor
 */
public interface Joinpoint {

    /**
     * Proceed to the next interceptor in the chain. The implementation and the semantics of this method depends on the
     * actual joinpoint type (see the children interfaces).
     *
     * @return see the children interfaces' proceed definition
     * @throws Throwable if the joinpoint throws an exception
     */
    Object proceed() throws Throwable;

    /**
     * Return the object that holds the current joinpoint's static part. For instance, the target object for an
     * invocation.
     *
     * @return the object (can be null if the accessible object is static)
     */
    Object getThis();

    /**
     * Return the static part of this joinpoint. The static part is an accessible object on which a chain of
     * interceptors is installed.
     */
    AccessibleObject getStaticPart();

}
