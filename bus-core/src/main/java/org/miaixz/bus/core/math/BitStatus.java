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
package org.miaixz.bus.core.math;

/**
 * 通过位运算表示状态的工具类 参数必须是 `偶数` 且 `大于等于0`
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BitStatus {

    /**
     * 增加状态
     *
     * @param states 原状态
     * @param stat   要添加的状态
     * @return 新的状态值
     */
    public static int add(final int states, final int stat) {
        check(states, stat);
        return states | stat;
    }

    /**
     * 判断是否含有状态
     *
     * @param states 原状态
     * @param stat   要判断的状态
     * @return true：有
     */
    public static boolean has(final int states, final int stat) {
        check(states, stat);
        return (states & stat) == stat;
    }

    /**
     * 删除一个状态
     *
     * @param states 原状态
     * @param stat   要删除的状态
     * @return 新的状态值
     */
    public static int remove(final int states, final int stat) {
        check(states, stat);
        if (has(states, stat)) {
            return states ^ stat;
        }
        return states;
    }

    /**
     * 清空状态就是0
     *
     * @return 0
     */
    public static int clear() {
        return 0;
    }

    /**
     * 检查
     * <ul>
     * <li>必须大于0</li>
     * <li>必须为偶数</li>
     * </ul>
     *
     * @param args 被检查的状态
     */
    private static void check(final int... args) {
        for (final int arg : args) {
            if (arg < 0) {
                // 位运算中 0 在二进制表示中所有位都是 0，它并不能代表任何特定状态
                // 如果 0 被允许作为一个合法状态值，那么在检查状态时（比如通过 has() 方法），它将无法有效区分 0 是代表“无状态”还是代表某个特定状态。
                // 如果允许 0 作为状态，它可能会与清空状态的操作（clear() 方法返回 0）发生混淆，从而导致逻辑上的错误。
                throw new IllegalArgumentException(arg + " 必须大于等于0");
            }
            if ((arg & 1) == 1) {
                // 使用偶数作为状态值，确保每个状态只占用二进制中的一位，从而避免不同状态之间的混淆，确保位运算的准确性。
                throw new IllegalArgumentException(arg + " 不是偶数");
            }
        }
    }

}
