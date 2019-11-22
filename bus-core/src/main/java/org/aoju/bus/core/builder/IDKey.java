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
package org.aoju.bus.core.builder;

/**
 * Wrap an identity key (System.identityHashCode())
 * so that an object can only be equal() to itself.
 * <p>
 * This is necessary to disambiguate the occasional duplicate
 * identityHashCodes that can occur.
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
final class IDKey {

    private final Object value;
    private final int id;

    /**
     * Constructor for IDKey
     *
     * @param _value The value
     */
    IDKey(final Object _value) {
        // This is the Object hash code
        id = System.identityHashCode(_value);
        // There have been some cases (LANG-459) that return the
        // same identity hash code for different objects.  So
        // the value is also added to disambiguate these cases.
        value = _value;
    }

    /**
     * returns hash code - i.e. the system identity hashcode.
     *
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        return id;
    }

    /**
     * checks if instances are equal
     *
     * @param other The other object to compare to
     * @return if the instances are for the same object
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof IDKey)) {
            return false;
        }
        final IDKey idKey = (IDKey) other;
        if (id != idKey.id) {
            return false;
        }
        // Note that identity equals is used.
        return value == idKey.value;
    }

}
