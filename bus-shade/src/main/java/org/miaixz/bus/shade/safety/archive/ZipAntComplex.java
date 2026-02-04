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
package org.miaixz.bus.shade.safety.archive;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.complex.AntComplex;

/**
 * A {@link Complex} implementation that filters {@link ZipArchiveEntry} entries based on Ant-style path matching. This
 * class extends {@link AntComplex} and provides a way to apply Ant patterns to the names of ZIP archive entries.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipAntComplex extends AntComplex<ZipArchiveEntry> implements Complex<ZipArchiveEntry> {

    /**
     * Constructs a new {@code ZipAntComplex} with the specified Ant pattern.
     *
     * @param ant The Ant-style pattern to use for filtering.
     */
    public ZipAntComplex(String ant) {
        super(ant);
    }

    /**
     * Converts a {@link ZipArchiveEntry} into a string representation for pattern matching. This implementation returns
     * the name of the ZIP entry.
     *
     * @param entry The {@link ZipArchiveEntry} to convert.
     * @return The name of the ZIP archive entry.
     */
    @Override
    protected String toText(ZipArchiveEntry entry) {
        return entry.getName();
    }

}
