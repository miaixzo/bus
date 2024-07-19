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
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sequence extends ArrayList<Attributes> implements Value {

    private static final long serialVersionUID = -1L;

    private final Attributes parent;
    private final String privateCreator;
    private final int tag;
    private volatile int length = -1;
    private volatile boolean readOnly;

    Sequence(Attributes parent, String privateCreator, int tag, int initialCapacity) {
        super(initialCapacity);
        this.parent = parent;
        this.privateCreator = privateCreator;
        this.tag = tag;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
        for (Attributes attrs : this) {
            attrs.setReadOnly();
        }
    }

    private void ensureModifiable() {
        if (readOnly) {
            throw new UnsupportedOperationException("read-only");
        }
    }

    public final Attributes getParent() {
        return parent;
    }

    private void setParent(Collection<? extends Attributes> c) {
        boolean bigEndian = parent.bigEndian();
        for (Attributes attrs : c) {
            if (attrs.bigEndian() != bigEndian)
                throw new IllegalArgumentException(
                        "Endian of Item must match Endian of parent Data Set");
            if (!attrs.isRoot())
                throw new IllegalArgumentException(
                        "Item already contained by Sequence");
        }
        for (Attributes attrs : c)
            attrs.setParent(parent, privateCreator, tag);
    }

    public void trimToSize(boolean recursive) {
        ensureModifiable();
        super.trimToSize();
        if (recursive)
            for (Attributes attrs : this)
                attrs.trimToSize(recursive);
    }

    @Override
    public int indexOf(Object o) {
        ListIterator<Attributes> it = listIterator();
        while (it.hasNext())
            if (it.next() == o)
                return it.previousIndex();
        return -1;
    }

    @Override
    public boolean add(Attributes attrs) {
        ensureModifiable();
        return super.add(attrs.setParent(parent, privateCreator, tag));
    }

    @Override
    public void add(int index, Attributes attrs) {
        ensureModifiable();
        super.add(index, attrs.setParent(parent, privateCreator, tag));
    }

    @Override
    public boolean addAll(Collection<? extends Attributes> c) {
        ensureModifiable();
        setParent(c);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Attributes> c) {
        ensureModifiable();
        setParent(c);
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        ensureModifiable();
        for (Attributes attrs : this)
            attrs.setParent(null, null, 0);
        super.clear();
    }

    @Override
    public Attributes remove(int index) {
        ensureModifiable();
        return super.remove(index).setParent(null, null, 0);
    }

    @Override
    public boolean remove(Object o) {
        ensureModifiable();
        if (o instanceof Attributes && super.remove(o)) {
            ((Attributes) o).setParent(null, null, 0);
            return true;
        }
        return false;
    }

    @Override
    public Attributes set(int index, Attributes attrs) {
        ensureModifiable();
        return super.set(index, attrs.setParent(parent, privateCreator, tag));
    }

    @Override
    public String toString() {
        return size() + " Items";
    }

    @Override
    public int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
        int len = 0;
        for (Attributes item : this) {
            len += 8 + item.calcLength(encOpts, explicitVR);
            if (item.isEmpty() ? encOpts.undefEmptyItemLength
                    : encOpts.undefItemLength)
                len += 8;
        }
        if (isEmpty() ? encOpts.undefEmptySequenceLength
                : encOpts.undefSequenceLength)
            len += 8;
        length = len;
        return len;
    }

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

    @Override
    public void writeTo(ImageOutputStream out, VR vr) throws IOException {
        for (Attributes item : this)
            item.writeItemTo(out);
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }

}
