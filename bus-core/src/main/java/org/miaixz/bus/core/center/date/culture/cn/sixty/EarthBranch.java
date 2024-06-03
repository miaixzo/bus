/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
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
package org.miaixz.bus.core.center.date.culture.cn.sixty;

import org.miaixz.bus.core.center.date.Between;
import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.Element;
import org.miaixz.bus.core.center.date.culture.cn.Zodiac;
import org.miaixz.bus.core.center.date.culture.cn.minor.PengZuEarthBranch;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 地支
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EarthBranch extends Samsara {

    /**
     * 古今十二时辰与时间对照
     * 子时（23时至01时）：夜半时分，也是一天的开始。
     * 丑时（01时至03时）：鸡鸣时分，意味着新的一天开始。
     * 寅时（03时至05时）：平旦，夜与日的交替之际，太阳即将初升。
     * 卯时（05时至07时）：日出时分，太阳刚刚露脸，冉冉初升。
     * 辰时（07时至09时）：食时，古人吃早饭的时间。
     * 巳时（09时至11时）：隅中，临近中午的时候。
     * 午时（11时至13时）：日中，太阳最猛烈的时候。
     * 未时（13时至15时）：日昳，太阳偏西为日跌。
     * 申时（15时至17时）：哺时，也被称为日铺、夕食等。
     * 酉时（17时至19时）：日入时分，太阳落山，天地昏黄，万物朦胧。
     * 戌时（19时至21时）：黄昏时分，太阳已经落山，天色逐渐暗淡。
     * 亥时（21时至23时）：人定时分，夜色已深，人们已经停止活动，安歇睡眠了。
     */
    public static final String[] NAMES = {
            "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    /**
     * 时辰对应的小时范围
     */
    private static final Map<String, Integer[]> TIME_MAP = new HashMap<>();
    /**
     * 小时到时辰的映射
     */
    private static final Map<Integer, String> HOUR_MAP = new HashMap<>();

    static {
        int hour = 23;
        for (final String c : NAMES) {
            // 初始化时辰对应的小时范围
            TIME_MAP.put(c, new Integer[]{hour % 24, (hour + 2) % 24});

            // 初始化小时到时辰的映射
            HOUR_MAP.put(hour % 24, c + "时");
            HOUR_MAP.put((hour + 1) % 24, c + "时");

            hour += 2;
        }
    }

    public EarthBranch(int index) {
        super(NAMES, index);
    }

    public EarthBranch(String name) {
        super(NAMES, name);
    }

    public static EarthBranch fromIndex(int index) {
        return new EarthBranch(index);
    }

    public static EarthBranch fromName(String name) {
        return new EarthBranch(name);
    }

    /**
     * 将时辰描述转换为现代时间段。
     *
     * @param chrono 时辰描述，可以是“子时”。
     * @return {@link Between} 对象，表示起始和结束时间。
     * @throws IllegalArgumentException 如果输入的时辰描述无效。
     */
    public static Between of(final String chrono) {
        if (StringKit.isEmpty(chrono)) {
            throw new IllegalArgumentException("Invalid args for : " + chrono);
        }
        final String baseTime = chrono.replace("时", Normal.EMPTY);
        final Integer[] hours = TIME_MAP.get(baseTime);
        if (hours == null) {
            throw new IllegalArgumentException("Invalid time");
        }
        final Integer startHour = hours[0];
        final Integer endHour = hours[1];


        final LocalDateTime start = LocalDateTime.now().withHour(startHour).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime end = (startHour > endHour) ? start.plusDays(1).withHour(endHour) : start.withHour(endHour);

        final Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
        final Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());

        return Between.of(startDate, endDate);
    }

    /**
     * 根据给定的小时数转换为对应的时辰描述。
     *
     * @param hour 小时数，应在0到23之间。
     * @return 时辰描述，如果小时数无效，则返回“未知”。
     */
    public static String get(final int hour) {
        return HOUR_MAP.getOrDefault(hour, "未知");
    }

    public EarthBranch next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 五行
     *
     * @return 五行
     */
    public Element getElement() {
        return Element.fromIndex(new int[]{4, 2, 0, 0, 2, 1, 1, 2, 3, 3, 2, 4}[index]);
    }

    /**
     * 藏干之本气
     *
     * @return 天干
     */
    public HeavenStem getHideHeavenStemMain() {
        return HeavenStem.fromIndex(new int[]{9, 5, 0, 1, 4, 2, 3, 5, 6, 7, 4, 8}[index]);
    }

    /**
     * 藏干之中气，无中气返回null
     *
     * @return 天干
     */
    public HeavenStem getHideHeavenStemMiddle() {
        int n = new int[]{-1, 9, 2, -1, 1, 6, 5, 3, 8, -1, 7, 0}[index];
        return n == -1 ? null : HeavenStem.fromIndex(n);
    }

    /**
     * 藏干之余气，无余气返回null
     *
     * @return 天干
     */
    public HeavenStem getHideHeavenStemResidual() {
        int n = new int[]{-1, 7, 4, -1, 9, 4, -1, 1, 4, -1, 3, -1}[index];
        return n == -1 ? null : HeavenStem.fromIndex(n);
    }

    /**
     * 生肖
     *
     * @return 生肖
     */
    public Zodiac getZodiac() {
        return Zodiac.fromIndex(index);
    }

    /**
     * 方位
     *
     * @return 方位
     */
    public Direction getDirection() {
        return Direction.fromIndex(new int[]{0, 4, 2, 2, 4, 8, 8, 4, 6, 6, 4, 0}[index]);
    }

    /**
     * 相冲的地支（子午冲，丑未冲，寅申冲，辰戌冲，卯酉冲，巳亥冲）
     *
     * @return 地支
     */
    public EarthBranch next() {
        return next(6);
    }

    /**
     * 煞（逢巳日、酉日、丑日必煞东；亥日、卯日、未日必煞西；申日、子日、辰日必煞南；寅日、午日、戌日必煞北。）
     *
     * @return 方位
     */
    public Direction getOminous() {
        return Direction.fromIndex(new int[]{8, 2, 0, 6}[index % 4]);
    }

    /**
     * 地支彭祖百忌
     *
     * @return 地支彭祖百忌
     */
    public PengZuEarthBranch getPengZuEarthBranch() {
        return PengZuEarthBranch.fromIndex(index);
    }

}
