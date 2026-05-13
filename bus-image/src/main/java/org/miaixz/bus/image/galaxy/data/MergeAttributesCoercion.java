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

/**
 * Represents the MergeAttributesCoercion type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MergeAttributesCoercion implements AttributesCoercion {

    /**
     * The new attrs value.
     */
    private final Attributes newAttrs;

    /**
     * The next value.
     */
    private final AttributesCoercion next;

    /**
     * Creates a new instance.
     *
     * @param mergedAttrs the merged attrs.
     * @param next        the next.
     */
    public MergeAttributesCoercion(Attributes mergedAttrs, AttributesCoercion next) {
        this.newAttrs = mergedAttrs;
        this.next = next;
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
        Attributes.unifyCharacterSets(attrs, newAttrs);
        if (modified != null) {
            attrs.update(UpdatePolicy.OVERWRITE, newAttrs, modified);
        } else {
            attrs.addAll(newAttrs);
        }
        if (next != null)
            next.coerce(attrs, modified);
    }

}
