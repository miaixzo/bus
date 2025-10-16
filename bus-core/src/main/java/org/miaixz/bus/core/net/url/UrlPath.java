/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.net.url;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Represents the path component of a URL.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlPath {

    private List<CharSequence> segments;
    private boolean withEngTag;

    /**
     * Creates a new {@link UrlPath} instance.
     *
     * @return A new {@link UrlPath} instance.
     */
    public static UrlPath of() {
        return new UrlPath();
    }

    /**
     * Creates a new {@link UrlPath} instance from a path string.
     *
     * @param pathStr The initial path string.
     * @param charset The charset for decoding the path string; if {@code null}, no decoding is performed.
     * @return A new {@link UrlPath} instance.
     */
    public static UrlPath of(final CharSequence pathStr, final Charset charset) {
        return of().parse(pathStr, charset);
    }

    /**
     * Cleans up the path segment by removing leading/trailing slashes and whitespace.
     *
     * @param path The path segment or full path string.
     * @return The cleaned path segment.
     */
    private static String fixPath(final CharSequence path) {
        Assert.notNull(path, "Path segment must be not null!");
        if ("/".contentEquals(path)) {
            return Normal.EMPTY;
        }

        String segmentStr = StringKit.trim(path);
        segmentStr = StringKit.removePrefix(segmentStr, Symbol.SLASH);
        segmentStr = StringKit.removeSuffix(segmentStr, Symbol.SLASH);
        segmentStr = StringKit.trim(segmentStr);
        return segmentStr;
    }

    /**
     * Sets whether to append a trailing slash ({@code /}) to the path.
     *
     * @param withEngTag {@code true} to append a trailing slash, {@code false} otherwise.
     * @return This {@link UrlPath} instance for method chaining.
     */
    public UrlPath setWithEndTag(final boolean withEngTag) {
        this.withEngTag = withEngTag;
        return this;
    }

    /**
     * Returns the list of path segments. If the list is empty, it returns an empty list.
     *
     * @return The list of path segments.
     */
    public List<CharSequence> getSegments() {
        return ObjectKit.defaultIfNull(this.segments, ListKit.empty());
    }

    /**
     * Retrieves the segment at the specified index.
     *
     * @param index The index of the segment to retrieve.
     * @return The segment at the specified index, or {@code null} if the index is out of bounds.
     */
    public CharSequence getSegment(final int index) {
        if (null == this.segments || index >= this.segments.size()) {
            return null;
        }
        return this.segments.get(index);
    }

    /**
     * Adds a segment to the end of the path.
     *
     * @param segment The path segment to add.
     * @return This {@link UrlPath} instance for method chaining.
     */
    public UrlPath add(final CharSequence segment) {
        addInternal(fixPath(segment), false);
        return this;
    }

    /**
     * Adds a segment to the beginning of the path.
     *
     * @param segment The path segment to add.
     * @return This {@link UrlPath} instance for method chaining.
     */
    public UrlPath addBefore(final CharSequence segment) {
        addInternal(fixPath(segment), true);
        return this;
    }

    /**
     * Parses a path string and populates this {@link UrlPath} instance.
     *
     * @param path    The path string, such as {@code aaa/bb/ccc} or {@code /aaa/bbb/ccc}.
     * @param charset The charset for decoding the path; if {@code null}, no decoding is performed.
     * @return This {@link UrlPath} instance for method chaining.
     */
    public UrlPath parse(CharSequence path, final Charset charset) {
        if (StringKit.isNotEmpty(path)) {
            // Preserve the trailing slash rule if the original URL ends with '/'
            if (StringKit.endWith(path, Symbol.C_SLASH)) {
                this.withEngTag = true;
            }

            path = fixPath(path);
            if (StringKit.isNotEmpty(path)) {
                final List<String> split = CharsBacker.split(path, Symbol.SLASH);
                for (final String seg : split) {
                    addInternal(UrlDecoder.decodeForPath(seg, charset), false);
                }
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return build(null);
    }

    /**
     * Builds the path string with a leading slash ({@code /}).
     * <p>
     * {@code path = path-abempty / path-absolute / path-noscheme / path-rootless / path-empty}
     *
     * @param charset The charset for encoding the path; if {@code null}, no encoding is performed.
     * @return The constructed path string, or an empty string if there are no segments.
     */
    public String build(final Charset charset) {
        if (CollKit.isEmpty(this.segments)) {
            return withEngTag ? Symbol.SLASH : Normal.EMPTY;
        }

        final StringBuilder builder = new StringBuilder();
        for (final CharSequence segment : segments) {
            // According to RFC 3986, Section 3.3, a colon is allowed in the path.
            builder.append(Symbol.C_SLASH).append(RFC3986.SEGMENT.encode(segment, charset));
        }

        if (withEngTag) {
            if (StringKit.isEmpty(builder)) {
                builder.append(Symbol.C_SLASH);
            } else if (!StringKit.endWith(builder, Symbol.C_SLASH)) {
                builder.append(Symbol.C_SLASH);
            }
        }

        return builder.toString();
    }

    /**
     * Adds a segment to the path.
     *
     * @param segment The segment to add.
     * @param before  {@code true} to add the segment at the beginning, {@code false} to add it at the end.
     */
    private void addInternal(final CharSequence segment, final boolean before) {
        if (this.segments == null) {
            this.segments = new LinkedList<>();
        }

        if (before) {
            this.segments.add(0, segment);
        } else {
            this.segments.add(segment);
        }
    }

}
