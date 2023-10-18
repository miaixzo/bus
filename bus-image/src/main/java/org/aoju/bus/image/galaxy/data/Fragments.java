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
package org.aoju.bus.image.galaxy.data;

import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.io.ImageEncodingOptions;
import org.aoju.bus.image.galaxy.io.ImageOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Fragments extends ArrayList<Object> implements Value {

    private final VR vr;
    private final boolean bigEndian;

    public Fragments(VR vr, boolean bigEndian, int initialCapacity) {
        super(initialCapacity);
        this.vr = vr;
        this.bigEndian = bigEndian;
    }

    public final VR vr() {
        return vr;
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    @Override
    public String toString() {
        return size() + " Fragments";
    }

    @Override
    public boolean add(Object frag) {
        add(size(), frag);
        return true;
    }

    @Override
    public void add(int index, Object frag) {
        super.add(index,
                null == frag || (frag instanceof byte[]) && ((byte[]) frag).length == 0
                        ? NULL
                        : frag);
    }

    @Override
    public boolean addAll(Collection<? extends Object> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Object> c) {
        for (Object o : c)
            add(index++, o);
        return !c.isEmpty();
    }

    @Override
    public void writeTo(ImageOutputStream out, VR vr)
            throws IOException {
        for (Object frag : this)
            out.writeAttribute(Tag.Item, vr, frag, null);
    }

    @Override
    public int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
        int len = 0;
        for (Object frag : this) {
            len += 8;
            if (frag instanceof Value)
                len += ((Value) frag).calcLength(encOpts, explicitVR, vr);
            else
                len += (((byte[]) frag).length + 1) & ~1;
        }
        return len;
    }

    @Override
    public int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return -1;
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (null == object)
            return false;
        if (getClass() != object.getClass())
            return false;

        Fragments other = (Fragments) object;
        if (bigEndian != other.bigEndian)
            return false;
        if (vr != other.vr)
            return false;

        ListIterator<Object> e1 = listIterator();
        ListIterator<Object> e2 = other.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            if (!itemsEqual(o1, o2))
                return false;
        }
        return !e1.hasNext() && !e2.hasNext();
    }

    @Override
    public int hashCode() {
        final int prime = 31;

        int hashCode = 1;
        for (Object e : this)
            hashCode = prime * hashCode + itemHashCode(e);

        hashCode = prime * hashCode + (bigEndian ? 1231 : 1237);
        hashCode = prime * hashCode + ((null == vr) ? 0 : vr.hashCode());
        return hashCode;
    }

    private boolean itemsEqual(Object o1, Object o2) {

        if (null == o1) {
            return null == o2;
        } else {
            if (o1 instanceof byte[]) {
                if (o2 instanceof byte[] && ((byte[]) o1).length == ((byte[]) o2).length) {
                    return Arrays.equals((byte[]) o1, (byte[]) o2);
                } else {
                    return false;
                }
            } else {
                return o1.equals(o2);
            }
        }
    }

    private int itemHashCode(Object e) {
        if (null == e) {
            return 0;
        } else {
            if (e instanceof byte[])
                return Arrays.hashCode((byte[]) e);
            else
                return e.hashCode();
        }
    }

}
