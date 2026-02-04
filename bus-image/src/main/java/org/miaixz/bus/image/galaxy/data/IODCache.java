/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.data;

import java.io.IOException;
import java.util.HashMap;

import org.miaixz.bus.image.IOD;

/**
 * A cache for {@link IOD} (Information Object Definition) instances, mapped by their URI. This class helps to avoid
 * redundant loading of IODs by storing previously loaded instances.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class IODCache {

    /**
     * The internal map that stores IOD instances, keyed by their URI.
     */
    private final HashMap<String, IOD> map = new HashMap<>();

    /**
     * Clears all cached {@link IOD} instances from this cache.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Retrieves an {@link IOD} instance from the cache based on its URI. If the IOD is not found in the cache, it is
     * loaded using {@link IOD#load(String)}, stored in the cache, and then returned.
     * 
     * @param uri The URI of the IOD to retrieve.
     * @return The {@link IOD} instance corresponding to the given URI.
     * @throws IOException if an I/O error occurs during loading of the IOD.
     */
    public IOD get(String uri) throws IOException {
        IOD iod = map.get(uri);
        if (iod == null)
            map.put(uri, iod = IOD.load(uri));
        return iod;
    }

}
