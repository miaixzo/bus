/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
