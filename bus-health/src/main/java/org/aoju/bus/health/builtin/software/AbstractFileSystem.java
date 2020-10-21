/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.builtin.software;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Config;

import java.util.Arrays;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.1.1
 * @since JDK 1.8+
 */
@ThreadSafe
public abstract class AbstractFileSystem implements FileSystem {

    public static final String OSHI_NETWORK_FILESYSTEM_TYPES = "health.network.filesystem.types";
    public static final String OSHI_PSEUDO_FILESYSTEM_TYPES = "health.pseudo.filesystem.types";

    /**
     * FileSystem types which are network-based and should be excluded from
     * local-only lists
     */
    protected static final List<String> NETWORK_FS_TYPES = Arrays.asList(
            Config.get(OSHI_NETWORK_FILESYSTEM_TYPES, Normal.EMPTY).split(Symbol.COMMA));

    protected static final List<String> PSEUDO_FS_TYPES = Arrays.asList(
            Config.get(OSHI_PSEUDO_FILESYSTEM_TYPES, Normal.EMPTY).split(Symbol.COMMA));

    @Override
    public List<OSFileStore> getFileStores() {
        return getFileStores(false);
    }

}
