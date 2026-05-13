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
import java.util.Collection;
import java.util.ListIterator;

import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

/**
 * Represents the Sequence type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Sequence extends ArrayList<Attributes> implements Value {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852273702208L;

    /**
     * The parent value.
     */
    private final Attributes parent;

    /**
     * The private creator value.
     */
    private final String privateCreator;

    /**
     * The tag value.
     */
    private final int tag;

    /**
     * The length value.
     */
    private volatile int length = -1;

    /**
     * The read only value.
     */
    private volatile boolean readOnly;

    /**
     * Creates a new instance.
     *
     * @param parent          the parent.
     * @param privateCreator  the private creator.
     * @param tag             the tag.
     * @param initialCapacity the initial capacity.
     */
    Sequence(Attributes parent, String privateCreator, int tag, int initialCapacity) {
        super(initialCapacity);
        this.parent = parent;
        this.privateCreator = privateCreator;
        this.tag = tag;
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
        for (Attributes attrs : this) {
            attrs.setReadOnly();
        }
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
     * Gets the parent.
     *
     * @return the parent.
     */
    public final Attributes getParent() {
        return parent;
    }

    /**
     * Sets the parent.
     *
     * @param c the c.
     */
    private void setParent(Collection<? extends Attributes> c) {
        boolean bigEndian = parent.bigEndian();
        for (Attributes attrs : c) {
            if (attrs.bigEndian() != bigEndian)
                throw new IllegalArgumentException("Endian of Item must match Endian of parent Data Set");
            if (!attrs.isRoot())
                throw new IllegalArgumentException("Item already contained by Sequence");
        }
        for (Attributes attrs : c)
            attrs.setParent(parent, privateCreator, tag);
    }

    /**
     * Executes the trim to size operation.
     *
     * @param recursive the recursive.
     */
    public void trimToSize(boolean recursive) {
        ensureModifiable();
        super.trimToSize();
        if (recursive)
            for (Attributes attrs : this)
                attrs.trimToSize(recursive);
    }

    /**
     * Executes the index of operation.
     *
     * @param o the o.
     * @return the operation result.
     */
    @Override
    public int indexOf(Object o) {
        ListIterator<Attributes> it = listIterator();
        while (it.hasNext())
            if (it.next() == o)
                return it.previousIndex();
        return -1;
    }

    /**
     * Executes the add operation.
     *
     * @param attrs the attrs.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean add(Attributes attrs) {
        ensureModifiable();
        return super.add(attrs.setParent(parent, privateCreator, tag));
    }

    /**
     * Executes the add operation.
     *
     * @param index the index.
     * @param attrs the attrs.
     */
    @Override
    public void add(int index, Attributes attrs) {
        ensureModifiable();
        super.add(index, attrs.setParent(parent, privateCreator, tag));
    }

    /**
     * Adds the all.
     *
     * @param c the c.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean addAll(Collection<? extends Attributes> c) {
        ensureModifiable();
        setParent(c);
        return super.addAll(c);
    }

    /**
     * Adds the all.
     *
     * @param index the index.
     * @param c     the c.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean addAll(int index, Collection<? extends Attributes> c) {
        ensureModifiable();
        setParent(c);
        return super.addAll(index, c);
    }

    /**
     * Executes the clear operation.
     */
    @Override
    public void clear() {
        ensureModifiable();
        for (Attributes attrs : this)
            attrs.setParent(null, null, 0);
        super.clear();
    }

    /**
     * Executes the remove operation.
     *
     * @param index the index.
     * @return the operation result.
     */
    @Override
    public Attributes remove(int index) {
        ensureModifiable();
        return super.remove(index).setParent(null, null, 0);
    }

    /**
     * Executes the remove operation.
     *
     * @param o the o.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean remove(Object o) {
        ensureModifiable();
        if (o instanceof Attributes && super.remove(o)) {
            ((Attributes) o).setParent(null, null, 0);
            return true;
        }
        return false;
    }

    /**
     * Executes the set operation.
     *
     * @param index the index.
     * @param attrs the attrs.
     * @return the operation result.
     */
    @Override
    public Attributes set(int index, Attributes attrs) {
        ensureModifiable();
        return super.set(index, attrs.setParent(parent, privateCreator, tag));
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return size() + " Items";
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
        for (Attributes item : this) {
            len += 8 + item.calcLength(encOpts, explicitVR);
            if (item.isEmpty() ? encOpts.undefEmptyItemLength : encOpts.undefItemLength)
                len += 8;
        }
        if (isEmpty() ? encOpts.undefEmptySequenceLength : encOpts.undefSequenceLength)
            len += 8;
        length = len;
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
        if (isEmpty())
            return encOpts.undefEmptySequenceLength ? -1 : 0;

        if (encOpts.undefSequenceLength)
            return -1;

        if (length == -1)
            calcLength(encOpts, explicitVR, vr);

        return length;
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
        for (Attributes item : this)
            item.writeItemTo(out);
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

}
