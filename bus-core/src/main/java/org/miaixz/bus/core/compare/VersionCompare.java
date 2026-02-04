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
package org.miaixz.bus.core.compare;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.xyz.CompareKit;

/**
 * A comparator for version strings. It sorts versions from smallest to largest, with smaller versions appearing first.
 * Supports version formats like {@code 1.3.20.8}, {@code 6.82.20160101}, and {@code 8.5a}/{@code 8.5c}. Inspired by
 * {@code java.lang.module.ModuleDescriptor.Version}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VersionCompare extends NullCompare<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852262966591L;

    /**
     * Singleton instance of {@code VersionCompare}.
     */
    public static final VersionCompare INSTANCE = new VersionCompare();

    /**
     * Default constructor.
     */
    public VersionCompare() {
        this(false);
    }

    /**
     * Constructs a new {@code VersionCompare}.
     *
     * @param nullGreater whether {@code null} values should be considered the largest and placed at the end.
     */
    public VersionCompare(final boolean nullGreater) {
        super(nullGreater, (VersionCompare::compareVersion));
    }

    /**
     * Compares two version strings. {@code null} versions are considered the smallest.
     * 
     * <pre>
     * compare(null, "v1") &lt; 0
     * compare("v1", "v1")  = 0
     * compare(null, null)   = 0
     * compare("v1", null) &gt; 0
     * compare("1.0.0", "1.0.2") &lt; 0
     * compare("1.0.2", "1.0.2a") &lt; 0
     * compare("1.13.0", "1.12.1c") &gt; 0
     * compare("V0.0.20170102", "V0.0.20170101") &gt; 0
     * </pre>
     *
     * @param version1 the first version string.
     * @param version2 the second version string.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     */
    private static int compareVersion(final String version1, final String version2) {
        return CompareKit.compare(Version.of(version1), Version.of(version2));
    }

}
