/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.sensitive.provider;

import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.sensitive.Builder;

/**
 * 脱敏策略
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements StrategyProvider {

    /**
     * 自动模式
     *
     * @param mode   脱敏模型
     * @param rawVal 源字符
     * @param shadow 遮挡字符
     * @return the string
     */
    public static String build(Builder.Mode mode,
                               String rawVal,
                               String shadow) {
        StringBuilder resultBuilder = new StringBuilder();
        int length = rawVal.length();
        if (mode == Builder.Mode.TAIL || mode == Builder.Mode.HEAD) {
            // 以1/2作为遮挡范围
            int half = (int) Math.ceil(length / 2.0);
            boolean head = mode == Builder.Mode.HEAD;
            if (head) {
                resultBuilder.append(StringKit.repeat(shadow, half))
                        .append(rawVal, half, length);
            } else {
                resultBuilder.append(rawVal, 0, length - half)
                        .append(StringKit.repeat(shadow, half));
            }
            return resultBuilder.toString();
        }
        // 仅有两个字符,不能采用遮挡中间的做法
        if (length == 2) {
            return resultBuilder.append(rawVal, 0, 1)
                    .append(shadow).toString();
        }
        // 以一半字符被mask作为目标
        int middle = Math.max((int) Math.ceil(length / 2.0), 1);
        // 计算首尾字符长度
        int side = Math.max((int) Math.floor((length - middle) / 2.0), 1);
        // 修正中间被mask的长度
        middle = length - side * 2;
        resultBuilder.append(rawVal, 0, side)
                .append(StringKit.repeat(shadow, middle))
                .append(rawVal, side + middle, length);
        return resultBuilder.toString();
    }


    /**
     * 手动模式
     *
     * @param mode            脱敏模型
     * @param fixedHeaderSize 固定头部长度
     * @param fixedTailorSize 固定尾部长度
     * @param rawVal          源字符
     * @param shadow          遮挡字符
     * @return the string
     */
    public static String build(Builder.Mode mode,
                               int fixedHeaderSize,
                               int fixedTailorSize,
                               String rawVal,
                               String shadow) {
        StringBuilder resultBuilder = new StringBuilder();
        int length = rawVal.length();
        int maskLength;
        switch (mode) {
            case TAIL:
                if (length <= fixedHeaderSize) {
                    return rawVal;
                }
                maskLength = length - fixedHeaderSize;
                resultBuilder.append(rawVal, 0, fixedHeaderSize)
                        .append(StringKit.repeat(shadow, maskLength));
                break;
            default:
            case HEAD:
                if (length <= fixedTailorSize) {
                    return rawVal;
                }
                maskLength = length - fixedTailorSize;
                resultBuilder.append(StringKit.repeat(shadow, maskLength))
                        .append(rawVal.substring(maskLength));
                break;
            case MIDDLE:
                int unmaskLength = fixedTailorSize + fixedHeaderSize;
                if (length <= unmaskLength) {
                    return rawVal;
                }
                maskLength = length - unmaskLength;
                resultBuilder.append(rawVal, 0, fixedHeaderSize)
                        .append(StringKit.repeat(shadow, maskLength))
                        .append(rawVal, fixedHeaderSize + maskLength, length);
                break;
        }
        return resultBuilder.toString();
    }

}
