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

import java.util.Objects;

/**
 * Represents the NullifyAttributesCoercion type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NullifyAttributesCoercion implements AttributesCoercion {

    /**
     * The nullify tags value.
     */
    private final int[] nullifyTags;

    /**
     * The next value.
     */
    private final AttributesCoercion next;

    /**
     * Creates a new instance.
     *
     * @param nullifyTags the nullify tags.
     * @param next        the next.
     */
    public NullifyAttributesCoercion(int[] nullifyTags, AttributesCoercion next) {
        this.nullifyTags = Objects.requireNonNull(nullifyTags);
        this.next = next;
    }

    /**
     * Executes the value of operation.
     *
     * @param nullifyTags the nullify tags.
     * @param next        the next.
     * @return the operation result.
     */
    public static AttributesCoercion valueOf(int[] nullifyTags, AttributesCoercion next) {
        return nullifyTags != null && nullifyTags.length > 0 ? new NullifyAttributesCoercion(nullifyTags, next) : next;
    }

    /**
     * Executes the remap uid operation.
     *
     * @param uid the uid.
     * @return the operation result.
     */
    @Override
    public String remapUID(String uid) {
        return next != null ? next.remapUID(uid) : uid;
    }

    /**
     * Executes the coerce operation.
     *
     * @param attrs    the attrs.
     * @param modified the modified.
     * @throws Exception if the operation cannot be completed.
     */
    @Override
    public void coerce(Attributes attrs, Attributes modified) throws Exception {
        VR.Holder vr = new VR.Holder();
        for (int nullifyTag : nullifyTags) {
            Object value = attrs.getValue(nullifyTag, vr);
            if (value != null && value != Value.NULL) {
                Object originalValue = attrs.setNull(nullifyTag, vr.vr);
                if (modified != null)
                    modified.setValue(nullifyTag, vr.vr, originalValue);
            }
        }
        if (next != null)
            next.coerce(attrs, modified);
    }

}
