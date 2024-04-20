/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image.galaxy.data;

import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NullifyAttributesCoercion implements AttributesCoercion {

    private final int[] nullifyTags;
    private final AttributesCoercion next;

    public NullifyAttributesCoercion(int[] nullifyTags, AttributesCoercion next) {
        this.nullifyTags = Objects.requireNonNull(nullifyTags);
        this.next = next;
    }

    public static AttributesCoercion valueOf(int[] nullifyTags, AttributesCoercion next) {
        return null != nullifyTags && nullifyTags.length > 0
                ? new NullifyAttributesCoercion(nullifyTags, next)
                : next;
    }

    @Override
    public String remapUID(String uid) {
        return null != next ? next.remapUID(uid) : uid;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) {
        VR.Holder vr = new VR.Holder();
        for (int nullifyTag : nullifyTags) {
            Object value = attrs.getValue(nullifyTag, vr);
            if (null != value && value != Value.NULL) {
                if (null != modified)
                    modified.setValue(nullifyTag, vr.vr, attrs.remove(nullifyTag));
                attrs.setNull(nullifyTag, vr.vr);
            }
        }
        if (null != next)
            next.coerce(attrs, modified);
    }

}
