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
package org.miaixz.bus.core.key;

import java.util.Date;

/**
 * Long类型ID生成器,固定为19位长度
 * 根据当前毫秒数和可选的服务器/实例ID生成唯一的数字ID
 * 适合用作分布式应用程序中的数据库主键
 * <p>
 * 示例:
 * </p>
 *
 * <ul>
 *   <li>LongId lid = new LongId()    - 创建一个实例来生成带有服务器为0的ID
 *   <li>LongId lid = new LongId(123) - 创建一个实例来生成带有服务器为123的ID
 *   <li>lid.id() - 使用实例上的服务器ID生成ID
 * </ul>
 *
 * <p>
 *    有了生成的id,您就可以用它检索信息,无论是否有实例
 * <ul>
 *   <li>LongId.getDate(id) - 从ID中提取时间戳
 *   <li>LongId.getServerId(id) - 从ID中提取服务器ID
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LongID {

    /**
     * 对于ID的每个组件,我们使用了多少个十六进制数字
     */
    private static final int COUNTER_HEX_DIGITS = 2;
    private static final int SERVER_HEX_DIGITS = 3;

    private static final int COUNTER_MAX = 255;
    private static final int SERVER_MAX = 4095;
    /**
     * 填充信息
     */
    private static final String ZEROES = "00000000000";
    /**
     * 可选的服务器ID为0 可以通过创建 new LongID(serverId)来设置
     */
    private final String serverIdAsHex;
    /**
     * 状态变量,用于在同一毫秒内调用时确保惟一的id
     * 跨实例共享,因此即使您使用相同的服务器创建两个对象,id仍然是惟一的
     */
    private long millisPrevious = 0;
    private long counterWithinThisMilli = 0;

    /**
     * 使用默认的serverId创建一个新实例: 0
     */
    public LongID() {
        this(0);
    }

    /**
     * 为特定的服务器/应用程序实例创建一个新实例 确保每个实例生成一组惟一的id
     *
     * @param serverId 服务器ID
     */
    public LongID(long serverId) {
        if (serverId > SERVER_MAX || serverId < 0)
            throw new IllegalArgumentException("Server Id must be in the range 0-" + SERVER_MAX);

        // 将serverId转换为十六进制,根据需要填充0 .
        String asHex = Long.toHexString(serverId);
        serverIdAsHex = ZEROES.substring(0, SERVER_HEX_DIGITS - asHex.length()) + asHex;
    }

    /**
     * 获取生成LongId的日期/时间
     *
     * @param longId 用这个类生成的LongId的数字id
     * @return Date对象, 表示生成LongId的时间.
     */
    public static Date getDate(long longId) {
        // 转换为十六进制,然后去掉最后6个十六进制数字 其余的将是时间戳
        String hexInput = Long.toHexString(longId);
        if (hexInput.length() < COUNTER_HEX_DIGITS + SERVER_HEX_DIGITS + 1)
            throw new IllegalArgumentException("Input is too short to be a LongId");
        return new Date(Long.decode("0x" + hexInput.substring(0, hexInput.length() - (COUNTER_HEX_DIGITS + SERVER_HEX_DIGITS))));
    }

    /**
     * 获取生成LongId的服务器/实例ID.
     *
     * @param longId 用这个类生成的LongId的数字id
     * @return 生成LongId的服务器/实例的数字ID
     */
    public static long getServerId(long longId) {
        // 将数字转换为十六进制 以最后几个十六进制数字为例 把它们转换成数字 这是服务器Id.
        String hexInput = Long.toHexString(longId);
        if (hexInput.length() < COUNTER_HEX_DIGITS + SERVER_HEX_DIGITS + 1)
            throw new IllegalArgumentException("Input is too short to be a LongId");
        return Long.decode("0x" + hexInput.substring(hexInput.length() - SERVER_HEX_DIGITS));
    }

    /**
     * 从一个长longid获取相同毫秒的计数器 除了调试之外没有什么用处.
     *
     * @param longId 用这个类生成的LongId的数字id
     * @return 数字计数器
     */
    public static long getCounter(long longId) {
        // 转换为十六进制,删除服务器数字,然后查看剩下的最后几个十六进制数字
        String hexInput = Long.toHexString(longId);
        if (hexInput.length() < COUNTER_HEX_DIGITS + SERVER_HEX_DIGITS + 1)
            throw new IllegalArgumentException("Input is too short to be a LongId");
        return Long.decode("0x" + hexInput.substring(hexInput.length() - (COUNTER_HEX_DIGITS + SERVER_HEX_DIGITS), hexInput.length() - SERVER_HEX_DIGITS));
    }

    /**
     * 生成一个新的ID. Synchronized,这样每个线程将等待前一个线程完成,允许我们
     * 当两个线程在同一毫秒内碰撞时,维护状态并保证唯一的ID
     * 如果我们在一毫秒内达到计数器限制,我们将睡眠一毫秒并重新开始
     *
     * @return ID 适合用作数据库键的唯一标识
     */
    public synchronized long id() {
        // 当前时间戳
        long millisCurrent = System.currentTimeMillis();

        // 如果不在相同的毫秒中,则重置静态变量(safe since Synchronized)
        if (millisPrevious != millisCurrent) {
            millisPrevious = millisCurrent;
            counterWithinThisMilli = 0;
        }
        // 如果计数器溢出,休眠1ms,然后递归调用
        else if (counterWithinThisMilli >= COUNTER_MAX) {
            try {
                Thread.sleep(1);
            }
            // sleep抛出一个已检查的异常,因此我们必须在这里处理它,否则就把它踢到楼上,迫使调用者处理它
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return id();
        }
        //如果在相同的毫秒中,增量计数器
        else counterWithinThisMilli++;

        millisPrevious = millisCurrent;

        // 转换毫秒为十六进制,不需要填充.
        String millisAsHex = Long.toHexString(millisCurrent);

        // 转换计数器十六进制,需要填充0.
        String counterAsHex = Long.toHexString(counterWithinThisMilli);
        counterAsHex = ZEROES.substring(0, COUNTER_HEX_DIGITS - counterAsHex.length()) + counterAsHex;

        return Long.decode("0x" + millisAsHex + counterAsHex + serverIdAsHex);
    }

}
