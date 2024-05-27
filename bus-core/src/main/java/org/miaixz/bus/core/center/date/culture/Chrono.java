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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.Between;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 时辰转换器，支持宋以后的二十四时辰制度。
 * <p>本转换器提供以下功能：
 * <ul>
 * <li>处理包含“时”、“初”或“正”后缀的时辰描述，并自动返回相应的现代时间段。
 * “初”和“正”分别对应每个时辰的前半段和后半段，而不带后缀的“时”描述则涵盖该时辰的完整时间段。</li>
 * <li>根据小时数转换为相应的时辰描述，通过{@code isAbs}参数控制是否包含“初”或“正”。</li>
 * </ul>
 * <p>
 * 异常情况：
 * <ul>
 * <li>如果输入的时辰描述无效或不被识别，{@code toModernTime} 方法将抛出 {@code IllegalArgumentException}。</li>
 * <li>同样，如果{@code toShiChen}方法接收到无效的小时数，将返回“未知”。</li>
 * </ul>
 * 示例：
 * <ul>
 * <li>{@code toModernTime("子时")} 返回的时间段从23点开始到1点结束。</li>
 * <li>{@code toModernTime("子初")} 返回的时间段从23点开始到0点结束。</li>
 * <li>{@code toModernTime("子正")} 返回的时间段从0点开始到1点结束。</li>
 * <li>{@code toShiChen(0, false)} 返回“子正”。</li>
 * <li>{@code toShiChen(0, true)} 返回“子时”。</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Chrono {

    /**
     * 子时：23时至01时
     */
    C2301("2301", "子", "夜半时分,也是一天的开始。"),
    /**
     * 丑时：01时至03时
     */
    _0103("0103", "丑", "鸡鸣之时,意味着新的一天开始了。"),
    /**
     * 寅时：03时至05时
     */
    _0305("0305", "寅", "新的开始,被视为“旭日东升”的时刻。"),
    /**
     * 卯时：05时至07时
     */
    _0507("0507", "卯", "日出时分，太阳刚刚露脸，冉冉初升。"),
    /**
     * 辰时：07时至09时
     */
    _0709("0709", "辰", "食时，古人吃早饭的时间。"),
    /**
     * 巳时：09时至11时
     */
    _0911("0911", "巳", "隅中，临近中午的时候。"),
    /**
     * 午时：11时至13时
     */
    _1113("1113", "午", "日中，太阳最猛烈的时候。"),

    /**
     * 未时：13时至15时
     */
    _1315("1315", "未", "日昳，太阳偏西为日跌。"),

    /**
     * 申时：15时至17时
     */
    _1517("1517", "申", "哺时，也被称为日铺、夕食等。"),

    /**
     * 酉时：17时至19时
     */
    _1719("1719", "酉", "日入时分，太阳落山，天地昏黄，万物朦胧。"),

    /**
     * 戌时：19时至21时
     */
    _1921("1921", "戌", "黄昏时分，太阳已经落山，天色逐渐暗淡。"),

    /**
     * 亥时：21时至23时
     */
    _2123("2123", "亥", "人定时分，夜色已深，人们已经停止活动，安歇睡眠了。");

    private static final Chrono[] ENUMS = Chrono.values();
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
        for (final Chrono c : ENUMS) {
            // 初始化时辰对应的小时范围
            TIME_MAP.put(c.name, new Integer[]{hour % 24, (hour + 2) % 24});

            // 初始化小时到时辰的映射
            HOUR_MAP.put(hour % 24, c.name + "时");
            HOUR_MAP.put((hour + 1) % 24, c.name + "时");

            hour += 2;
        }
    }

    /**
     * 编码
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;
    /**
     * 描述
     */
    private String desc;
    Chrono(String code, String name) {
        this.code = code;
        this.name = name;
    }


    Chrono(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    /**
     * 将时辰描述转换为现代时间段。
     *
     * @param chrono 时辰描述，可以是“子时”。
     * @return {@link Between} 对象，表示起始和结束时间。
     * @throws IllegalArgumentException 如果输入的时辰描述无效。
     */
    public static Between toTime(final String chrono) {
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
    public static String toChrono(final int hour) {
        return HOUR_MAP.getOrDefault(hour, "未知");
    }

}
