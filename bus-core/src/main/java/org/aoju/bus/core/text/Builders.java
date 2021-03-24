/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.text;

import org.aoju.bus.core.builder.Builder;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.ArrayKit;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.Objects;

/**
 * 提供比StringBuffer更灵活和更强大的API.
 *
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public class Builders implements CharSequence, Appendable, Serializable, Builder<String> {

    private static final long serialVersionUID = 1L;

    /**
     * 默认容量
     */
    private static final int CAPACITY = 16;
    /**
     * 存放的字符数组
     */
    protected char[] buffer;
    /**
     * 当前指针位置,或者叫做已经加入的字符数,此位置总在最后一个字符之后
     */
    protected int size;
    /**
     * 新的一行
     */
    private String newLine;
    /**
     * null字符串
     */
    private String nullText;

    /**
     * 构造
     */
    public Builders() {
        this(CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     */
    public Builders(int initialCapacity) {
        super();
        if (initialCapacity <= 0) {
            initialCapacity = CAPACITY;
        }
        this.buffer = new char[initialCapacity];
    }

    /**
     * 构造
     *
     * @param str 初始字符串
     */
    public Builders(final String str) {
        super();
        if (null == str) {
            this.buffer = new char[CAPACITY];
        } else {
            this.buffer = new char[str.length() + CAPACITY];
            append(str);
        }
    }

    /**
     * 创建字符串构建器
     *
     * @param strs 初始字符串
     */
    public Builders(CharSequence... strs) {
        this(ArrayKit.isEmpty(strs) ? CAPACITY : (totalLength(strs) + CAPACITY));
        for (int i = 0; i < strs.length; i++) {
            append(strs[i]);
        }
    }

    /**
     * 创建字符串构建器
     *
     * @return {@link Builders}
     */
    public static Builders create() {
        return new Builders();
    }

    /**
     * 创建字符串构建器
     *
     * @param initialCapacity 初始容量
     * @return {@link Builders}
     */
    public static Builders create(int initialCapacity) {
        return new Builders(initialCapacity);
    }

    /**
     * 创建字符串构建器
     *
     * @param strs 初始字符串
     * @return {@link Builders}
     */
    public static Builders create(CharSequence... strs) {
        return new Builders(strs);
    }

    /**
     * 给定字符串数组的总长度
     * null字符长度定义为0
     *
     * @param strs 字符串数组
     * @return 总长度
     */
    private static int totalLength(CharSequence... strs) {
        int totalLength = 0;
        for (CharSequence str : strs) {
            totalLength += (null == str ? 4 : str.length());
        }
        return totalLength;
    }

    @Override
    public int length() {
        return this.size;
    }

    @Override
    public char charAt(int index) {
        if (index < 0) {
            index = this.size + index;
        }
        if ((index < 0) || (index > this.size)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return this.buffer[index];
    }

    public String getNewLineText() {
        return newLine;
    }

    public Builders setNewLineText(final String newLine) {
        this.newLine = newLine;
        return this;
    }

    public String getNullText() {
        return nullText;
    }

    public Builders setNullText(String nullText) {
        if (null != nullText && nullText.isEmpty()) {
            nullText = null;
        }
        this.nullText = nullText;
        return this;
    }

    /**
     * 通过删除最后一个字符或添加Unicode 0的填充来更新生成器的长度
     *
     * @param length 要设置的长度必须为0或正
     * @return this
     * @throws IndexOutOfBoundsException 如果长度是负的
     */
    public Builders setLength(final int length) {
        if (length < 0) {
            throw new StringIndexOutOfBoundsException(length);
        }
        if (length < this.size) {
            this.size = length;
        } else if (length > this.size) {
            ensureCapacity(length);
            final int oldEnd = this.size;
            final int newEnd = length;
            this.size = length;
            for (int i = oldEnd; i < newEnd; i++) {
                this.buffer[i] = '\0';
            }
        }
        return this;
    }

    public int capacity() {
        return buffer.length;
    }

    /**
     * 检查容量并确保它至少是指定的大小
     *
     * @param capacity 确保大小
     * @return this
     */
    public Builders ensureCapacity(final int capacity) {
        if (capacity > buffer.length) {
            final char[] old = buffer;
            buffer = new char[capacity * 2];
            System.arraycopy(old, 0, buffer, 0, size);
        }
        return this;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public Builders clear() {
        return reset();
    }

    /**
     * 删除全部字符，位置归零
     *
     * @return this
     */
    public Builders reset() {
        this.size = 0;
        return this;
    }

    /**
     * 在指定的索引处设置字符
     *
     * @param index 要设置的索引
     * @param ch    新字符
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     * @see #charAt(int)
     * @see #deleteCharAt(int)
     */
    public Builders setCharAt(final int index, final char ch) {
        if (index < 0 || index >= length()) {
            throw new StringIndexOutOfBoundsException(index);
        }
        buffer[index] = ch;
        return this;
    }

    /**
     * 删除指定索引处的字符
     *
     * @param index 要删除的索引
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     * @see #charAt(int)
     * @see #setCharAt(int, char)
     */
    public Builders deleteCharAt(final int index) {
        if (index < 0 || index >= size) {
            throw new StringIndexOutOfBoundsException(index);
        }
        deleteImpl(index, index + 1, 1);
        return this;
    }

    /**
     * 将字符数组复制到指定的数组中.
     *
     * @param destination 目标数组，null将导致创建一个数组
     * @return 输入数组，除非它是null或太小
     */
    public char[] getChars(char[] destination) {
        final int len = length();
        if (null == destination || destination.length < len) {
            destination = new char[len];
        }
        System.arraycopy(buffer, 0, destination, 0, len);
        return destination;
    }

    /**
     * 将字符数组复制到指定的数组中
     *
     * @param startIndex       要复制的第一个索引(包括)必须有效
     * @param endIndex         最后一个索引(exclusive)必须有效
     * @param destination      目标数组不能为空或太小
     * @param destinationIndex 要在目的地开始复制的索引
     * @throws NullPointerException      如果数组为空
     * @throws IndexOutOfBoundsException 如果任何索引无效
     */
    public void getChars(final int startIndex, final int endIndex, final char[] destination, final int destinationIndex) {
        if (startIndex < 0) {
            throw new StringIndexOutOfBoundsException(startIndex);
        }
        if (endIndex < 0 || endIndex > length()) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if (startIndex > endIndex) {
            throw new StringIndexOutOfBoundsException("end < start");
        }
        System.arraycopy(buffer, startIndex, destination, destinationIndex, endIndex - startIndex);
    }

    /**
     * 如果可能，从提供的{@link Readable}直接将字符读入底层字符缓冲区，而不进行额外的复制
     *
     * @param readable 读取对象
     * @return 读取的字符数
     * @throws IOException 如果发生I/O错误
     */
    public int readFrom(final Readable readable) throws IOException {
        final int oldSize = size;
        if (readable instanceof Reader) {
            final Reader r = (Reader) readable;
            ensureCapacity(size + 1);
            int read;
            while ((read = r.read(buffer, size, buffer.length - size)) != -1) {
                size += read;
                ensureCapacity(size + 1);
            }
        } else if (readable instanceof CharBuffer) {
            final CharBuffer cb = (CharBuffer) readable;
            final int remaining = cb.remaining();
            ensureCapacity(size + remaining);
            cb.get(buffer, size, remaining);
            size += remaining;
        } else {
            while (true) {
                ensureCapacity(size + 1);
                final CharBuffer buf = CharBuffer.wrap(buffer, size, buffer.length - size);
                final int read = readable.read(buf);
                if (read == -1) {
                    break;
                }
                size += read;
            }
        }
        return size - oldSize;
    }

    /**
     * 将新行字符串附加到此字符串生成器
     *
     * @return this
     */
    public Builders appendNewLine() {
        if (null == newLine) {
            append(System.getProperty("line.separator"));
            return this;
        }
        return append(newLine);
    }

    /**
     * 将表示null的文本附加到此字符串生成器.
     *
     * @return this
     */
    public Builders appendNull() {
        if (null == nullText) {
            return this;
        }
        return append(nullText);
    }

    /**
     * 将对象附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param obj 要追加的对象
     * @return this
     */
    public Builders append(final Object obj) {
        if (null == obj) {
            return appendNull();
        }
        if (obj instanceof CharSequence) {
            return append((CharSequence) obj);
        }
        return append(obj.toString());
    }

    /**
     * 将对象附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param seq 要附加的字符序列
     * @return this
     */
    @Override
    public Builders append(final CharSequence seq) {
        if (null == seq) {
            return appendNull();
        }
        if (seq instanceof Builders) {
            return append((Builders) seq);
        }
        if (seq instanceof StringBuilder) {
            return append((StringBuilder) seq);
        }
        if (seq instanceof StringBuffer) {
            return append((StringBuffer) seq);
        }
        if (seq instanceof CharBuffer) {
            return append((CharBuffer) seq);
        }
        return append(seq.toString());
    }

    /**
     * 将对象附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param seq        要附加的字符序列
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    @Override
    public Builders append(final CharSequence seq, final int startIndex, final int length) {
        if (null == seq) {
            return appendNull();
        }
        return append(seq.toString(), startIndex, length);
    }

    /**
     * 将对象附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param str 要追加的字符串
     * @return this
     */
    public Builders append(final String str) {
        if (null == str) {
            return appendNull();
        }
        final int strLen = str.length();
        if (strLen > 0) {
            final int len = length();
            ensureCapacity(len + strLen);
            str.getChars(0, strLen, buffer, len);
            size += strLen;
        }
        return this;
    }

    /**
     * 将对象附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param str        要追加的字符串
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders append(final String str, final int startIndex, final int length) {
        if (null == str) {
            return appendNull();
        }
        if (startIndex < 0 || startIndex > str.length()) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if (length < 0 || (startIndex + length) > str.length()) {
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if (length > 0) {
            final int len = length();
            ensureCapacity(len + length);
            str.getChars(startIndex, startIndex + length, buffer, len);
            size += length;
        }
        return this;
    }

    /**
     * 调用 {@link String#format(String, Object...)}并附加结果
     *
     * @param format 格式字符串
     * @param objs   要在格式字符串中使用的对象
     * @return this
     * @see String#format(String, Object...)
     */
    public Builders append(final String format, final Object... objs) {
        return append(String.format(format, objs));
    }

    /**
     * 将char缓冲区的内容附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param buf 要附加的字符缓冲区
     * @return this
     */
    public Builders append(final CharBuffer buf) {
        if (null == buf) {
            return appendNull();
        }
        if (buf.hasArray()) {
            final int length = buf.remaining();
            final int len = length();
            ensureCapacity(len + length);
            System.arraycopy(buf.array(), buf.arrayOffset() + buf.position(), buffer, len, length);
            size += length;
        } else {
            append(buf.toString());
        }
        return this;
    }

    /**
     * 将char缓冲区的内容附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param buf        要附加的字符缓冲区
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders append(final CharBuffer buf, final int startIndex, final int length) {
        if (null == buf) {
            return appendNull();
        }
        if (buf.hasArray()) {
            final int totalLength = buf.remaining();
            if (startIndex < 0 || startIndex > totalLength) {
                throw new StringIndexOutOfBoundsException("startIndex must be valid");
            }
            if (length < 0 || (startIndex + length) > totalLength) {
                throw new StringIndexOutOfBoundsException("length must be valid");
            }
            final int len = length();
            ensureCapacity(len + length);
            System.arraycopy(buf.array(), buf.arrayOffset() + buf.position() + startIndex, buffer, len, length);
            size += length;
        } else {
            append(buf.toString(), startIndex, length);
        }
        return this;
    }

    /**
     * 将对象附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param str 要追加的字符串
     * @return this
     */
    public Builders append(final StringBuffer str) {
        if (null == str) {
            return appendNull();
        }
        final int strLen = str.length();
        if (strLen > 0) {
            final int len = length();
            ensureCapacity(len + strLen);
            str.getChars(0, strLen, buffer, len);
            size += strLen;
        }
        return this;
    }

    /**
     * 将对象附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param str        要追加的字符串
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders append(final StringBuffer str, final int startIndex, final int length) {
        if (null == str) {
            return appendNull();
        }
        if (startIndex < 0 || startIndex > str.length()) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if (length < 0 || (startIndex + length) > str.length()) {
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if (length > 0) {
            final int len = length();
            ensureCapacity(len + length);
            str.getChars(startIndex, startIndex + length, buffer, len);
            size += length;
        }
        return this;
    }

    /**
     * 将对象附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param str 要追加的字符串
     * @return this
     */
    public Builders append(final StringBuilder str) {
        if (null == str) {
            return appendNull();
        }
        final int strLen = str.length();
        if (strLen > 0) {
            final int len = length();
            ensureCapacity(len + strLen);
            str.getChars(0, strLen, buffer, len);
            size += strLen;
        }
        return this;
    }

    /**
     * 将StringBuilder的一部分附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}
     *
     * @param str        要追加的字符串
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders append(final StringBuilder str, final int startIndex, final int length) {
        if (null == str) {
            return appendNull();
        }
        if (startIndex < 0 || startIndex > str.length()) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if (length < 0 || (startIndex + length) > str.length()) {
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if (length > 0) {
            final int len = length();
            ensureCapacity(len + length);
            str.getChars(startIndex, startIndex + length, buffer, len);
            size += length;
        }
        return this;
    }

    /**
     * 将String的一部分附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}
     *
     * @param str 要追加的字符串
     * @return this
     */
    public Builders append(final Builders str) {
        if (null == str) {
            return appendNull();
        }
        final int strLen = str.length();
        if (strLen > 0) {
            final int len = length();
            ensureCapacity(len + strLen);
            System.arraycopy(str.buffer, 0, buffer, len, strLen);
            size += strLen;
        }
        return this;
    }

    /**
     * 将字符串生成器的一部分附加到此字符串生成器
     * 附加null将调用{@link #appendNull()}.
     *
     * @param str        要追加的字符串
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders append(final Builders str, final int startIndex, final int length) {
        if (null == str) {
            return appendNull();
        }
        if (startIndex < 0 || startIndex > str.length()) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if (length < 0 || (startIndex + length) > str.length()) {
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if (length > 0) {
            final int len = length();
            ensureCapacity(len + length);
            str.getChars(startIndex, startIndex + length, buffer, len);
            size += length;
        }
        return this;
    }

    /**
     * 向字符串生成器追加一个char数组
     * 附加null将调用{@link #appendNull()}.
     *
     * @param chars 要附加的字符数组
     * @return this
     */
    public Builders append(final char[] chars) {
        if (null == chars) {
            return appendNull();
        }
        final int strLen = chars.length;
        if (strLen > 0) {
            final int len = length();
            ensureCapacity(len + strLen);
            System.arraycopy(chars, 0, buffer, len, strLen);
            size += strLen;
        }
        return this;
    }

    /**
     * 向字符串生成器追加一个char数组
     * 附加null将调用{@link #appendNull()}
     *
     * @param chars      要附加的字符数组
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders append(final char[] chars, final int startIndex, final int length) {
        if (null == chars) {
            return appendNull();
        }
        if (startIndex < 0 || startIndex > chars.length) {
            throw new StringIndexOutOfBoundsException("Invalid startIndex: " + length);
        }
        if (length < 0 || (startIndex + length) > chars.length) {
            throw new StringIndexOutOfBoundsException("Invalid length: " + length);
        }
        if (length > 0) {
            final int len = length();
            ensureCapacity(len + length);
            System.arraycopy(chars, startIndex, buffer, len, length);
            size += length;
        }
        return this;
    }

    /**
     * 将布尔值附加到字符串生成器.
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders append(final boolean value) {
        if (value) {
            ensureCapacity(size + 4);
            buffer[size++] = 't';
            buffer[size++] = 'r';
            buffer[size++] = 'u';
            buffer[size++] = 'e';
        } else {
            ensureCapacity(size + 5);
            buffer[size++] = 'f';
            buffer[size++] = 'a';
            buffer[size++] = 'l';
            buffer[size++] = 's';
            buffer[size++] = 'e';
        }
        return this;
    }

    /**
     * 将char值附加到字符串生成器.
     *
     * @param ch 要附加的值
     * @return this
     */
    @Override
    public Builders append(final char ch) {
        final int len = length();
        ensureCapacity(len + 1);
        buffer[size++] = ch;
        return this;
    }

    /**
     * 使用<code>String.valueOf</code>向字符串生成器追加一个int值
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders append(final int value) {
        return append(String.valueOf(value));
    }

    /**
     * 使用<code>String.valueOf</code>向字符串生成器追加一个long值
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders append(final long value) {
        return append(String.valueOf(value));
    }

    /**
     * 使用<code>String.valueOf</code>向字符串生成器追加一个float值
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders append(final float value) {
        return append(String.valueOf(value));
    }

    /**
     * 使用<code>String.valueOf</code>向字符串生成器追加一个double值
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders append(final double value) {
        return append(String.valueOf(value));
    }

    /**
     * 将对象后接新行追加到此字符串生成器
     * 附加null将调用{@link #appendNull()}
     *
     * @param obj 要附加的值
     * @return this
     */
    public Builders appendln(final Object obj) {
        return append(obj).appendNewLine();
    }

    /**
     * 将对象后接新行追加到此字符串生成器
     * 附加null将调用{@link #appendNull()}
     *
     * @param str 要附加的值
     * @return this
     */
    public Builders appendln(final String str) {
        return append(str).appendNewLine();
    }

    /**
     * 将对象后接新行追加到此字符串生成器
     * 附加null将调用{@link #appendNull()}
     *
     * @param str        要附加的值
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders appendln(final String str, final int startIndex, final int length) {
        return append(str, startIndex, length).appendNewLine();
    }

    /**
     * 调用{@link String#format(String, Object...)}并附加结果
     *
     * @param format 格式字符串
     * @param objs   要在格式字符串中使用的对象
     * @return this
     * @see String#format(String, Object...)
     */
    public Builders appendln(final String format, final Object... objs) {
        return append(format, objs).appendNewLine();
    }

    /**
     * 向此字符串生成器追加一个字符串缓冲区，后跟新行
     * 附加null将调用{@link #appendNull()}
     *
     * @param str 要追加的字符串缓冲区
     * @return this
     */
    public Builders appendln(final StringBuffer str) {
        return append(str).appendNewLine();
    }

    /**
     * 向此字符串生成器追加一个字符串缓冲区，后跟新行
     * * 附加null将调用{@link #appendNull()}
     *
     * @param str t他附加字符串生成器
     * @return this
     */
    public Builders appendln(final StringBuilder str) {
        return append(str).appendNewLine();
    }

    /**
     * 向此字符串生成器追加一个字符串缓冲区，后跟新行
     * 附加null将调用{@link #appendNull()}
     *
     * @param str        要追加的字符串缓冲区
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders appendln(final StringBuilder str, final int startIndex, final int length) {
        return append(str, startIndex, length).appendNewLine();
    }

    /**
     * 向此字符串生成器追加一个字符串缓冲区，后跟新行
     * 附加null将调用{@link #appendNull()}
     *
     * @param str        要追加的字符串缓冲区
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders appendln(final StringBuffer str, final int startIndex, final int length) {
        return append(str, startIndex, length).appendNewLine();
    }

    /**
     * 向此字符串生成器追加一个字符串缓冲区，后跟新行
     * 附加null将调用{@link #appendNull()}
     *
     * @param str 要追加的字符串缓冲区
     * @return this
     */
    public Builders appendln(final Builders str) {
        return append(str).appendNewLine();
    }

    /**
     * 向此字符串生成器追加一个字符串缓冲区，后跟新行
     * 附加null将调用{@link #appendNull()}
     *
     * @param str        要追加的字符串缓冲区
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders appendln(final Builders str, final int startIndex, final int length) {
        return append(str, startIndex, length).appendNewLine();
    }

    /**
     * 向此字符串生成器追加一个字符串缓冲区，后跟新行
     * 附加null将调用{@link #appendNull()}
     *
     * @param chars 要附加的字符数组
     * @return this
     */
    public Builders appendln(final char[] chars) {
        return append(chars).appendNewLine();
    }

    /**
     * 向字符串生成器追加一个字符数组，后跟一个新行
     * 附加null将调用{@link #appendNull()}
     *
     * @param chars      要追加的字符串缓冲区
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param length     要追加的长度必须有效
     * @return this
     */
    public Builders appendln(final char[] chars, final int startIndex, final int length) {
        return append(chars, startIndex, length).appendNewLine();
    }

    /**
     * 将布尔值后跟新行追加到字符串生成器.
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders appendln(final boolean value) {
        return append(value).appendNewLine();
    }

    /**
     * 向字符串生成器追加一个字符值，后跟一个新行.
     *
     * @param ch 要附加的值
     * @return this
     */
    public Builders appendln(final char ch) {
        return append(ch).appendNewLine();
    }

    /**
     * 使用<code>String.valueOf</code>向字符串生成器追加一个int值，后面跟一个新行
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders appendln(final int value) {
        return append(value).appendNewLine();
    }

    /**
     * 使用<code>String.valueOf</code>向字符串生成器追加一个long值，后面跟一个新行
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders appendln(final long value) {
        return append(value).appendNewLine();
    }

    /**
     * 使用<code>String.valueOf</code>向字符串生成器追加一个float值，后面跟一个新行
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders appendln(final float value) {
        return append(value).appendNewLine();
    }

    /**
     * 使用<code>String.valueOf</code>向字符串生成器追加一个double值，后面跟一个新行
     *
     * @param value 要附加的值
     * @return this
     */
    public Builders appendln(final double value) {
        return append(value).appendNewLine();
    }

    /**
     * 不使用任何分隔符将数组中的每个项添加到生成器
     * 附加一个空数组将没有效果
     * 每个对象都使用{@link #append(Object)}进行追加
     *
     * @param <T>   元素类型
     * @param array 要追加的数组
     * @return this
     */
    public <T> Builders appendAll(final T... array) {
        if (null != array && array.length > 0) {
            for (final Object element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * 在不使用任何分隔符的情况下将迭代中的每个项附加到生成器
     * 附加一个null iterable将没有效果
     * 每个对象都使用{@link #append(Object)}进行追加
     *
     * @param iterable 可追加的迭代
     * @return this
     */
    public Builders appendAll(final Iterable<?> iterable) {
        if (null != iterable) {
            for (final Object o : iterable) {
                append(o);
            }
        }
        return this;
    }

    /**
     * 在不使用任何分隔符的情况下将迭代器中的每个项附加到生成器
     * 附加一个空迭代器将没有效果
     * 每个对象都使用{@link #append(Object)}进行追加.
     *
     * @param it 要追加的迭代器
     * @return this
     */
    public Builders appendAll(final Iterator<?> it) {
        if (null != it) {
            while (it.hasNext()) {
                append(it.next());
            }
        }
        return this;
    }

    /**
     * 在每个值之间追加放置分隔符的数组，但不在第一个值之前或最后一个值之后追加
     * 附加一个空数组将没有效果
     * 每个对象都使用{@link #append(Object)}进行追加
     *
     * @param array     要追加的数组
     * @param separator 要使用的分隔符，null表示没有分隔符
     * @return this
     */
    public Builders appendWithSeparators(final Object[] array, final String separator) {
        if (null != array && array.length > 0) {
            final String sep = Objects.toString(separator, Normal.EMPTY);
            append(array[0]);
            for (int i = 1; i < array.length; i++) {
                append(sep);
                append(array[i]);
            }
        }
        return this;
    }

    /**
     * 在每个值之间追加一个可迭代的放置分隔符，但不是在第一个值之前或最后一个值之后
     * 附加一个null iterable将没有效果
     * 每个对象都使用{@link #append(Object)}进行追加.
     *
     * @param iterable  可追加的迭代
     * @param separator 要使用的分隔符，null表示没有分隔符
     * @return this
     */
    public Builders appendWithSeparators(final Iterable<?> iterable, final String separator) {
        if (null != iterable) {
            final String sep = Objects.toString(separator, Normal.EMPTY);
            final Iterator<?> it = iterable.iterator();
            while (it.hasNext()) {
                append(it.next());
                if (it.hasNext()) {
                    append(sep);
                }
            }
        }
        return this;
    }

    /**
     * 在每个值之间添加分隔符，但不在第一个值之前或最后一个值之后添加迭代器
     * 附加一个空迭代器将没有效果
     * 每个对象都使用{@link #append(Object)}进行追加.
     *
     * @param it        要追加的迭代器
     * @param separator 要使用的分隔符，null表示没有分隔符
     * @return this
     */
    public Builders appendWithSeparators(final Iterator<?> it, final String separator) {
        if (null != it) {
            final String sep = Objects.toString(separator, Normal.EMPTY);
            while (it.hasNext()) {
                append(it.next());
                if (it.hasNext()) {
                    append(sep);
                }
            }
        }
        return this;
    }

    /**
     * 将一个对象附加到左侧的生成器内边距上，使其具有固定的宽度。
     * 使用对象的<code>toString</code>
     * 如果对象的长度大于长度，则左手边就会丢失
     * 如果对象为空，则使用空文本值
     *
     * @param obj     要追加的对象null使用空文本
     * @param width   固定的字段宽度，零或负没有影响
     * @param padChar 要使用的填充字符
     * @return this
     */
    public Builders appendFixedWidthPadLeft(final Object obj, final int width, final char padChar) {
        if (width > 0) {
            ensureCapacity(size + width);
            String str = null == obj ? getNullText() : obj.toString();
            if (null == str) {
                str = Normal.EMPTY;
            }
            final int strLen = str.length();
            if (strLen >= width) {
                str.getChars(strLen - width, strLen, buffer, size);
            } else {
                final int padLen = width - strLen;
                for (int i = 0; i < padLen; i++) {
                    buffer[size + i] = padChar;
                }
                str.getChars(0, strLen, buffer, size + padLen);
            }
            size += width;
        }
        return this;
    }

    /**
     * 将一个对象附加到左侧的生成器内边距上，使其具有固定的宽度
     * 使用<code>String.valueOf</code>字符串，使用<code>int</code>
     * 如果格式化的值大于长度，则左侧边将丢失
     *
     * @param value   要附加的值
     * @param width   固定的字段宽度，零或负没有影响
     * @param padChar 要使用的填充字符
     * @return this
     */
    public Builders appendFixedWidthPadLeft(final int value, final int width, final char padChar) {
        return appendFixedWidthPadLeft(String.valueOf(value), width, padChar);
    }

    /**
     * 将对象附加到右侧的生成器内边距，使其具有固定的长度。
     * 使用对象的<code>toString</code>
     * 如果物体比长度大，右边的部分就会丢失
     * 如果对象为空，则使用空文本值
     *
     * @param obj     要追加的对象null使用空文本
     * @param width   固定的字段宽度，零或负没有影响
     * @param padChar 要使用的填充字符
     * @return this
     */
    public Builders appendFixedWidthPadRight(final Object obj, final int width, final char padChar) {
        if (width > 0) {
            ensureCapacity(size + width);
            String str = null == obj ? getNullText() : obj.toString();
            if (null == str) {
                str = Normal.EMPTY;
            }
            final int strLen = str.length();
            if (strLen >= width) {
                str.getChars(0, width, buffer, size);
            } else {
                final int padLen = width - strLen;
                str.getChars(0, strLen, buffer, size);
                for (int i = 0; i < padLen; i++) {
                    buffer[size + strLen + i] = padChar;
                }
            }
            size += width;
        }
        return this;
    }

    /**
     * 将对象附加到右侧的生成器内边距，使其具有固定的长度
     * 使用<code>String.valueOf</code>字符串，使用<code>int</code>
     * 如果格式化的值大于长度，则右侧将丢失
     *
     * @param value   要附加的值
     * @param width   固定的字段宽度，零或负没有影响
     * @param padChar 要使用的填充字符
     * @return this
     */
    public Builders appendFixedWidthPadRight(final int value, final int width, final char padChar) {
        return appendFixedWidthPadRight(String.valueOf(value), width, padChar);
    }

    /**
     * 将对象的字符串表示形式插入到此生成器中
     * 插入null将使用存储的空文本值
     *
     * @param index 要添加的索引必须有效
     * @param obj   要插入的对象
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(final int index, final Object obj) {
        if (null == obj) {
            return insert(index, nullText);
        }
        return insert(index, obj.toString());
    }

    /**
     * 将字符串插入到此生成器中.
     * 插入null将使用存储的空文本值.
     *
     * @param index 要添加的索引必须有效
     * @param str   要插入的字符串
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(final int index, String str) {
        validateIndex(index);
        if (null == str) {
            str = nullText;
        }
        if (null != str) {
            final int strLen = str.length();
            if (strLen > 0) {
                final int newSize = size + strLen;
                ensureCapacity(newSize);
                System.arraycopy(buffer, index, buffer, index + strLen, size - index);
                size = newSize;
                str.getChars(0, strLen, buffer, index);
            }
        }
        return this;
    }

    /**
     * 将字符数组插入到此生成器中.
     * 插入null将使用存储的空文本值.
     *
     * @param index 要添加的索引必须有效
     * @param chars 要插入的char数组
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(final int index, final char[] chars) {
        validateIndex(index);
        if (null == chars) {
            return insert(index, nullText);
        }
        final int len = chars.length;
        if (len > 0) {
            ensureCapacity(size + len);
            System.arraycopy(buffer, index, buffer, index + len, size - index);
            System.arraycopy(chars, 0, buffer, index, len);
            size += len;
        }
        return this;
    }

    /**
     * 将字符数组的一部分插入到此生成器中.
     * 插入null将使用存储的空文本值.
     *
     * @param index  要添加的索引必须有效
     * @param chars  要插入的char数组
     * @param offset 字符数组中要开始的偏移量必须有效
     * @param length 要复制的字符数组部分的长度必须为正
     * @return this
     * @throws IndexOutOfBoundsException 如果任何索引无效
     */
    public Builders insert(final int index, final char[] chars, final int offset, final int length) {
        validateIndex(index);
        if (null == chars) {
            return insert(index, nullText);
        }
        if (offset < 0 || offset > chars.length) {
            throw new StringIndexOutOfBoundsException("Invalid offset: " + offset);
        }
        if (length < 0 || offset + length > chars.length) {
            throw new StringIndexOutOfBoundsException("Invalid length: " + length);
        }
        if (length > 0) {
            ensureCapacity(size + length);
            System.arraycopy(buffer, index, buffer, index + length, size - index);
            System.arraycopy(chars, offset, buffer, index, length);
            size += length;
        }
        return this;
    }

    /**
     * 将值插入此生成器.
     *
     * @param index 要添加的索引必须有效
     * @param value 要插入的值
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(int index, final boolean value) {
        validateIndex(index);
        if (value) {
            ensureCapacity(size + 4);
            System.arraycopy(buffer, index, buffer, index + 4, size - index);
            buffer[index++] = 't';
            buffer[index++] = 'r';
            buffer[index++] = 'u';
            buffer[index] = 'e';
            size += 4;
        } else {
            ensureCapacity(size + 5);
            System.arraycopy(buffer, index, buffer, index + 5, size - index);
            buffer[index++] = 'f';
            buffer[index++] = 'a';
            buffer[index++] = 'l';
            buffer[index++] = 's';
            buffer[index] = 'e';
            size += 5;
        }
        return this;
    }

    /**
     * 将值插入此生成器.
     *
     * @param index 要添加的索引必须有效
     * @param value 要插入的值
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(final int index, final char value) {
        validateIndex(index);
        ensureCapacity(size + 1);
        System.arraycopy(buffer, index, buffer, index + 1, size - index);
        buffer[index] = value;
        size++;
        return this;
    }

    /**
     * 将值插入此生成器.
     *
     * @param index 要添加的索引必须有效
     * @param value 要插入的值
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(final int index, final int value) {
        return insert(index, String.valueOf(value));
    }

    /**
     * 将值插入此生成器.
     *
     * @param index 要添加的索引必须有效
     * @param value 要插入的值
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(final int index, final long value) {
        return insert(index, String.valueOf(value));
    }

    /**
     * 将值插入此生成器.
     *
     * @param index 要添加的索引必须有效
     * @param value 要插入的值
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(final int index, final float value) {
        return insert(index, String.valueOf(value));
    }

    /**
     * 将值插入此生成器.
     *
     * @param index 要添加的索引必须有效
     * @param value 要插入的值
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders insert(final int index, final double value) {
        return insert(index, String.valueOf(value));
    }

    /**
     * 内部方法，用于在不进行验证的情况下删除范围
     *
     * @param startIndex 开始索引，必须是有效的
     * @param endIndex   结束索引(排他)必须有效
     * @param len        长度，必须是有效的
     * @throws IndexOutOfBoundsException 如果任何索引无效
     */
    private void deleteImpl(final int startIndex, final int endIndex, final int len) {
        System.arraycopy(buffer, endIndex, buffer, startIndex, size - endIndex);
        size -= len;
    }

    /**
     * 删除两个指定索引之间的字符
     *
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param endIndex   唯一的结束索引必须有效，除非太大，否则将被视为字符串的结束
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders delete(final int startIndex, int endIndex) {
        endIndex = validateRange(startIndex, endIndex);
        final int len = endIndex - startIndex;
        if (len > 0) {
            deleteImpl(startIndex, endIndex, len);
        }
        return this;
    }

    /**
     * 删除在生成器中出现的字符.
     *
     * @param ch 要删除的字符
     * @return this
     */
    public Builders deleteAll(final char ch) {
        for (int i = 0; i < size; i++) {
            if (buffer[i] == ch) {
                final int start = i;
                while (++i < size) {
                    if (buffer[i] != ch) {
                        break;
                    }
                }
                final int len = i - start;
                deleteImpl(start, i, len);
                i -= len;
            }
        }
        return this;
    }

    /**
     * 删除在生成器中出现的字符
     *
     * @param ch 要删除的字符
     * @return this
     */
    public Builders deleteFirst(final char ch) {
        for (int i = 0; i < size; i++) {
            if (buffer[i] == ch) {
                deleteImpl(i, i + 1, 1);
                break;
            }
        }
        return this;
    }

    /**
     * 删除生成器中出现的字符串
     *
     * @param str 若要删除的字符串为空，则不执行任何操作
     * @return this
     */
    public Builders deleteAll(final String str) {
        final int len = null == str ? 0 : str.length();
        if (len > 0) {
            int index = indexOf(str, 0);
            while (index >= 0) {
                deleteImpl(index, index + len, len);
                index = indexOf(str, index);
            }
        }
        return this;
    }

    /**
     * 删除生成器中出现的字符串
     *
     * @param str 若要删除的字符串为空，则不执行任何操作
     * @return this
     */
    public Builders deleteFirst(final String str) {
        final int len = null == str ? 0 : str.length();
        if (len > 0) {
            final int index = indexOf(str, 0);
            if (index >= 0) {
                deleteImpl(index, index + len, len);
            }
        }
        return this;
    }

    /**
     * 删除匹配程序匹配的生成器的所有部分.
     * 匹配器可用于执行高级删除行为。例如，
     * 您可以编写一个匹配器来删除字符“a”后面跟一个数字的所有匹配项
     *
     * @param matcher 要使用的matcher来查找删除，null不导致任何操作
     * @return this
     */
    public Builders deleteAll(final Matchers matcher) {
        return replace(matcher, null, 0, size, -1);
    }

    /**
     * 使用指定的匹配器删除生成器中的第一个匹配项.
     * <p>
     * 匹配器可用于执行高级删除行为。例如，您可以编写一个匹配器来删除字符“a”后面跟着一个数字的地方.
     *
     * @param matcher 要使用的matcher来查找删除，null不导致任何操作
     * @return this
     */
    public Builders deleteFirst(final Matchers matcher) {
        return replace(matcher, null, 0, size, 1);
    }

    /**
     * 内部方法，用于在不进行验证的情况下删除范围.
     *
     * @param startIndex 开始索引，必须是有效的
     * @param endIndex   结束索引(排他)必须有效
     * @param removeLen  要删除的长度(endIndex - startIndex)必须有效
     * @param insertStr  要替换的字符串null表示删除范围
     * @param insertLen  插入字符串的长度必须有效
     * @throws IndexOutOfBoundsException 如果任何索引无效
     */
    private void replaceImpl(final int startIndex, final int endIndex, final int removeLen, final String insertStr, final int insertLen) {
        final int newSize = size - removeLen + insertLen;
        if (insertLen != removeLen) {
            ensureCapacity(newSize);
            System.arraycopy(buffer, endIndex, buffer, startIndex + insertLen, size - endIndex);
            size = newSize;
        }
        if (insertLen > 0) {
            insertStr.getChars(0, insertLen, buffer, startIndex);
        }
    }

    /**
     * 将字符串生成器的一部分替换为另一个字符串
     * 插入字符串的长度不必与删除的长度匹配
     *
     * @param startIndex 开始索引，必须是有效的
     * @param endIndex   唯一的结束索引必须有效，除非太大，否则将被视为字符串的结束
     * @param replaceStr 要替换的字符串null表示删除范围
     * @return this
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Builders replace(final int startIndex, int endIndex, final String replaceStr) {
        endIndex = validateRange(startIndex, endIndex);
        final int insertLen = null == replaceStr ? 0 : replaceStr.length();
        replaceImpl(startIndex, endIndex, endIndex - startIndex, replaceStr, insertLen);
        return this;
    }

    /**
     * 在整个生成器中使用替换字符替换搜索字符
     *
     * @param search  搜索字符
     * @param replace 替换字符
     * @return this
     */
    public Builders replaceAll(final char search, final char replace) {
        if (search != replace) {
            for (int i = 0; i < size; i++) {
                if (buffer[i] == search) {
                    buffer[i] = replace;
                }
            }
        }
        return this;
    }

    /**
     * 用生成器中的替换字符替换搜索字符的第一个实例.
     *
     * @param search  搜索字符
     * @param replace 替换字符
     * @return this
     */
    public Builders replaceFirst(final char search, final char replace) {
        if (search != replace) {
            for (int i = 0; i < size; i++) {
                if (buffer[i] == search) {
                    buffer[i] = replace;
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 在整个生成器中使用替换字符串替换搜索字符串
     *
     * @param searchStr  如果搜索字符串为空，则不执行任何操作
     * @param replaceStr 替换字符串null相当于空字符串
     * @return this
     */
    public Builders replaceAll(final String searchStr, final String replaceStr) {
        final int searchLen = null == searchStr ? 0 : searchStr.length();
        if (searchLen > 0) {
            final int replaceLen = null == replaceStr ? 0 : replaceStr.length();
            int index = indexOf(searchStr, 0);
            while (index >= 0) {
                replaceImpl(index, index + searchLen, searchLen, replaceStr, replaceLen);
                index = indexOf(searchStr, index + replaceLen);
            }
        }
        return this;
    }

    /**
     * 用替换字符串替换搜索字符串的第一个实例
     *
     * @param searchStr  如果搜索字符串为空，则不执行任何操作
     * @param replaceStr 替换字符串null相当于空字符串
     * @return this
     */
    public Builders replaceFirst(final String searchStr, final String replaceStr) {
        final int searchLen = null == searchStr ? 0 : searchStr.length();
        if (searchLen > 0) {
            final int index = indexOf(searchStr, 0);
            if (index >= 0) {
                final int replaceLen = null == replaceStr ? 0 : replaceStr.length();
                replaceImpl(index, index + searchLen, searchLen, replaceStr, replaceLen);
            }
        }
        return this;
    }

    /**
     * 用替换字符串替换生成器中的所有匹配项
     *
     * @param matcher    要使用的matcher来查找删除，null不导致任何操作
     * @param replaceStr 替换字符串null相当于空字符串
     * @return this
     */
    public Builders replaceAll(final Matchers matcher, final String replaceStr) {
        return replace(matcher, replaceStr, 0, size, -1);
    }

    /**
     * 用替换字符串替换生成器中的所有匹配项
     *
     * @param matcher    要使用的matcher来查找删除，null不导致任何操作
     * @param replaceStr 替换字符串null相当于空字符串
     * @return this
     */
    public Builders replaceFirst(final Matchers matcher, final String replaceStr) {
        return replace(matcher, replaceStr, 0, size, 1);
    }

    /**
     * 高级搜索并在构建器中使用匹配器进行替换
     *
     * @param matcher      要使用的matcher来查找删除，null不导致任何操作
     * @param replaceStr   将匹配项替换为null的字符串是delete
     * @param startIndex   起始索引(包括起始索引)必须有效
     * @param endIndex     唯一的结束索引必须有效，除非太大，否则将被视为字符串的结束
     * @param replaceCount 要替换的次数，-1表示替换所有
     * @return this
     * @throws IndexOutOfBoundsException 如果开始索引无效
     */
    public Builders replace(
            final Matchers matcher, final String replaceStr,
            final int startIndex, int endIndex, final int replaceCount) {
        endIndex = validateRange(startIndex, endIndex);
        return replaceImpl(matcher, replaceStr, startIndex, endIndex, replaceCount);
    }

    /**
     * 使用匹配器在构建器中替换
     *
     * @param matcher      要使用的matcher来查找删除，null不导致任何操作
     * @param replaceStr   将匹配项替换为null的字符串是delete
     * @param from         开始索引，必须是有效的
     * @param to           结束索引(排他)必须有效
     * @param replaceCount 要替换的次数，-1表示替换所有
     * @return this
     * @throws IndexOutOfBoundsException 如果任何索引无效
     */
    private Builders replaceImpl(
            final Matchers matcher, final String replaceStr,
            final int from, int to, int replaceCount) {
        if (null == matcher || size == 0) {
            return this;
        }
        final int replaceLen = null == replaceStr ? 0 : replaceStr.length();
        final char[] buf = buffer;
        for (int i = from; i < to && replaceCount != 0; i++) {
            final int removeLen = matcher.isMatch(buf, i, from, to);
            if (removeLen > 0) {
                replaceImpl(i, i + removeLen, removeLen, replaceStr, replaceLen);
                to = to - removeLen + replaceLen;
                i = i + replaceLen - 1;
                if (replaceCount > 0) {
                    replaceCount--;
                }
            }
        }
        return this;
    }

    /**
     * 反转将每个字符放在相反索引中的字符串生成器
     *
     * @return this
     */
    public Builders reverse() {
        if (size == 0) {
            return this;
        }

        final int half = size / 2;
        final char[] buf = buffer;
        for (int leftIdx = 0, rightIdx = size - 1; leftIdx < half; leftIdx++, rightIdx--) {
            final char swap = buf[leftIdx];
            buf[leftIdx] = buf[rightIdx];
            buf[rightIdx] = swap;
        }
        return this;
    }

    /**
     * 通过从开头和结尾删除小于或等于空格的字符来修剪生成器
     *
     * @return this
     */
    public Builders trim() {
        if (size == 0) {
            return this;
        }
        int len = size;
        final char[] buf = buffer;
        int pos = 0;
        while (pos < len && buf[pos] <= Symbol.C_SPACE) {
            pos++;
        }
        while (pos < len && buf[len - 1] <= Symbol.C_SPACE) {
            len--;
        }
        if (len < size) {
            delete(len, size);
        }
        if (pos > 0) {
            delete(0, pos);
        }
        return this;
    }

    /**
     * 检查此生成器是否以指定的字符串开始
     *
     * @param str 要搜索的字符串null返回false
     * @return 如果生成器从字符串开始，则为真
     */
    public boolean startsWith(final String str) {
        if (null == str) {
            return false;
        }
        final int len = str.length();
        if (len == 0) {
            return true;
        }
        if (len > size) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (buffer[i] != str.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查此生成器是否以指定的字符串结束
     *
     * @param str 要搜索的字符串null返回false
     * @return 如果生成器以字符串结束，则为真
     */
    public boolean endsWith(final String str) {
        if (null == str) {
            return false;
        }
        final int len = str.length();
        if (len == 0) {
            return true;
        }
        if (len > size) {
            return false;
        }
        int pos = size - len;
        for (int i = 0; i < len; i++, pos++) {
            if (buffer[pos] != str.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CharSequence subSequence(final int startIndex, final int endIndex) {
        if (startIndex < 0) {
            throw new StringIndexOutOfBoundsException(startIndex);
        }
        if (endIndex > size) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if (startIndex > endIndex) {
            throw new StringIndexOutOfBoundsException(endIndex - startIndex);
        }
        return substring(startIndex, endIndex);
    }

    /**
     * 将此字符串生成器的一部分提取为字符串
     *
     * @param start 起始索引(包括起始索引)必须有效
     * @return 新的字符串
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public String substring(final int start) {
        return substring(start, size);
    }

    /**
     * 将此字符串生成器的一部分提取为字符串
     *
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param endIndex   唯一的结束索引必须有效，除非太大，否则将被视为字符串的结束
     * @return 新的字符串
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public String substring(final int startIndex, int endIndex) {
        endIndex = validateRange(startIndex, endIndex);
        return new String(buffer, startIndex, endIndex - startIndex);
    }

    /**
     * 从字符串生成器中提取最左边的字符而不引发异常.
     *
     * @param length 要提取的字符数，负返回空字符串
     * @return 新的字符串
     */
    public String leftString(final int length) {
        if (length <= 0) {
            return Normal.EMPTY;
        } else if (length >= size) {
            return new String(buffer, 0, size);
        } else {
            return new String(buffer, 0, length);
        }
    }

    /**
     * 从字符串生成器中提取最右边的字符而不引发异常.
     *
     * @param length 要提取的字符数，负返回空字符串
     * @return 新的字符串
     */
    public String rightString(final int length) {
        if (length <= 0) {
            return Normal.EMPTY;
        } else if (length >= size) {
            return new String(buffer, 0, size);
        } else {
            return new String(buffer, size - length, length);
        }
    }

    /**
     * 从字符串生成器中间提取一些字符而不引发异常
     *
     * @param index  下标从-开始，表示0
     * @param length 要提取的字符数，负返回空字符串
     * @return 新的字符串
     */
    public String midString(int index, final int length) {
        if (index < 0) {
            index = 0;
        }
        if (length <= 0 || index >= size) {
            return Normal.EMPTY;
        }
        if (size <= index + length) {
            return new String(buffer, index, size - index);
        }
        return new String(buffer, index, length);
    }

    /**
     * 检查字符串生成器是否包含指定的字符
     *
     * @param ch 要找的字符
     * @return 如果生成器包含该字符，则为真
     */
    public boolean contains(final char ch) {
        final char[] thisBuf = buffer;
        for (int i = 0; i < this.size; i++) {
            if (thisBuf[i] == ch) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查字符串生成器是否包含指定的字符串
     *
     * @param str 要找的字符
     * @return 如果生成器包含该字符，则为真
     */
    public boolean contains(final String str) {
        return indexOf(str, 0) >= 0;
    }

    /**
     * 检查字符串生成器是否包含使用指定匹配器匹配的字符串
     *
     * @param matcher 要使用的匹配器，如果为null返回-1
     * @return 如果匹配器在生成器中找到匹配项，则为真
     */
    public boolean contains(final Matchers matcher) {
        return indexOf(matcher, 0) >= 0;
    }

    /**
     * 搜索字符串生成器以查找指定char类型的第一个引用.
     *
     * @param ch 要找的字符
     * @return 字符的第一个索引，如果没有找到则为-1
     */
    public int indexOf(final char ch) {
        return indexOf(ch, 0);
    }

    /**
     * 搜索字符串生成器以查找指定char类型的第一个引用.
     *
     * @param ch         要找的字符
     * @param startIndex 从索引开始，无效的索引四舍五入到边缘
     * @return 字符的第一个索引，如果没有找到则为-1
     */
    public int indexOf(final char ch, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (startIndex >= size) {
            return -1;
        }
        final char[] thisBuf = buffer;
        for (int i = startIndex; i < size; i++) {
            if (thisBuf[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 搜索字符串生成器以查找指定字符串的第一个引用
     *
     * @param str 要查找的字符串，如果为null返回-1
     * @return 字符的第一个索引，如果没有找到则为-1
     */
    public int indexOf(final String str) {
        return indexOf(str, 0);
    }

    /**
     * 从给定索引开始搜索，搜索字符串生成器以查找指定字符串的第一个引用
     *
     * @param str        要查找的字符串，如果为null返回-1
     * @param startIndex 从索引开始，无效的索引四舍五入到边缘
     * @return 字符的第一个索引，如果没有找到则为-1
     */
    public int indexOf(final String str, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (null == str || startIndex >= size) {
            return -1;
        }
        final int strLen = str.length();
        if (strLen == 1) {
            return indexOf(str.charAt(0), startIndex);
        }
        if (strLen == 0) {
            return startIndex;
        }
        if (strLen > size) {
            return -1;
        }
        final char[] thisBuf = buffer;
        final int len = size - strLen + 1;
        outer:
        for (int i = startIndex; i < len; i++) {
            for (int j = 0; j < strLen; j++) {
                if (str.charAt(j) != thisBuf[i + j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /**
     * 使用matcher搜索字符串生成器以查找第一个匹配项.
     *
     * @param matcher 要使用的匹配器，null返回-1
     * @return 第一个索引匹配，如果没有找到，则为-1
     */
    public int indexOf(final Matchers matcher) {
        return indexOf(matcher, 0);
    }

    /**
     * 使用matcher搜索字符串生成器，以查找从给定索引中搜索的第一个匹配项.
     *
     * @param matcher    要使用的匹配器，null返回-1
     * @param startIndex 从索引开始，无效的索引四舍五入到边缘
     * @return 第一个索引匹配，如果没有找到，则为-1
     */
    public int indexOf(final Matchers matcher, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (null == matcher || startIndex >= size) {
            return -1;
        }
        final int len = size;
        final char[] buf = buffer;
        for (int i = startIndex; i < len; i++) {
            if (matcher.isMatch(buf, i, startIndex, len) > 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 搜索字符串生成器以查找对指定字符的最后一个引用.
     *
     * @param ch 要找的字符
     * @return 字符的最后一个索引，如果没有找到，则为-1
     */
    public int lastIndexOf(final char ch) {
        return lastIndexOf(ch, size - 1);
    }

    /**
     * 搜索字符串生成器以查找对指定字符的最后一个引用.
     *
     * @param ch         要找的字符
     * @param startIndex 从索引开始，无效的索引四舍五入到边缘
     * @return 字符的最后一个索引，如果没有找到，则为-1
     */
    public int lastIndexOf(final char ch, int startIndex) {
        startIndex = (startIndex >= size ? size - 1 : startIndex);
        if (startIndex < 0) {
            return -1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (buffer[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 搜索字符串生成器以查找对指定字符串的最后引用
     *
     * @param str 要查找的字符串，null返回-1
     * @return 字符串的最后一个索引，如果没有找到，则为-1
     */
    public int lastIndexOf(final String str) {
        return lastIndexOf(str, size - 1);
    }

    /**
     * 从给定索引开始搜索，搜索字符串生成器以查找指定字符串的最后一个引用
     *
     * @param str        要查找的字符串，null返回-1
     * @param startIndex 从索引开始，无效的索引四舍五入到边缘
     * @return 字符串的最后一个索引，如果没有找到，则为-1
     */
    public int lastIndexOf(final String str, int startIndex) {
        startIndex = (startIndex >= size ? size - 1 : startIndex);
        if (null == str || startIndex < 0) {
            return -1;
        }
        final int strLen = str.length();
        if (strLen > 0 && strLen <= size) {
            if (strLen == 1) {
                return lastIndexOf(str.charAt(0), startIndex);
            }

            outer:
            for (int i = startIndex - strLen + 1; i >= 0; i--) {
                for (int j = 0; j < strLen; j++) {
                    if (str.charAt(j) != buffer[i + j]) {
                        continue outer;
                    }
                }
                return i;
            }

        } else if (strLen == 0) {
            return startIndex;
        }
        return -1;
    }

    /**
     * 使用matcher搜索字符串生成器以查找最后一个匹配项
     *
     * @param matcher 要使用的匹配器，null返回-1
     * @return 最后一个索引匹配，如果没有找到，则为-1
     */
    public int lastIndexOf(final Matchers matcher) {
        return lastIndexOf(matcher, size);
    }

    /**
     * 使用matcher搜索字符串生成器，以查找从给定索引中搜索的最后一个匹配项
     *
     * @param matcher    要使用的匹配器，null返回-1
     * @param startIndex 从索引开始，无效的索引四舍五入到边缘
     * @return 最后一个索引匹配，如果没有找到，则为-1
     */
    public int lastIndexOf(final Matchers matcher, int startIndex) {
        startIndex = (startIndex >= size ? size - 1 : startIndex);
        if (null == matcher || startIndex < 0) {
            return -1;
        }
        final char[] buf = buffer;
        final int endIndex = startIndex + 1;
        for (int i = startIndex; i >= 0; i--) {
            if (matcher.isMatch(buf, i, 0, endIndex) > 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 检查此生成器的内容与另一个生成器的内容是否包含相同的字符内容(忽略大小写)
     *
     * @param other 要检查的对象null返回false
     * @return 如果生成器以相同的顺序包含相同的字符，则为真
     */
    public boolean equalsIgnoreCase(final Builders other) {
        if (this == other) {
            return true;
        }
        if (this.size != other.size) {
            return false;
        }
        final char[] thisBuf = this.buffer;
        final char[] otherBuf = other.buffer;
        for (int i = size - 1; i >= 0; i--) {
            final char c1 = thisBuf[i];
            final char c2 = otherBuf[i];
            if (c1 != c2 && Character.toUpperCase(c1) != Character.toUpperCase(c2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查此生成器的内容与另一个生成器的内容是否包含相同的字符内容.
     *
     * @param other 要检查的对象null返回false
     * @return 如果生成器以相同的顺序包含相同的字符，则为真
     */
    public boolean equals(final Builders other) {
        if (this == other) {
            return true;
        }
        if (null == other) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        final char[] thisBuf = this.buffer;
        final char[] otherBuf = other.buffer;
        for (int i = size - 1; i >= 0; i--) {
            if (thisBuf[i] != otherBuf[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 实现{@link Builder}接口
     *
     * @return 构建器一个 String
     * @see #toString()
     */
    @Override
    public String build() {
        return toString();
    }

    /**
     * 检查此生成器的内容与另一个生成器的内容是否包含相同的字符内容.
     *
     * @param obj 要检查的对象null返回false
     * @return 如果生成器以相同的顺序包含相同的字符，则为真
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Builders && equals((Builders) obj);
    }

    @Override
    public int hashCode() {
        final char[] buf = buffer;
        int hash = 0;
        for (int i = size - 1; i >= 0; i--) {
            hash = 31 * hash + buf[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        return new String(buffer, 0, size);
    }

    /**
     * 生成字符串
     *
     * @param isReset 是否重置，重置后相当于空的构建器
     * @return 生成的字符串
     */
    public String toString(boolean isReset) {
        if (this.size > 0) {
            final String s = new String(this.buffer, 0, this.size);
            if (isReset) {
                reset();
            }
            return s;
        }
        return Normal.EMPTY;
    }

    /**
     * 获取字符串生成器的StringBuffer版本，在每次调用该方法时创建一个新实例
     *
     * @return 构建器一个 StringBuffer
     */
    public StringBuffer toStringBuffer() {
        return new StringBuffer(size).append(buffer, 0, size);
    }

    /**
     * 获取字符串生成器的StringBuilder版本，在每次调用该方法时创建一个新实例
     *
     * @return 构建器一个 StringBuilder
     */
    public StringBuilder toStringBuilder() {
        return new StringBuilder(size).append(buffer, 0, size);
    }

    /**
     * 验证定义生成器范围的参数.
     *
     * @param startIndex 起始索引(包括起始索引)必须有效
     * @param endIndex   唯一的结束索引必须有效，除非太大，否则将被视为字符串的结束
     * @return 新的字符串
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    protected int validateRange(final int startIndex, int endIndex) {
        if (startIndex < 0) {
            throw new StringIndexOutOfBoundsException(startIndex);
        }
        if (endIndex > size) {
            endIndex = size;
        }
        if (startIndex > endIndex) {
            throw new StringIndexOutOfBoundsException("end < start");
        }
        return endIndex;
    }

    /**
     * 验证在生成器中定义单个索引的参数
     *
     * @param index 索引，必须是有效的
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    protected void validateIndex(final int index) {
        if (index < 0 || index > size) {
            throw new StringIndexOutOfBoundsException(index);
        }
    }

}
