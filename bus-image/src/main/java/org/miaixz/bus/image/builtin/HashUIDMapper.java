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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.UID;

/**
 * An implementation of the {@link UIDMapper} interface that generates a new, name-based UID by hashing the original
 * UID. This can be used for de-identification purposes where original UIDs need to be replaced with consistent but
 * non-identifiable UIDs.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HashUIDMapper implements UIDMapper {

    /**
     * Generates a name-based UID by hashing the input UID. The generated UID is consistent for the same input UID.
     *
     * @param uid The original UID string to be hashed.
     * @return A new name-based UID derived from the input UID.
     */
    @Override
    public String get(String uid) {
        return UID.createNameBasedUID(uid.getBytes());
    }

}
