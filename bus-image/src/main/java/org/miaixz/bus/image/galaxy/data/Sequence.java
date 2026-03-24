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
 * @author Kimi Liu
 * @since Java 21+
 */
public class Sequence extends ArrayList<Attributes> implements Value {

    @Serial
    private static final long serialVersionUID = 2852273702208L;

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
                throw new IllegalArgumentException("Endian of Item must match Endian of parent Data Set");
            if (!attrs.isRoot())
                throw new IllegalArgumentException("Item already contained by Sequence");
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
            if (item.isEmpty() ? encOpts.undefEmptyItemLength : encOpts.undefItemLength)
                len += 8;
        }
        if (isEmpty() ? encOpts.undefEmptySequenceLength : encOpts.undefSequenceLength)
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
