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
package org.aoju.bus.core.reflect;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.TypeUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * <p>Type literal comparable to {@code javax.enterprise.util.TypeLiteral},
 * made generally available outside the JEE context. Allows the passing around of
 * a "token" that represents a type in a typesafe manner, as opposed to
 * passing the (non-parameterized) {@link Type} object itself. Consider:</p>
 * <p>
 * You might see such a typesafe API as:
 * <pre>
 * class Typesafe {
 *   &lt;T&gt; T obtain(Class&lt;T&gt; type, ...);
 * }
 * </pre>
 * Consumed in the manner of:
 * <pre>
 * Foo foo = typesafe.obtain(Foo.class, ...);
 * </pre>
 * Yet, you run into problems when you want to do this with a parameterized type:
 * <pre>
 * List&lt;String&gt; listOfString = typesafe.obtain(List.class, ...); // could only give us a raw List
 * </pre>
 * {@code java.lang.reflect.Type} might provide some value:
 * <pre>
 * Type listOfStringType = ...; // firstly, how to obtain this? Doable, but not straightforward.
 * List&lt;String&gt; listOfString = (List&lt;String&gt;) typesafe.obtain(listOfStringType, ...); // nongeneric Type would necessitate a cast
 * </pre>
 * The "type literal" concept was introduced to provide an alternative, i.e.:
 * <pre>
 * class Typesafe {
 *   &lt;T&gt; T obtain(TypeLiteral&lt;T&gt; type, ...);
 * }
 * </pre>
 * Consuming code looks like:
 * <pre>
 * List&lt;String&gt; listOfString = typesafe.obtain(new TypeLiteral&lt;List&lt;String&gt;&gt;() {}, ...);
 * </pre>
 * <p>
 * This has the effect of "jumping up" a level to tie a {@code java.lang.reflect.Type}
 * to a type variable while simultaneously making it short work to obtain a
 * {@code Type} instance for any given type, inline.
 * </p>
 * <p>Additionally {@link TypeLiteral} implements the {@link Typed} interface which
 * is a generalization of this concept, and which may be implemented in custom classes.
 * It is suggested that APIs be defined in terms of the interface, in the following manner:
 * </p>
 * <pre>
 *   &lt;T&gt; T obtain(Typed&lt;T&gt; typed, ...);
 * </pre>
 *
 * @author Kimi Liu
 * @version 3.6.0
 * @since JDK 1.8
 */
public abstract class TypeLiteral<T> implements Typed<T> {

    private static final TypeVariable<Class<TypeLiteral>> T = TypeLiteral.class.getTypeParameters()[0];

    /**
     * Represented type.
     */
    public final Type value;

    private final String toString;

    /**
     * The default constructor.
     */
    protected TypeLiteral() {
        this.value =
                Assert.notNull(TypeUtils.getTypeArguments(getClass(), TypeLiteral.class).get(T),
                        "%s does not assign type parameter %s", getClass(), TypeUtils.toLongString(T));

        this.toString = String.format("%s<%s>", TypeLiteral.class.getSimpleName(), TypeUtils.toString(value));
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeLiteral)) {
            return false;
        }
        final TypeLiteral<?> other = (TypeLiteral<?>) obj;
        return TypeUtils.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return 37 << 4 | value.hashCode();
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public Type getType() {
        return value;
    }

}
