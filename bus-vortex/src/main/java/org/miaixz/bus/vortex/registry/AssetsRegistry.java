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
package org.miaixz.bus.vortex.registry;

import org.miaixz.bus.vortex.Assets;

/**
 * A specialized, in-memory registry for managing API {@link Assets}.
 * <p>
 * This class extends the generic {@link AbstractRegistry} and configures it to specifically handle {@code Assets}
 * objects. Its primary responsibility is to define the unique key used to store and retrieve each asset.
 *
 * @author Kimi Liu
 * @see AbstractRegistry
 * @see Assets
 * @since Java 17+
 */
public class AssetsRegistry extends AbstractRegistry<Assets> {

    /**
     * Constructs an {@code AssetsRegistry} and configures its key generation strategy.
     * <p>
     * The unique key for each asset is defined as the concatenation of its method name and version (e.g.,
     * "user.getProfile1.0.0"). This composite key allows for quick lookups during request processing.
     */
    public AssetsRegistry() {
        setKeyGenerator(asset -> asset.getMethod() + asset.getVersion());
    }

    /**
     * A convenience method to retrieve an asset based on its method name and version.
     * <p>
     * This method constructs the composite key and delegates the lookup to the underlying map in the parent class.
     *
     * @param method  The method name of the asset.
     * @param version The version string of the asset.
     * @return The matching {@link Assets} object, or {@code null} if no asset is found for the given combination.
     */
    public Assets get(String method, String version) {
        return get(method + version);
    }

}
