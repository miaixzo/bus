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
package org.miaixz.bus.image;

import org.miaixz.bus.image.builtin.DefaultEditors;
import org.miaixz.bus.image.galaxy.EditorContext;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Represents an operation that modifies a set of DICOM attributes. As a functional interface, it is intended to be
 * implemented by a lambda expression or method reference that encapsulates the editing logic.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface Editors {

    /**
     * Performs the editing operation on the given DICOM attributes.
     *
     * @param attributes The DICOM attributes to be modified.
     * @param context    The context in which the editor is being applied, which may contain additional information or
     *                   state.
     */
    void apply(Attributes attributes, EditorContext context);

    /**
     * Creates the default bus-image editor for attribute overriding.
     *
     * @param tagToOverride DICOM attributes that should override matching target attributes.
     * @return The default editor implementation.
     */
    static Editors defaults(Attributes tagToOverride) {
        return defaults(false, null, tagToOverride);
    }

    /**
     * Creates the default bus-image editor for optional UID regeneration and attribute overriding.
     *
     * @param generateUIDs  Whether UI values should be rewritten.
     * @param tagToOverride DICOM attributes that should override matching target attributes.
     * @return The default editor implementation.
     */
    static Editors defaults(boolean generateUIDs, Attributes tagToOverride) {
        return defaults(generateUIDs, null, tagToOverride);
    }

    /**
     * Creates the default bus-image editor with a deterministic UID rewrite key.
     *
     * @param generateUIDs  Whether UI values should be rewritten.
     * @param globalKey     Hex encoded HMAC key for deterministic UID rewriting.
     * @param tagToOverride DICOM attributes that should override matching target attributes.
     * @return The default editor implementation.
     */
    static Editors defaults(boolean generateUIDs, String globalKey, Attributes tagToOverride) {
        return new DefaultEditors(generateUIDs, globalKey, tagToOverride);
    }

}
