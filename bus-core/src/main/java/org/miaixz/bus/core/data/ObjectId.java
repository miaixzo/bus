/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.data;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MongoDB ID生成策略实现
 * ObjectId由以下几部分组成：
 *
 * <pre>
 * 1. Time 时间戳。
 * 2. Machine 所在主机的唯一标识符，一般是机器主机名的散列值。
 * 3. 随机数
 * 4. INC 自增计数器。确保同一秒内产生objectId的唯一性。
 * </pre>
 * 参考：<a href="https://github.com/mongodb/mongo-java-driver/blob/master/bson/src/main/org/bson/types/ObjectId.java">ObjectId</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ObjectId {

    /**
     * 线程安全的下一个随机数,每次生成自增+1
     */
    private static final AtomicInteger NEXT_INC = new AtomicInteger(RandomKit.randomInt());
    /**
     * 机器信息
     */
    private static final char[] MACHINE_CODE = initMachineCode();

    /**
     * 给定的字符串是否为有效的ObjectId
     *
     * @param s 字符串
     * @return 是否为有效的ObjectId
     */
    public static boolean isValid(String s) {
        if (s == null) {
            return false;
        }
        s = StringKit.removeAll(s, Symbol.MINUS);
        final int len = s.length();
        if (len != 24) {
            return false;
        }

        char c;
        for (int i = 0; i < len; i++) {
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c >= 'a' && c <= 'f') {
                continue;
            }
            if (c >= 'A' && c <= 'F') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * 获取一个objectId的bytes表现形式
     *
     * @return objectId
     */
    public static byte[] nextBytes() {
        return next().getBytes();
    }

    /**
     * 获取一个objectId【没有下划线】。
     *
     * @return objectId
     */
    public static String id() {
        return next();
    }

    /**
     * 获取一个objectId【没有下划线】。
     *
     * @return objectId
     */
    public static String next() {
        final char[] ids = new char[24];
        int epoch = (int) ((System.currentTimeMillis() / 1000));
        // 4位字节 ： 时间戳
        for (int i = 7; i >= 0; i--) {
            ids[i] = Normal.DIGITS_16_LOWER[(epoch & 15)];
            epoch >>>= 4;
        }
        // 4位字节 ： 随机数
        System.arraycopy(MACHINE_CODE, 0, ids, 8, 8);
        // 4位字节： 自增序列。溢出后，相当于从0开始算。
        int seq = NEXT_INC.incrementAndGet();
        for (int i = 23; i >= 16; i--) {
            ids[i] = Normal.DIGITS_16_LOWER[(seq & 15)];
            seq >>>= 4;
        }
        return new String(ids);
    }

    /**
     * 获取一个objectId
     *
     * @param withHyphen 是否包含分隔符
     * @return objectId
     */
    public static String next(final boolean withHyphen) {
        if (!withHyphen) {
            return next();
        }
        final char[] ids = new char[26];
        ids[8] = Symbol.C_MINUS;
        ids[17] = Symbol.C_MINUS;
        int epoch = (int) ((System.currentTimeMillis() / 1000));
        // 4位字节 ： 时间戳
        for (int i = 7; i >= 0; i--) {
            ids[i] = Normal.DIGITS_16_LOWER[(epoch & 15)];
            epoch >>>= 4;
        }
        // 4位字节 ： 随机数
        System.arraycopy(MACHINE_CODE, 0, ids, 9, 8);
        // 4位字节： 自增序列。溢出后，相当于从0开始算。
        int seq = NEXT_INC.incrementAndGet();
        for (int i = 25; i >= 18; i--) {
            ids[i] = Normal.DIGITS_16_LOWER[(seq & 15)];
            seq >>>= 4;
        }
        return new String(ids);
    }

    /**
     * 初始化机器码
     *
     * @return 机器码
     */
    private static char[] initMachineCode() {
        // 机器码 : 4位随机数，8个字节。避免docker容器中生成相同机器码的bug
        final char[] macAndPid = new char[8];
        final Random random = new Random();
        for (int i = 7; i >= 0; i--) {
            macAndPid[i] = Normal.DIGITS_16_LOWER[random.nextInt() & 15];
        }
        return macAndPid;
    }

}
