/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.data;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

/**
 * Fragments are used for encapsulation of an encoded (=compressed) pixel data stream into the Pixel Data (7FE0,0010)
 * portion of the DICOM Data Set. They are encoded as a sequence of items with Value Representation OB. Each item is
 * either a byte[], {@link BulkData} or {@link Value#NULL}.
 * <p>
 * The first Item in the sequence of items before the encoded Pixel Data Stream is a Basic Offset Table item. The value
 * of the Basic Offset Table, however, is not required to be present. The first item is then {@link Value#NULL}.
 * </p>
 * <p>
 * Depending on the transfer syntax, a frame may be entirely contained within a single fragment, or may span multiple
 * fragments to support buffering during compression or to avoid exceeding the maximum size of a fixed length fragment.
 * A recipient can detect fragmentation of frames by comparing the number of fragments (the number of Items minus one
 * for the Basic Offset Table) with the number of frames.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Fragments extends ArrayList<Object> implements Value {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852262835172L;

    /**
     * The vr value.
     */
    private final VR vr;

    /**
     * The big endian value.
     */
    private final boolean bigEndian;

    /**
     * The read only value.
     */
    private volatile boolean readOnly;

    /**
     * Creates a new instance.
     *
     * @param vr              the vr.
     * @param bigEndian       the big endian.
     * @param initialCapacity the initial capacity.
     */
    public Fragments(VR vr, boolean bigEndian, int initialCapacity) {
        super(initialCapacity);
        this.vr = vr;
        this.bigEndian = bigEndian;
    }

    /**
     * Determines whether read only.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the read only.
     */
    public void setReadOnly() {
        this.readOnly = true;
    }

    /**
     * Executes the ensure modifiable operation.
     */
    private void ensureModifiable() {
        if (readOnly) {
            throw new UnsupportedOperationException("read-only");
        }
    }

    /**
     * Executes the vr operation.
     *
     * @return the operation result.
     */
    public final VR vr() {
        return vr;
    }

    /**
     * Executes the big endian operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean bigEndian() {
        return bigEndian;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return size() + " Fragments";
    }

    /**
     * Executes the add operation.
     *
     * @param frag the frag.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean add(Object frag) {
        add(size(), frag);
        return true;
    }

    /**
     * Executes the add operation.
     *
     * @param index the index.
     * @param frag  the frag.
     */
    @Override
    public void add(int index, Object frag) {
        ensureModifiable();
        super.add(index, frag == null || (frag instanceof byte[]) && ((byte[]) frag).length == 0 ? Value.NULL : frag);
    }

    /**
     * Adds the all.
     *
     * @param c the c.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean addAll(Collection<? extends Object> c) {
        return addAll(size(), c);
    }

    /**
     * Adds the all.
     *
     * @param index the index.
     * @param c     the c.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean addAll(int index, Collection<? extends Object> c) {
        ensureModifiable();
        for (Object o : c)
            add(index++, o);
        return !c.isEmpty();
    }

    /**
     * Writes the to.
     *
     * @param out the out.
     * @param vr  the vr.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void writeTo(ImageOutputStream out, VR vr) throws IOException {
        for (Object frag : this)
            out.writeAttribute(Tag.Item, vr, frag, null);
    }

    /**
     * Executes the calc length operation.
     *
     * @param encOpts    the enc opts.
     * @param explicitVR the explicit vr.
     * @param vr         the vr.
     * @return the operation result.
     */
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

    /**
     * Gets the encoded length.
     *
     * @param encOpts    the enc opts.
     * @param explicitVR the explicit vr.
     * @param vr         the vr.
     * @return the encoded length.
     */
    @Override
    public int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return -1;
    }

    /**
     * Converts this value to bytes.
     *
     * @param vr        the vr.
     * @param bigEndian the big endian.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
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

    /**
     * Returns the hash code.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public int hashCode() {
        final int prime = 31;

        int hashCode = 1;
        for (Object e : this)
            hashCode = prime * hashCode + itemHashCode(e);

        hashCode = prime * hashCode + (bigEndian ? 1231 : 1237);
        hashCode = prime * hashCode + ((vr == null) ? 0 : vr.hashCode());
        return hashCode;
    }

    /**
     * Executes the items equal operation.
     *
     * @param o1 the o1.
     * @param o2 the o2.
     * @return true if the condition is met; otherwise false.
     */
    private boolean itemsEqual(Object o1, Object o2) {

        if (o1 == null) {
            return o2 == null;
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

    /**
     * Executes the item hash code operation.
     *
     * @param e the e.
     * @return the operation result.
     */
    private int itemHashCode(Object e) {
        if (e == null) {
            return 0;
        } else {
            if (e instanceof byte[])
                return Arrays.hashCode((byte[]) e);
            else
                return e.hashCode();
        }
    }

}
