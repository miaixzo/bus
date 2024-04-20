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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.TextBuilder;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 可复用的字符串生成器,非线程安全
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextKit implements CharSequence, Appendable, Serializable {

    /**
     * 存放的字符数组
     */
    private char[] value;
    /**
     * 当前指针位置,或者叫做已经加入的字符数,此位置总在最后一个字符之后
     */
    private int position;

    /**
     * 构造
     */
    public TextKit() {
        this(TextBuilder.CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     */
    public TextKit(int initialCapacity) {
        value = new char[initialCapacity];
    }

    /**
     * 构造
     *
     * @param texts 初始字符串
     */
    public TextKit(CharSequence... texts) {
        this(ArrayKit.isEmpty(texts) ? TextBuilder.CAPACITY : (TextBuilder.totalLength(texts) + TextBuilder.CAPACITY));
        for (int i = 0; i < texts.length; i++) {
            append(texts[i]);
        }
    }

    /**
     * 创建字符串构建器
     *
     * @return {@link TextKit}
     */
    public static TextKit of() {
        return new TextKit();
    }

    /**
     * 创建字符串构建器
     *
     * @param initialCapacity 初始容量
     * @return {@link TextKit}
     */
    public static TextKit create(int initialCapacity) {
        return new TextKit(initialCapacity);
    }

    /**
     * 创建字符串构建器
     *
     * @param texts 初始字符串
     * @return {@link TextKit}
     */
    public static TextKit create(CharSequence... texts) {
        return new TextKit(texts);
    }

    /**
     * 计算相似度
     *
     * @param strA 字符串1
     * @param strB 字符串2
     * @return 相似度
     */
    public static double similar(String strA, String strB) {
        String newStrA, newStrB;
        if (strA.length() < strB.length()) {
            newStrA = removeSign(strB);
            newStrB = removeSign(strA);
        } else {
            newStrA = removeSign(strA);
            newStrB = removeSign(strB);
        }
        // 用较大的字符串长度作为分母,相似子串作为分子计算出字串相似度
        int temp = Math.max(newStrA.length(), newStrB.length());
        int temp2 = longestCommonSubstring(newStrA, newStrB).length();
        return MathKit.div(temp2, temp);
    }

    /**
     * 计算相似度百分比
     *
     * @param strA  字符串1
     * @param strB  字符串2
     * @param scale 保留小数
     * @return 百分比
     */
    public static String similar(String strA, String strB, int scale) {
        return MathKit.formatPercent(similar(strA, strB), scale);
    }

    /**
     * 将字符串的所有数据依次写成一行,去除无意义字符串
     *
     * @param text 字符串
     * @return 处理后的字符串
     */
    private static String removeSign(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        // 遍历字符串str,如果是汉字数字或字母,则追加到ab上面
        int length = text.length();
        for (int i = 0; i < length; i++) {
            sb.append(charReg(text.charAt(i)));
        }
        return sb.toString();
    }

    /**
     * 判断字符是否为汉字,数字和字母, 因为对符号进行相似度比较没有实际意义,故符号不加入考虑范围
     *
     * @param charValue 字符
     * @return 是否为汉字, 数字和字母
     */
    private static boolean charReg(char charValue) {
        return (charValue >= 0x4E00 && charValue <= 0XFFF) || //
                (charValue >= 'a' && charValue <= 'z') || //
                (charValue >= 'A' && charValue <= 'Z') || //
                (charValue >= Symbol.C_ZERO && charValue <= Symbol.C_NINE);
    }

    /**
     * 求公共子串,采用动态规划算法  其不要求所求得的字符在所给的字符串中是连续的
     *
     * @param strA 字符串1
     * @param strB 字符串2
     * @return 公共子串
     */
    private static String longestCommonSubstring(String strA, String strB) {
        char[] chars_strA = strA.toCharArray();
        char[] chars_strB = strB.toCharArray();
        int m = chars_strA.length;
        int n = chars_strB.length;

        // 初始化矩阵数据,matrix[0][0]的值为0, 如果字符数组chars_strA和chars_strB的对应位相同,则matrix[i][j]的值为左上角的值加1, 否则,matrix[i][j]的值等于左上方最近两个位置的较大值, 矩阵中其余各点的值为0.
        int[][] matrix = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (chars_strA[i - 1] == chars_strB[j - 1]) {
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                } else {
                    matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
                }
            }
        }

        // 矩阵中,如果matrix[m][n]的值不等于matrix[m-1][n]的值也不等于matrix[m][n-1]的值, 则matrix[m][n]对应的字符为相似字符元,并将其存入result数组中
        char[] result = new char[matrix[m][n]];
        int currentIndex = result.length - 1;
        while (matrix[m][n] != 0) {
            if (matrix[m][n] == matrix[m][n - 1]) {
                n--;
            } else if (matrix[m][n] == matrix[m - 1][n]) {
                m--;
            } else {
                result[currentIndex] = chars_strA[m - 1];
                currentIndex--;
                n--;
                m--;
            }
        }
        return new String(result);
    }

    /**
     * 追加对象,对象会被转换为字符串
     *
     * @param object 对象
     * @return this
     */
    public TextKit append(Object object) {
        return insert(this.position, object);
    }

    /**
     * 追加一个字符
     *
     * @param c 字符
     * @return this
     */
    @Override
    public TextKit append(char c) {
        return insert(this.position, c);
    }

    /**
     * 追加一个字符数组
     *
     * @param src 字符数组
     * @return this
     */
    public TextKit append(char[] src) {
        if (ArrayKit.isEmpty(src)) {
            return this;
        }
        return append(src, 0, src.length);
    }

    /**
     * 追加一个字符数组
     *
     * @param src    字符数组
     * @param srcPos 开始位置(包括)
     * @param length 长度
     * @return this
     */
    public TextKit append(char[] src, int srcPos, int length) {
        return insert(this.position, src, srcPos, length);
    }

    @Override
    public TextKit append(CharSequence csq) {
        return insert(this.position, csq);
    }

    @Override
    public TextKit append(CharSequence csq, int start, int end) {
        return insert(this.position, csq, start, end);
    }

    /**
     * 追加对象,对象会被转换为字符串
     *
     * @param index  位置
     * @param object 对象
     * @return this
     */
    public TextKit insert(int index, Object object) {
        if (object instanceof CharSequence) {
            return insert(index, (CharSequence) object);
        }
        return insert(index, Convert.toString(object));
    }

    /**
     * 插入指定字符
     *
     * @param index 位置
     * @param c     字符
     * @return this
     */
    public TextKit insert(int index, char c) {
        if (index < 0) {
            index = this.position + index;
        }
        if ((index < 0)) {
            throw new StringIndexOutOfBoundsException(index);
        }

        moveDataAfterIndex(index, 1);
        value[index] = c;
        this.position = Math.max(this.position, index) + 1;
        return this;
    }

    /**
     * 指定位置插入数据
     * 如果插入位置为当前位置,则定义为追加
     * 如果插入位置大于当前位置,则中间部分补充空格
     *
     * @param index 插入位置
     * @param src   源数组
     * @return this
     */
    public TextKit insert(int index, char[] src) {
        if (ArrayKit.isEmpty(src)) {
            return this;
        }
        return insert(index, src, 0, src.length);
    }

    /**
     * 指定位置插入数据
     * 如果插入位置为当前位置,则定义为追加
     * 如果插入位置大于当前位置,则中间部分补充空格
     *
     * @param index  插入位置
     * @param src    源数组
     * @param srcPos 位置
     * @param length 长度
     * @return this
     */
    public TextKit insert(int index, char[] src, int srcPos, int length) {
        if (ArrayKit.isEmpty(src) || srcPos > src.length || length <= 0) {
            return this;
        }
        if (index < 0) {
            index = 0;
        }
        if (srcPos < 0) {
            srcPos = 0;
        } else if (srcPos + length > src.length) {
            // 长度越界,只截取最大长度
            length = src.length - srcPos;
        }

        moveDataAfterIndex(index, length);
        // 插入数据
        System.arraycopy(src, srcPos, value, index, length);
        this.position = Math.max(this.position, index) + length;
        return this;
    }

    /**
     * 指定位置插入字符串的某个部分
     * 如果插入位置为当前位置,则定义为追加
     * 如果插入位置大于当前位置,则中间部分补充空格
     *
     * @param index 位置
     * @param csq   字符串
     * @return this
     */
    public TextKit insert(int index, CharSequence csq) {
        if (null == csq) {
            csq = Normal.EMPTY;
        }
        int len = csq.length();
        moveDataAfterIndex(index, csq.length());
        if (csq instanceof String) {
            ((String) csq).getChars(0, len, this.value, index);
        } else if (csq instanceof java.lang.StringBuilder) {
            ((java.lang.StringBuilder) csq).getChars(0, len, this.value, index);
        } else if (csq instanceof StringBuffer) {
            ((StringBuffer) csq).getChars(0, len, this.value, index);
        } else if (csq instanceof TextKit) {
            ((TextKit) csq).getChars(0, len, this.value, index);
        } else {
            for (int i = 0, j = this.position; i < len; i++, j++) {
                this.value[j] = csq.charAt(i);
            }
        }
        this.position = Math.max(this.position, index) + len;
        return this;
    }

    /**
     * 指定位置插入字符串的某个部分
     * 如果插入位置为当前位置,则定义为追加
     * 如果插入位置大于当前位置,则中间部分补充空格
     *
     * @param index 位置
     * @param csq   字符串
     * @param start 字符串开始位置(包括)
     * @param end   字符串结束位置(不包括)
     * @return this
     */
    public TextKit insert(int index, CharSequence csq, int start, int end) {
        if (null == csq) {
            csq = Normal.NULL;
        }
        final int csqLen = csq.length();
        if (start > csqLen) {
            return this;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > csqLen) {
            end = csqLen;
        }
        if (start >= end) {
            return this;
        }
        if (index < 0) {
            index = 0;
        }

        final int length = end - start;
        moveDataAfterIndex(index, length);
        for (int i = start, j = this.position; i < end; i++, j++) {
            value[j] = csq.charAt(i);
        }
        this.position = Math.max(this.position, index) + length;
        return this;
    }

    /**
     * 将指定段的字符列表写出到目标字符数组中
     *
     * @param srcBegin 起始位置(包括)
     * @param srcEnd   结束位置(不包括)
     * @param dst      目标数组
     * @param dstBegin 目标起始位置(包括)
     * @return this
     */
    public TextKit getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (srcBegin < 0) {
            srcBegin = 0;
        }
        if (srcEnd < 0) {
            srcEnd = 0;
        } else if (srcEnd > this.position) {
            srcEnd = this.position;
        }
        if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
        }
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
        return this;
    }

    /**
     * 是否有内容
     *
     * @return 是否有内容
     */
    public boolean hasContent() {
        return position > 0;
    }

    /**
     * 是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return position == 0;
    }

    /**
     * 删除全部字符,位置归零
     *
     * @return this
     */
    public TextKit clear() {
        return reset();
    }

    /**
     * 删除全部字符,位置归零
     *
     * @return this
     */
    public TextKit reset() {
        this.position = 0;
        return this;
    }

    /**
     * 删除到指定位置
     * 如果新位置小于等于0,则删除全部
     *
     * @param newPosition 新的位置,不包括这个位置
     * @return this
     */
    public TextKit delTo(int newPosition) {
        if (newPosition < 0) {
            this.reset();
        } else if (newPosition < this.position) {
            this.position = newPosition;
        }
        return this;
    }

    /**
     * 删除指定长度的字符
     *
     * @param start 开始位置(包括)
     * @param end   结束位置(不包括)
     * @return this
     */
    public TextKit del(int start, int end) {
        if (start < 0) {
            start = 0;
        }
        if (end > this.position) {
            end = this.position;
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException("Start is greater than End.");
        }
        if (end == this.position) {
            this.position = start;
        }

        int len = end - start;
        if (len > 0) {
            System.arraycopy(value, start + len, value, start, this.position - end);
            this.position -= len;
        }
        return this;
    }

    /**
     * 生成字符串
     *
     * @param isReset 是否重置,重置后相当于空的构建器
     * @return 生成的字符串
     */
    public String toString(boolean isReset) {
        if (position > 0) {
            final String s = new String(value, 0, position);
            if (isReset) {
                reset();
            }
            return s;
        }
        return Normal.EMPTY;
    }

    /**
     * 重置并返回生成的字符串
     *
     * @return 字符串
     */
    public String toStringAndReset() {
        return toString(true);
    }

    /**
     * 生成字符串
     */
    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public int length() {
        return this.position;
    }

    @Override
    public char charAt(int index) {
        if ((index < 0) || (index > this.position)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return this.value[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return subString(start, end);
    }

    /**
     * 返回自定段的字符串
     *
     * @param start 开始位置(包括)
     * @return this
     */
    public String subString(int start) {
        return subString(start, this.position);
    }

    /**
     * 返回自定段的字符串
     *
     * @param start 开始位置(包括)
     * @param end   结束位置(不包括)
     * @return this
     */
    public String subString(int start, int end) {
        return new String(this.value, start, end - start);
    }

    /**
     * 指定位置之后的数据后移指定长度
     *
     * @param index  位置
     * @param length 位移长度
     */
    private void moveDataAfterIndex(int index, int length) {
        ensureCapacity(Math.max(this.position, index) + length);
        if (index < this.position) {
            // 插入位置在已有数据范围内,后移插入位置之后的数据
            System.arraycopy(this.value, index, this.value, index + length, this.position - index);
        } else if (index > this.position) {
            // 插入位置超出范围,则当前位置到index清除为空格
            Arrays.fill(this.value, this.position, index, Symbol.C_SPACE);
        }
        // 不位移
    }

    /**
     * 确认容量是否够用,不够用则扩展容量
     *
     * @param minimumCapacity 最小容量
     */
    private void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > value.length) {
            expandCapacity(minimumCapacity);
        }
    }

    /**
     * 扩展容量
     * 首先对容量进行二倍扩展,如果小于最小容量,则扩展为最小容量
     *
     * @param minimumCapacity 需要扩展的最小容量
     */
    private void expandCapacity(int minimumCapacity) {
        int newCapacity = value.length * 2 + 2;
        if (newCapacity < minimumCapacity) {
            newCapacity = minimumCapacity;
        }
        if (newCapacity < 0) {
            if (minimumCapacity < 0) {
                // overflow
                throw new OutOfMemoryError("Capacity is too long and max than Integer.MAX");
            }
            newCapacity = Integer.MAX_VALUE;
        }
        value = Arrays.copyOf(value, newCapacity);
    }

}
