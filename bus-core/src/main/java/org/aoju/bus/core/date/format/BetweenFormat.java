/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.core.date.format;

import org.aoju.bus.core.consts.Fields;
import org.aoju.bus.core.utils.StringUtils;

/**
 * 时长格式化器
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class BetweenFormat {

    /**
     * 时长毫秒数
     */
    private long betweenMs;
    /**
     * 格式化级别
     */
    private Fields.Level level;
    /**
     * 格式化级别的最大个数
     */
    private int levelMaxCount;

    /**
     * 构造
     *
     * @param betweenMs 日期间隔
     * @param level     级别，按照天、小时、分、秒、毫秒分为5个等级，根据传入等级，格式化到相应级别
     */
    public BetweenFormat(long betweenMs, Fields.Level  level) {
        this(betweenMs, level, 0);
    }

    /**
     * 构造
     *
     * @param betweenMs     日期间隔
     * @param level         级别，按照天、小时、分、秒、毫秒分为5个等级，根据传入等级，格式化到相应级别
     * @param levelMaxCount 格式化级别的最大个数，假如级别个数为1，但是级别到秒，那只显示一个级别
     */
    public BetweenFormat(long betweenMs, Fields.Level level, int levelMaxCount) {
        this.betweenMs = betweenMs;
        this.level = level;
        this.levelMaxCount = levelMaxCount;
    }

    /**
     * 格式化日期间隔输出<br>
     *
     * @return 格式化后的字符串
     */
    public String format() {
        final StringBuilder sb = new StringBuilder();
        if (betweenMs > 0) {
            long day = betweenMs / Fields.Unit.DAY.getMillis();
            long hour = betweenMs / Fields.Unit.HOUR.getMillis() - day * 24;
            long minute = betweenMs / Fields.Unit.MINUTE.getMillis() - day * 24 * 60 - hour * 60;
            long second = betweenMs / Fields.Unit.SECOND.getMillis() - ((day * 24 + hour) * 60 + minute) * 60;
            long millisecond = betweenMs - (((day * 24 + hour) * 60 + minute) * 60 + second) * 1000;

            final int level = this.level.ordinal();
            int levelCount = 0;

            if (isLevelCountValid(levelCount) && 0 != day && level >= Fields.Level.DAY.ordinal()) {
                sb.append(day).append(Fields.Level.DAY.name);
                levelCount++;
            }
            if (isLevelCountValid(levelCount) && 0 != hour && level >= Fields.Level.HOUR.ordinal()) {
                sb.append(hour).append(Fields.Level.HOUR.name);
                levelCount++;
            }
            if (isLevelCountValid(levelCount) && 0 != minute && level >= Fields.Level.MINUTE.ordinal()) {
                sb.append(minute).append(Fields.Level.MINUTE.name);
                levelCount++;
            }
            if (isLevelCountValid(levelCount) && 0 != second && level >= Fields.Level.SECOND.ordinal()) {
                sb.append(second).append(Fields.Level.SECOND.name);
                levelCount++;
            }
            if (isLevelCountValid(levelCount) && 0 != millisecond && level >= Fields.Level.MILLSECOND.ordinal()) {
                sb.append(millisecond).append(Fields.Level.MILLSECOND.name);
                levelCount++;
            }
        }

        if (StringUtils.isEmpty(sb)) {
            sb.append(0).append(this.level.name);
        }

        return sb.toString();
    }

    /**
     * 获得 时长毫秒数
     *
     * @return 时长毫秒数
     */
    public long getBetweenMs() {
        return betweenMs;
    }

    /**
     * 设置 时长毫秒数
     *
     * @param betweenMs 时长毫秒数
     */
    public void setBetweenMs(long betweenMs) {
        this.betweenMs = betweenMs;
    }

    /**
     * 获得 格式化级别
     *
     * @return 格式化级别
     */
    public Fields.Level getLevel() {
        return level;
    }

    /**
     * 设置格式化级别
     *
     * @param level 格式化级别
     */
    public void setLevel(Fields.Level level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return format();
    }

    /**
     * 等级数量是否有效<br>
     * 有效的定义是：levelMaxCount大于0（被设置），当前等级数量没有超过这个最大值
     *
     * @param levelCount 登记数量
     * @return 是否有效
     */
    private boolean isLevelCountValid(int levelCount) {
        return this.levelMaxCount <= 0 || levelCount < this.levelMaxCount;
    }

}
