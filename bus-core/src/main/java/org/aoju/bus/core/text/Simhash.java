/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.text;

import org.aoju.bus.core.lang.Murmur;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 局部敏感hash,用于海量文本去重
 * 局部敏感hash定义：假定两个字符串具有一定的相似性,
 * 在hash之后,仍然能保持这种相似性,就称之为局部敏感hash
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public class Simhash {

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
    public Simhash() {
        this(4, 3);
    }

    /**
     * 构造
     *
     * @param fracCount     存储段数
     * @param hammingThresh 汉明距离的衡量标准
     */
    public Simhash(int fracCount, int hammingThresh) {
        this.fracCount = fracCount;
        this.fracBitNum = bitNum / fracCount;
        this.hammingThresh = hammingThresh;
        this.storage = new ArrayList<>(fracCount);
        for (int i = 0; i < fracCount; i++) {
            storage.add(new HashMap<>());
        }
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
