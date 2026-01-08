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
