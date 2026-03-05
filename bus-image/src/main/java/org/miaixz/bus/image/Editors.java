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

import org.miaixz.bus.image.galaxy.EditorContext;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Represents an operation that modifies a set of DICOM attributes. As a functional interface, it is intended to be
 * implemented by a lambda expression or method reference that encapsulates the editing logic.
 *
 * @author Kimi Liu
 * @since Java 17+
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

}
