/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.bloom;

import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.HashKit;
import org.aoju.bus.core.toolkit.IoKit;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.BitSet;

/**
 * BloomFilter实现方式2，此方式使用BitSet存储
 * Hash算法的使用使用固定顺序，只需指定个数即可
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public class BitSetBloomFilter implements BloomFilter {

    private static final long serialVersionUID = 1L;

    private final BitSet bitSet;
    private final int bitSetSize;
    private final int addedElements;
    private final int hashFunctionNumber;

    /**
     * 构造一个布隆过滤器，过滤器的容量为c * n 个bit
     *
     * @param c 当前过滤器预先开辟的最大包含记录,通常要比预计存入的记录多一倍
     * @param n 当前过滤器预计所要包含的记录
     * @param k 哈希函数的个数，等同每条记录要占用的bit数
     */
    public BitSetBloomFilter(int c, int n, int k) {
        this.hashFunctionNumber = k;
        this.bitSetSize = (int) Math.ceil(c * k);
        this.addedElements = n;
        this.bitSet = new BitSet(this.bitSetSize);
    }

    /**
     * 将字符串的字节表示进行多哈希编码
     *
     * @param text       待添加进过滤器的字符串字节表示
     * @param hashNumber 要经过的哈希个数
     * @return 各个哈希的结果数组
     */
    public static int[] createHashes(String text, int hashNumber) {
        int[] result = new int[hashNumber];
        for (int i = 0; i < hashNumber; i++) {
            result[i] = hash(text, i);

        }
        return result;
    }

    /**
     * 计算Hash值
     *
     * @param text 被计算Hash的字符串
     * @param k    Hash算法序号
     * @return Hash值
     */
    public static int hash(String text, int k) {
        switch (k) {
            case 0:
                return HashKit.rsHash(text);
            case 1:
                return HashKit.jsHash(text);
            case 2:
                return HashKit.elfHash(text);
            case 3:
                return HashKit.bkdrHash(text);
            case 4:
                return HashKit.apHash(text);
            case 5:
                return HashKit.djbHash(text);
            case 6:
                return HashKit.sdbmHash(text);
            case 7:
                return HashKit.pjwHash(text);
            default:
                return 0;
        }
    }

    /**
     * 通过文件初始化过滤器.
     *
     * @param path    文件路径
     * @param charset 字符集
     * @throws IOException IO异常
     */
    public void init(String path, String charset) throws IOException {
        BufferedReader reader = FileKit.getReader(path, charset);
        try {
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                this.add(line);
            }
        } finally {
            IoKit.close(reader);
        }
    }

    @Override
    public boolean add(String text) {
        if (contains(text)) {
            return false;
        }

        int[] positions = createHashes(text, hashFunctionNumber);
        for (int value : positions) {
            int position = Math.abs(value % bitSetSize);
            bitSet.set(position, true);
        }
        return true;
    }

    /**
     * 判定是否包含指定字符串
     *
     * @param text 字符串
     * @return 是否包含，存在误差
     */
    @Override
    public boolean contains(String text) {
        int[] positions = createHashes(text, hashFunctionNumber);
        for (int i : positions) {
            int position = Math.abs(i % bitSetSize);
            if (!bitSet.get(position)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return 得到当前过滤器的错误率
     */
    public double getFalsePositiveProbability() {
        return Math.pow((1 - Math.exp(-hashFunctionNumber * (double) addedElements / bitSetSize)), hashFunctionNumber);
    }

}