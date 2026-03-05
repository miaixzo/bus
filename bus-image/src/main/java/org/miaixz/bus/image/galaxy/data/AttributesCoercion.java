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
 * Interface for coercing DICOM attributes. Implementations of this interface can modify or remap attributes within a
 * DICOM dataset.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AttributesCoercion {

    /**
     * A no-op implementation of {@code AttributesCoercion} that performs no coercion and no UID remapping.
     */
    AttributesCoercion NONE = new AttributesCoercion() {

        @Override
        public void coerce(Attributes attrs, Attributes modified) {
        }

        @Override
        public String remapUID(String uid) {
            return uid;
        }
    };

    /**
     * Coerces the given DICOM attributes. This method can modify the attributes in place or record changes in the
     * {@code modified} attributes object.
     * 
     * @param attrs    The attributes to coerce.
     * @param modified A collection to store modified attributes, or {@code null} if not needed.
     * @throws Exception if an error occurs during coercion.
     */
    void coerce(Attributes attrs, Attributes modified) throws Exception;

    /**
     * Remaps a given UID to a new UID. This is typically used for anonymization or de-identification purposes.
     * 
     * @param uid The original UID to remap.
     * @return The remapped UID, or the original UID if no remapping is performed.
     */
    String remapUID(String uid);

}
