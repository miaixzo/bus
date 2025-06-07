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
package org.miaixz.bus.core.text;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 字符串连接器（拼接器），通过给定的字符串和多个元素，拼接为一个字符串 相较于{@link java.util.StringJoiner}提供更加灵活的配置，包括：
 * <ul>
 * <li>支持任意Appendable接口实现</li>
 * <li>支持每个元素单独wrap</li>
 * <li>支持自定义null的处理逻辑</li>
 * <li>支持自定义默认结果</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringJoiner implements Appendable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852233611385L;

    private Appendable appendable;
    private CharSequence delimiter;
    private CharSequence prefix;
    private CharSequence suffix;
    /**
     * 前缀和后缀是否包装每个元素，true表示包装每个元素，false包装整个字符串
     */
    private boolean wrapElement;
    /**
     * null元素处理逻辑
     */
    private NullMode nullMode = NullMode.NULL_STRING;
    /**
     * 当结果为空时默认返回的拼接结果
     */
    private String emptyResult = Normal.EMPTY;
    /**
     * appendable中是否包含内容，用于判断增加内容时，是否首先加入分隔符
     */
    private boolean hasContent;

    /**
     * 构造
     *
     * @param delimiter 分隔符，{@code null}表示无连接符，直接拼接
     */
    public StringJoiner(final CharSequence delimiter) {
        this(null, delimiter);
    }

    /**
     * 构造
     *
     * @param appendable 字符串追加器，拼接的字符串都将加入到此，{@code null}使用默认{@link StringBuilder}
     * @param delimiter  分隔符，{@code null}表示无连接符，直接拼接
     */
    public StringJoiner(final Appendable appendable, final CharSequence delimiter) {
        this(appendable, delimiter, null, null);
    }

    /**
     * 构造
     *
     * @param delimiter 分隔符，{@code null}表示无连接符，直接拼接
     * @param prefix    前缀
     * @param suffix    后缀
     */
    public StringJoiner(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        this(null, delimiter, prefix, suffix);
    }

    /**
     * 构造
     *
     * @param appendable 字符串追加器，拼接的字符串都将加入到此，{@code null}使用默认{@link StringBuilder}
     * @param delimiter  分隔符，{@code null}表示无连接符，直接拼接
     * @param prefix     前缀
     * @param suffix     后缀
     */
    public StringJoiner(final Appendable appendable, final CharSequence delimiter, final CharSequence prefix,
            final CharSequence suffix) {
        if (null != appendable) {
            this.appendable = appendable;
            checkHasContent(appendable);
        }

        this.delimiter = delimiter;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * 根据已有StrJoiner配置新建一个新的StrJoiner
     *
     * @param joiner 已有StrJoiner
     * @return 新的StrJoiner，配置相同
     */
    public static StringJoiner of(final StringJoiner joiner) {
        final StringJoiner joinerNew = new StringJoiner(joiner.delimiter, joiner.prefix, joiner.suffix);
        joinerNew.wrapElement = joiner.wrapElement;
        joinerNew.nullMode = joiner.nullMode;
        joinerNew.emptyResult = joiner.emptyResult;

        return joinerNew;
    }

    /**
     * 使用指定分隔符创建StrJoiner
     *
     * @param delimiter 分隔符
     * @return StringJoiner
     */
    public static StringJoiner of(final CharSequence delimiter) {
        return new StringJoiner(delimiter);
    }

    /**
     * 使用指定分隔符创建StrJoiner
     *
     * @param delimiter 分隔符
     * @param prefix    前缀
     * @param suffix    后缀
     * @return StringJoiner
     */
    public static StringJoiner of(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return new StringJoiner(delimiter, prefix, suffix);
    }

    /**
     * 设置分隔符
     *
     * @param delimiter 分隔符，{@code null}表示无连接符，直接拼接
     * @return this
     */
    public StringJoiner setDelimiter(final CharSequence delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * 设置前缀
     *
     * @param prefix 前缀
     * @return this
     */
    public StringJoiner setPrefix(final CharSequence prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * 设置后缀
     *
     * @param suffix 后缀
     * @return this
     */
    public StringJoiner setSuffix(final CharSequence suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * 设置前缀和后缀是否包装每个元素
     *
     * @param wrapElement true表示包装每个元素，false包装整个字符串
     * @return this
     */
    public StringJoiner setWrapElement(final boolean wrapElement) {
        this.wrapElement = wrapElement;
        return this;
    }

    /**
     * 设置{@code null}元素处理逻辑
     *
     * @param nullMode 逻辑枚举，可选忽略、转换为""或转换为null字符串
     * @return this
     */
    public StringJoiner setNullMode(final NullMode nullMode) {
        this.nullMode = nullMode;
        return this;
    }

    /**
     * 设置当没有任何元素加入时，默认返回的字符串，默认""
     *
     * @param emptyResult 默认字符串
     * @return this
     */
    public StringJoiner setEmptyResult(final String emptyResult) {
        this.emptyResult = emptyResult;
        return this;
    }

    /**
     * 追加对象到拼接器中，支持：
     * <ul>
     * <li>null，按照 {@link #nullMode} 策略追加</li>
     * <li>array，逐个追加</li>
     * <li>{@link Iterator}，逐个追加</li>
     * <li>{@link Iterable}，逐个追加</li>
     * <li>{@link Map.Entry}，追加键，分隔符，再追加值</li>
     * </ul>
     *
     * @param object 对象，支持数组、集合等
     * @return this
     */
    public StringJoiner append(final Object object) {
        if (null == object) {
            append((CharSequence) null);
        } else if (ArrayKit.isArray(object)) {
            append(new ArrayIterator<>(object));
        } else if (object instanceof Iterator) {
            append((Iterator<?>) object);
        } else if (object instanceof Iterable) {
            append(((Iterable<?>) object).iterator());
        } else if (object instanceof Map.Entry) {
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
            append(entry.getKey()).append(entry.getValue());
        } else {
            append(Convert.toString(object));
        }
        return this;
    }

    /**
     * 追加数组中的元素到拼接器中
     *
     * @param <T>   元素类型
     * @param array 元素数组
     * @return this
     */
    public <T> StringJoiner append(final T[] array) {
        if (null == array) {
            return this;
        }
        return append(new ArrayIterator<>(array));
    }

    /**
     * 追加{@link Iterator}中的元素到拼接器中
     *
     * @param <T>      元素类型
     * @param iterator 元素列表
     * @return this
     */
    public <T> StringJoiner append(final Iterator<T> iterator) {
        if (null != iterator) {
            while (iterator.hasNext()) {
                append(iterator.next());
            }
        }
        return this;
    }

    /**
     * 追加数组中的元素到拼接器中
     *
     * @param <T>       元素类型
     * @param array     元素数组
     * @param toStrFunc 元素对象转换为字符串的函数
     * @return this
     */
    public <T> StringJoiner append(final T[] array, final Function<T, ? extends CharSequence> toStrFunc) {
        return append((Iterator<T>) new ArrayIterator<>(array), toStrFunc);
    }

    /**
     * 追加{@link Iterator}中的元素到拼接器中
     *
     * @param <E>       元素类型
     * @param iterable  元素列表
     * @param toStrFunc 元素对象转换为字符串的函数
     * @return this
     */
    public <E> StringJoiner append(final Iterable<E> iterable,
            final Function<? super E, ? extends CharSequence> toStrFunc) {
        return append(IteratorKit.getIter(iterable), toStrFunc);
    }

    /**
     * 追加{@link Iterator}中的元素到拼接器中
     *
     * @param <E>       元素类型
     * @param iterator  元素列表
     * @param toStrFunc 元素对象转换为字符串的函数
     * @return this
     */
    public <E> StringJoiner append(final Iterator<E> iterator,
            final Function<? super E, ? extends CharSequence> toStrFunc) {
        if (null != iterator) {
            while (iterator.hasNext()) {
                append(toStrFunc.apply(iterator.next()));
            }
        }
        return this;
    }

    @Override
    public StringJoiner append(final CharSequence csq) {
        return append(csq, 0, StringKit.length(csq));
    }

    @Override
    public StringJoiner append(CharSequence csq, final int startInclude, int endExclude) {
        if (null == csq) {
            switch (this.nullMode) {
            case IGNORE:
                return this;
            case TO_EMPTY:
                csq = Normal.EMPTY;
                break;
            case NULL_STRING:
                csq = Normal.NULL;
                endExclude = Normal.NULL.length();
                break;
            }
        }
        try {
            final Appendable appendable = prepare();
            if (wrapElement && StringKit.isNotEmpty(this.prefix)) {
                appendable.append(prefix);
            }
            appendable.append(csq, startInclude, endExclude);
            if (wrapElement && StringKit.isNotEmpty(this.suffix)) {
                appendable.append(suffix);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public StringJoiner append(final char c) {
        return append(String.valueOf(c));
    }

    /**
     * 合并一个StrJoiner 到当前的StrJoiner 合并规则为，在尾部直接追加，当存在{@link #prefix}时，如果{@link #wrapElement}为{@code false}，则去除之。
     *
     * @param stringJoiner 其他的StrJoiner
     * @return this
     */
    public StringJoiner merge(final StringJoiner stringJoiner) {
        if (null != stringJoiner && null != stringJoiner.appendable) {
            final String otherStr = stringJoiner.toString();
            if (stringJoiner.wrapElement) {
                this.append(otherStr);
            } else {
                this.append(otherStr, this.prefix.length(), otherStr.length());
            }
        }
        return this;
    }

    /**
     * 长度 长度计算方式为prefix + suffix + content 此方法结果与toString().length()一致。
     *
     * @return 长度，如果结果为{@code null}，返回-1
     */
    public int length() {
        return (this.appendable != null ? this.appendable.toString().length() + StringKit.length(suffix)
                : null == this.emptyResult ? -1 : emptyResult.length());
    }

    @Override
    public String toString() {
        if (null == this.appendable) {
            return emptyResult;
        }

        String result = this.appendable.toString();
        if (!wrapElement && StringKit.isNotEmpty(this.suffix)) {
            result += this.suffix;
        }
        return result;
    }

    /**
     * 准备连接器，如果连接器非空，追加元素，否则初始化前缀
     *
     * @return {@link Appendable}
     * @throws IOException IO异常
     */
    private Appendable prepare() throws IOException {
        if (hasContent) {
            if (null != delimiter) {
                this.appendable.append(delimiter);
            }
        } else {
            if (null == this.appendable) {
                this.appendable = new StringBuilder();
            }
            if (!wrapElement && StringKit.isNotEmpty(this.prefix)) {
                this.appendable.append(this.prefix);
            }
            this.hasContent = true;
        }
        return this.appendable;
    }

    /**
     * 检查用户传入的{@link Appendable} 是否已经存在内容，而且不能以分隔符结尾
     *
     * @param appendable {@link Appendable}
     */
    private void checkHasContent(final Appendable appendable) {
        if (appendable instanceof CharSequence) {
            final CharSequence charSequence = (CharSequence) appendable;
            if (charSequence.length() > 0 && StringKit.endWith(charSequence, delimiter)) {
                this.hasContent = true;
            }
        } else {
            final String initStr = appendable.toString();
            if (StringKit.isNotEmpty(initStr) && !StringKit.endWith(initStr, delimiter)) {
                this.hasContent = true;
            }
        }
    }

    /**
     * {@code null}处理的模式
     */
    public enum NullMode {
        /**
         * 忽略{@code null}，即null元素不加入拼接的字符串
         */
        IGNORE,
        /**
         * {@code null}转为""
         */
        TO_EMPTY,
        /**
         * {@code null}转为null字符串
         */
        NULL_STRING
    }

}
