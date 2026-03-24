/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.builtin.software.common;

import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.builtin.software.FileSystem;
import org.miaixz.bus.health.builtin.software.OSFileStore;

/**
 * Common methods for filesystem implementations
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractFileSystem implements FileSystem {

    /**
     * FileSystem types which are network-based and should be excluded from local-only lists
     */
    protected static final List<String> NETWORK_FS_TYPES = Arrays
            .asList(Config.get(Config._NETWORK_FILESYSTEM_TYPES, Normal.EMPTY).split(Symbol.COMMA));

    protected static final List<String> PSEUDO_FS_TYPES = Arrays
            .asList(Config.get(Config._PSEUDO_FILESYSTEM_TYPES, Normal.EMPTY).split(Symbol.COMMA));

    @Override
    public List<OSFileStore> getFileStores() {
        return getFileStores(false);
    }

}
