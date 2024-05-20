/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.net.url;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * URL中Path部分的封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlPath {

    private List<String> segments;
    private boolean withEngTag;

    /**
     * 构建UrlPath
     *
     * @return UrlPath
     */
    public static UrlPath of() {
        return new UrlPath();
    }

    /**
     * 构建UrlPath
     *
     * @param pathStr 初始化的路径字符串
     * @param charset decode用的编码，null表示不做decode
     * @return UrlPath
     */
    public static UrlPath of(final CharSequence pathStr, final Charset charset) {
        return of().parse(pathStr, charset);
    }

    /**
     * 修正路径，包括去掉前后的/，去掉空白符
     *
     * @param path 节点或路径path
     * @return 修正后的路径
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
     * 是否path的末尾加 /
     *
     * @param withEngTag 是否path的末尾加 /
     * @return this
     */
    public UrlPath setWithEndTag(final boolean withEngTag) {
        this.withEngTag = withEngTag;
        return this;
    }

    /**
     * 获取path的节点列表，如果列表为空，返回{@link ListKit#empty()}
     *
     * @return 节点列表
     */
    public List<String> getSegments() {
        return ObjectKit.defaultIfNull(this.segments, ListKit.empty());
    }

    /**
     * 获得指定节点
     *
     * @param index 节点位置
     * @return 节点，无节点或者越界返回null
     */
    public String getSegment(final int index) {
        if (null == this.segments || index >= this.segments.size()) {
            return null;
        }
        return this.segments.get(index);
    }

    /**
     * 添加到path最后面
     *
     * @param segment Path节点
     * @return this
     */
    public UrlPath add(final CharSequence segment) {
        addInternal(fixPath(segment), false);
        return this;
    }

    /**
     * 添加到path最前面
     *
     * @param segment Path节点
     * @return this
     */
    public UrlPath addBefore(final CharSequence segment) {
        addInternal(fixPath(segment), true);
        return this;
    }

    /**
     * 解析path
     *
     * @param path    路径，类似于aaa/bb/ccc或/aaa/bbb/ccc
     * @param charset decode编码，null表示不解码
     * @return this
     */
    public UrlPath parse(CharSequence path, final Charset charset) {
        if (StringKit.isNotEmpty(path)) {
            // 原URL中以/结尾，则这个规则需保留
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

    /**
     * 构建path，前面带'/'
     * <pre>
     *     path = path-abempty / path-absolute / path-noscheme / path-rootless / path-empty
     * </pre>
     *
     * @param charset encode编码，null表示不做encode
     * @return 如果没有任何内容，则返回空字符串""
     */
    public String build(final Charset charset) {
        return build(charset, true);
    }

    /**
     * 构建path，前面带'/'
     * <pre>
     *     path = path-abempty / path-absolute / path-noscheme / path-rootless / path-empty
     * </pre>
     *
     * @param charset       encode编码，null表示不做encode
     * @param encodePercent 是否编码`%`
     * @return 如果没有任何内容，则返回空字符串""
     */
    public String build(final Charset charset, final boolean encodePercent) {
        if (CollKit.isEmpty(this.segments)) {
            // 没有节点的path取决于是否末尾追加/，如果不追加返回空串，否则返回/
            return withEngTag ? Symbol.SLASH : Normal.EMPTY;
        }

        final char[] safeChars = encodePercent ? null : new char[]{Symbol.C_PERCENT};
        final StringBuilder builder = new StringBuilder();
        for (final String segment : segments) {
            if (builder.length() == 0) {
                // 根据https://www.ietf.org/rfc/rfc3986.html#section-3.3定义
                // path的第一部分不允许有":"，其余部分允许
                // 在此处的Path部分特指host之后的部分，即不包含第一部分
                builder.append(Symbol.C_SLASH).append(RFC3986.SEGMENT_NZ_NC.encode(segment, charset, safeChars));
            } else {
                builder.append(Symbol.C_SLASH).append(RFC3986.SEGMENT.encode(segment, charset, safeChars));
            }
        }

        if (withEngTag) {
            if (StringKit.isEmpty(builder)) {
                // 空白追加是保证以/开头
                builder.append(Symbol.C_SLASH);
            } else if (!StringKit.endWith(builder, Symbol.C_SLASH)) {
                // 尾部没有/则追加，否则不追加
                builder.append(Symbol.C_SLASH);
            }
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return build(null);
    }

    /**
     * 增加节点
     *
     * @param segment 节点
     * @param before  是否在前面添加
     */
    private void addInternal(final CharSequence segment, final boolean before) {
        if (this.segments == null) {
            this.segments = new LinkedList<>();
        }

        final String seg = StringKit.toString(segment);
        if (before) {
            this.segments.add(0, seg);
        } else {
            this.segments.add(seg);
        }
    }

}
