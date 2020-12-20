/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.lang.Murmur;
import org.aoju.bus.core.toolkit.MathKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 文本相似度计算,局部敏感hash,用于海量文本去重
 * 局部敏感hash定义：假定两个字符串具有一定的相似性,
 * 在hash之后,仍然能保持这种相似性,就称之为局部敏感hash
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
public class Similarity {

    private final int bitNum = 64;
    /**
     * 存储段数,默认按照4段进行simhash存储
     */
    private final int fracCount;
    private final int fracBitNum;
    /**
     * 汉明距离的衡量标准,小于此距离标准表示相似
     */
    private final int hammingThresh;

    /**
     * 按照分段存储simhash,查找更快速
     */
    private List<Map<String, List<Long>>> storage;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 构造
     */
    public Similarity() {
        this(4, 3);
    }

    /**
     * 构造
     *
     * @param fracCount     存储段数
     * @param hammingThresh 汉明距离的衡量标准
     */
    public Similarity(int fracCount, int hammingThresh) {
        this.fracCount = fracCount;
        this.fracBitNum = bitNum / fracCount;
        this.hammingThresh = hammingThresh;
        this.storage = new ArrayList<>(fracCount);
        for (int i = 0; i < fracCount; i++) {
            storage.add(new HashMap<>());
        }
    }

    /**
     * 计算相似度，两个都是空串相似度为1，被认为是相同的串
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

        // 用较大的字符串长度作为分母，相似子串作为分子计算出字串相似度
        int temp = Math.max(newStrA.length(), newStrB.length());
        if (0 == temp) {
            // 两个都是空串相似度为1，被认为是相同的串
            return 1;
        }

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
     * 将字符串的所有数据依次写成一行，去除无意义字符串
     *
     * @param str 字符串
     * @return 处理后的字符串
     */
    private static String removeSign(String str) {
        int length = str.length();
        StringBuilder sb = StringKit.builder(length);
        // 遍历字符串str,如果是汉字数字或字母，则追加到ab上面
        char c;
        for (int i = 0; i < length; i++) {
            c = str.charAt(i);
            if (isValidChar(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 判断字符是否为汉字，数字和字母， 因为对符号进行相似度比较没有实际意义，故符号不加入考虑范围。
     *
     * @param charValue 字符
     * @return true表示为非汉字，数字和字母，false反之
     */
    private static boolean isValidChar(char charValue) {
        return (charValue >= 0x4E00 && charValue <= 0X9FFF) ||
                (charValue >= 'a' && charValue <= 'z') ||
                (charValue >= 'A' && charValue <= 'Z') ||
                (charValue >= '0' && charValue <= '9');
    }

    /**
     * 求公共子串，采用动态规划算法。 其不要求所求得的字符在所给的字符串中是连续的。
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

        // 初始化矩阵数据,matrix[0][0]的值为0,如果字符数组chars_strA和chars_strB的对应位相同，
        // 则matrix[i][j]的值为左上角的值加1,否则，matrix[i][j]的值等于左上方最近两个位置的较大值,矩阵中其余各点的值为0
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

        // 矩阵中，如果matrix[m][n]的值不等于matrix[m-1][n]的值也不等于matrix[m][n-1]的值,
        // 则matrix[m][n]对应的字符为相似字符元，并将其存入result数组中
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
     * 指定文本计算simhash值
     *
     * @param segList 分词的词列表
     * @return Hash值
     */
    public long hash(Collection<? extends CharSequence> segList) {
        final int bitNum = this.bitNum;
        // 按照词语的hash值,计算simHashWeight(低位对齐)
        final int[] weight = new int[bitNum];
        long wordHash;
        for (CharSequence seg : segList) {
            wordHash = Murmur.hash64(seg);
            for (int i = 0; i < bitNum; i++) {
                if (((wordHash >> i) & 1) == 1)
                    weight[i] += 1;
                else
                    weight[i] -= 1;
            }
        }

        // 计算得到Simhash值
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitNum; i++) {
            sb.append((weight[i] > 0) ? 1 : 0);
        }

        return new BigInteger(sb.toString(), 2).longValue();
    }

    /**
     * 判断文本是否与已存储的数据重复
     *
     * @param segList 文本分词后的结果
     * @return 是否重复
     */
    public boolean equals(Collection<? extends CharSequence> segList) {
        long simhash = hash(segList);
        final List<String> fracList = splitSimhash(simhash);
        final int hammingThresh = this.hammingThresh;

        String frac;
        Map<String, List<Long>> fracMap;
        final ReentrantReadWriteLock.ReadLock readLock = this.lock.readLock();
        readLock.lock();
        try {
            for (int i = 0; i < fracCount; i++) {
                frac = fracList.get(i);
                fracMap = storage.get(i);
                if (fracMap.containsKey(frac)) {
                    for (Long simhash2 : fracMap.get(frac)) {
                        // 当汉明距离小于标准时相似
                        if (hamming(simhash, simhash2) < hammingThresh) {
                            return true;
                        }
                    }
                }
            }
        } finally {
            readLock.unlock();
        }
        return false;
    }

    /**
     * 按照索引进行存储
     *
     * @param simhash Simhash值
     */
    public void store(Long simhash) {
        final int fracCount = this.fracCount;
        final List<Map<String, List<Long>>> storage = this.storage;
        final List<String> lFrac = splitSimhash(simhash);

        String frac;
        Map<String, List<Long>> fracMap;
        final ReentrantReadWriteLock.WriteLock writeLock = this.lock.writeLock();
        writeLock.lock();
        try {
            for (int i = 0; i < fracCount; i++) {
                frac = lFrac.get(i);
                fracMap = storage.get(i);
                if (fracMap.containsKey(frac)) {
                    fracMap.get(frac).add(simhash);
                } else {
                    final List<Long> ls = new ArrayList<>();
                    ls.add(simhash);
                    fracMap.put(frac, ls);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 计算汉明距离
     *
     * @param s1 值1
     * @param s2 值2
     * @return 汉明距离
     */
    private int hamming(Long s1, Long s2) {
        final int bitNum = this.bitNum;
        int dis = 0;
        for (int i = 0; i < bitNum; i++) {
            if ((s1 >> i & 1) != (s2 >> i & 1))
                dis++;
        }
        return dis;
    }

    /**
     * 将simhash分成n段
     *
     * @param simhash Simhash值
     * @return N段Simhash
     */
    private List<String> splitSimhash(Long simhash) {
        final int bitNum = this.bitNum;
        final int fracBitNum = this.fracBitNum;

        final List<String> ls = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitNum; i++) {
            sb.append(simhash >> i & 1);
            if ((i + 1) % fracBitNum == 0) {
                ls.add(sb.toString());
                sb.setLength(0);
            }
        }
        return ls;
    }

}
