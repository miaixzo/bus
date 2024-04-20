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
package org.miaixz.bus.core.date;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.util.*;

/**
 * 道历
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Taoist {

    public static final int BIRTH_YEAR = -2697;
    /**
     * 三会日
     */
    public static final String[] SAN_HUI = {"1-7", "7-7", "10-15"};

    /**
     * 三元日
     */
    public static final String[] SAN_YUAN = {"1-15", "7-15", "10-15"};

    /**
     * 五腊日
     */
    public static final String[] WU_LA = {"1-1", "5-5", "7-7", "10-1", "12-8"};

    /**
     * 暗戊
     */
    public static final String[] AN_WU = {"未", "戌", "辰", "寅", "午", "子", "酉", "申", "巳", "亥", "卯", "丑"};

    /**
     * 日期对应的节日
     */
    public static final Map<String, List<Festival>> FESTIVAL = new HashMap<>() {
        private static final long serialVersionUID = 1;

        {
            put("1-1", Collections.nCopies(1, new Festival("天腊之辰", "天腊，此日五帝会于东方九炁青天")));
            put("1-3", Arrays.asList(new Festival("郝真人圣诞"), new Festival("孙真人圣诞")));
            put("1-5", Collections.nCopies(1, new Festival("孙祖清静元君诞")));
            put("1-7", Collections.nCopies(1, new Festival("举迁赏会", "此日上元赐福，天官同地水二官考校罪福")));
            put("1-9", Collections.nCopies(1, new Festival("玉皇上帝圣诞")));
            put("1-13", Collections.nCopies(1, new Festival("关圣帝君飞升")));
            put("1-15", Arrays.asList(new Festival("上元天官圣诞"), new Festival("老祖天师圣诞")));
            put("1-19", Collections.nCopies(1, new Festival("长春邱真人(邱处机)圣诞")));
            put("1-28", Collections.nCopies(1, new Festival("许真君(许逊天师)圣诞")));
            put("2-1", Arrays.asList(new Festival("勾陈天皇大帝圣诞"), new Festival("长春刘真人(刘渊然)圣诞")));
            put("2-2", Arrays.asList(new Festival("土地正神诞"), new Festival("姜太公圣诞")));
            put("2-3", Collections.nCopies(1, new Festival("文昌梓潼帝君圣诞")));
            put("2-6", Collections.nCopies(1, new Festival("东华帝君圣诞")));
            put("2-13", Collections.nCopies(1, new Festival("度人无量葛真君圣诞")));
            put("2-15", Collections.nCopies(1, new Festival("太清道德天尊(太上老君)圣诞")));
            put("2-19", Collections.nCopies(1, new Festival("慈航真人圣诞")));
            put("3-1", Collections.nCopies(1, new Festival("谭祖(谭处端)长真真人圣诞")));
            put("3-3", Collections.nCopies(1, new Festival("玄天上帝圣诞")));
            put("3-6", Collections.nCopies(1, new Festival("眼光娘娘圣诞")));
            put("3-15", Arrays.asList(new Festival("天师张大真人圣诞"), new Festival("财神赵公元帅圣诞")));
            put("3-16", Arrays.asList(new Festival("三茅真君得道之辰"), new Festival("中岳大帝圣诞")));
            put("3-18", Arrays.asList(new Festival("王祖(王处一)玉阳真人圣诞"), new Festival("后土娘娘圣诞")));
            put("3-19", Collections.nCopies(1, new Festival("太阳星君圣诞")));
            put("3-20", Collections.nCopies(1, new Festival("子孙娘娘圣诞")));
            put("3-23", Collections.nCopies(1, new Festival("天后妈祖圣诞")));
            put("3-26", Collections.nCopies(1, new Festival("鬼谷先师诞")));
            put("3-28", Collections.nCopies(1, new Festival("东岳大帝圣诞")));
            put("4-1", Collections.nCopies(1, new Festival("长生谭真君成道之辰")));
            put("4-10", Collections.nCopies(1, new Festival("何仙姑圣诞")));
            put("4-14", Collections.nCopies(1, new Festival("吕祖纯阳祖师圣诞")));
            put("4-15", Collections.nCopies(1, new Festival("钟离祖师圣诞")));
            put("4-18", Arrays.asList(new Festival("北极紫微大帝圣诞"), new Festival("泰山圣母碧霞元君诞"), new Festival("华佗神医先师诞")));
            put("4-20", Collections.nCopies(1, new Festival("眼光圣母娘娘诞")));
            put("4-28", Collections.nCopies(1, new Festival("神农先帝诞")));
            put("5-1", Collections.nCopies(1, new Festival("南极长生大帝圣诞")));
            put("5-5", Arrays.asList(new Festival("地腊之辰", "地腊，此日五帝会于南方三炁丹天"), new Festival("南方雷祖圣诞"), new Festival("地祗温元帅圣诞"), new Festival("雷霆邓天君圣诞")));
            put("5-11", Collections.nCopies(1, new Festival("城隍爷圣诞")));
            put("5-13", Arrays.asList(new Festival("关圣帝君降神"), new Festival("关平太子圣诞")));
            put("5-18", Collections.nCopies(1, new Festival("张天师圣诞")));
            put("5-20", Collections.nCopies(1, new Festival("马祖丹阳真人圣诞")));
            put("5-29", Collections.nCopies(1, new Festival("紫青白祖师圣诞")));
            put("6-1", Collections.nCopies(1, new Festival("南斗星君下降")));
            put("6-2", Collections.nCopies(1, new Festival("南斗星君下降")));
            put("6-3", Collections.nCopies(1, new Festival("南斗星君下降")));
            put("6-4", Collections.nCopies(1, new Festival("南斗星君下降")));
            put("6-5", Collections.nCopies(1, new Festival("南斗星君下降")));
            put("6-6", Collections.nCopies(1, new Festival("南斗星君下降")));
            put("6-10", Collections.nCopies(1, new Festival("刘海蟾祖师圣诞")));
            put("6-15", Collections.nCopies(1, new Festival("灵官王天君圣诞")));
            put("6-19", Collections.nCopies(1, new Festival("慈航(观音)成道日")));
            put("6-23", Collections.nCopies(1, new Festival("火神圣诞")));
            put("6-24", Arrays.asList(new Festival("南极大帝中方雷祖圣诞"), new Festival("关圣帝君圣诞")));
            put("6-26", Collections.nCopies(1, new Festival("二郎真君圣诞")));
            put("7-7", Arrays.asList(new Festival("道德腊之辰", "道德腊，此日五帝会于西方七炁素天"), new Festival("庆生中会", "此日中元赦罪，地官同天水二官考校罪福")));
            put("7-12", Collections.nCopies(1, new Festival("西方雷祖圣诞")));
            put("7-15", Collections.nCopies(1, new Festival("中元地官大帝圣诞")));
            put("7-18", Collections.nCopies(1, new Festival("王母娘娘圣诞")));
            put("7-20", Collections.nCopies(1, new Festival("刘祖(刘处玄)长生真人圣诞")));
            put("7-22", Collections.nCopies(1, new Festival("财帛星君文财神增福相公李诡祖圣诞")));
            put("7-26", Collections.nCopies(1, new Festival("张三丰祖师圣诞")));
            put("8-1", Collections.nCopies(1, new Festival("许真君飞升日")));
            put("8-3", Collections.nCopies(1, new Festival("九天司命灶君诞")));
            put("8-5", Collections.nCopies(1, new Festival("北方雷祖圣诞")));
            put("8-10", Collections.nCopies(1, new Festival("北岳大帝诞辰")));
            put("8-15", Collections.nCopies(1, new Festival("太阴星君诞")));
            put("9-1", Collections.nCopies(1, new Festival("北斗九皇降世之辰")));
            put("9-2", Collections.nCopies(1, new Festival("北斗九皇降世之辰")));
            put("9-3", Collections.nCopies(1, new Festival("北斗九皇降世之辰")));
            put("9-4", Collections.nCopies(1, new Festival("北斗九皇降世之辰")));
            put("9-5", Collections.nCopies(1, new Festival("北斗九皇降世之辰")));
            put("9-6", Collections.nCopies(1, new Festival("北斗九皇降世之辰")));
            put("9-7", Collections.nCopies(1, new Festival("北斗九皇降世之辰")));
            put("9-8", Collections.nCopies(1, new Festival("北斗九皇降世之辰")));
            put("9-9", Arrays.asList(new Festival("北斗九皇降世之辰"), new Festival("斗姥元君圣诞"), new Festival("重阳帝君圣诞"), new Festival("玄天上帝飞升"), new Festival("酆都大帝圣诞")));
            put("9-22", Collections.nCopies(1, new Festival("增福财神诞")));
            put("9-23", Collections.nCopies(1, new Festival("萨翁真君圣诞")));
            put("9-28", Collections.nCopies(1, new Festival("五显灵官马元帅圣诞")));
            put("10-1", Arrays.asList(new Festival("民岁腊之辰", "民岁腊，此日五帝会于北方五炁黑天"), new Festival("东皇大帝圣诞")));
            put("10-3", Collections.nCopies(1, new Festival("三茅应化真君圣诞")));
            put("10-6", Collections.nCopies(1, new Festival("天曹诸司五岳五帝圣诞")));
            put("10-15", Arrays.asList(new Festival("下元水官大帝圣诞"), new Festival("建生大会", "此日下元解厄，水官同天地二官考校罪福")));
            put("10-18", Collections.nCopies(1, new Festival("地母娘娘圣诞")));
            put("10-19", Collections.nCopies(1, new Festival("长春邱真君飞升")));
            put("10-20", Collections.nCopies(1, new Festival("虚靖天师(即三十代天师弘悟张真人)诞")));
            put("11-6", Collections.nCopies(1, new Festival("西岳大帝圣诞")));
            put("11-9", Collections.nCopies(1, new Festival("湘子韩祖圣诞")));
            put("11-11", Collections.nCopies(1, new Festival("太乙救苦天尊圣诞")));
            put("11-26", Collections.nCopies(1, new Festival("北方五道圣诞")));
            put("12-8", Collections.nCopies(1, new Festival("王侯腊之辰", "王侯腊，此日五帝会于上方玄都玉京")));
            put("12-16", Arrays.asList(new Festival("南岳大帝圣诞"), new Festival("福德正神诞")));
            put("12-20", Collections.nCopies(1, new Festival("鲁班先师圣诞")));
            put("12-21", Collections.nCopies(1, new Festival("天猷上帝圣诞")));
            put("12-22", Collections.nCopies(1, new Festival("重阳祖师圣诞")));
            put("12-23", Collections.nCopies(1, new Festival("祭灶王", "最适宜谢旧年太岁，开启拜新年太岁")));
            put("12-25", Arrays.asList(new Festival("玉帝巡天"), new Festival("天神下降")));
            put("12-29", Collections.nCopies(1, new Festival("清静孙真君(孙不二)成道")));
        }
    };

    /**
     * 八会日
     */
    public static final Map<String, String> BA_HUI = new HashMap<>() {

        private static final long serialVersionUID = 1;

        {
            put("丙午", "天会");
            put("壬午", "地会");
            put("壬子", "人会");
            put("庚午", "日会");
            put("庚申", "月会");
            put("辛酉", "星辰会");
            put("甲辰", "五行会");
            put("甲戌", "四时会");
        }
    };

    /**
     * 八节日
     */
    public static final Map<String, String> BA_JIE = new HashMap<>() {

        private static final long serialVersionUID = 1;

        {
            put("立春", "东北方度仙上圣天尊同梵炁始青天君下降");
            put("春分", "东方玉宝星上天尊同青帝九炁天君下降");
            put("立夏", "东南方好生度命天尊同梵炁始丹天君下降");
            put("夏至", "南方玄真万福天尊同赤帝三炁天君下降");
            put("立秋", "西南方太灵虚皇天尊同梵炁始素天君下降");
            put("秋分", "西方太妙至极天尊同白帝七炁天君下降");
            put("立冬", "西北方无量太华天尊同梵炁始玄天君下降");
            put("冬至", "北方玄上玉宸天尊同黑帝五炁天君下降");
        }
    };

    /**
     * 阴历
     */
    private final Lunar lunar;

    public Taoist(Lunar lunar) {
        this.lunar = lunar;
    }

    public static Taoist from(Lunar lunar) {
        return new Taoist(lunar);
    }

    public static Taoist from(int year, int month, int day, int hour, int minute, int second) {
        return from(Lunar.from(year + BIRTH_YEAR, month, day, hour, minute, second));
    }

    public static Taoist fromYmd(int year, int month, int day) {
        return from(year, month, day, 0, 0, 0);
    }

    public Lunar getLunar() {
        return lunar;
    }

    public int getYear() {
        return lunar.getYear() - BIRTH_YEAR;
    }

    public int getMonth() {
        return lunar.getMonth();
    }

    public int getDay() {
        return lunar.getDay();
    }

    public String getYearInChinese() {
        String y = getYear() + Normal.EMPTY;
        StringBuilder s = new StringBuilder();
        for (int i = 0, j = y.length(); i < j; i++) {
            s.append(Fields.CN_NUMBER[y.charAt(i) - Symbol.C_ZERO]);
        }
        return s.toString();
    }

    public String getMonthInChinese() {
        return lunar.getMonthInChinese();
    }

    public String getDayInChinese() {
        return lunar.getDayInChinese();
    }

    public List<Festival> getFestivals() {
        List<Festival> l = new ArrayList<>();
        List<Festival> fs = FESTIVAL.get(getMonth() + Symbol.MINUS + getDay());
        if (null != fs) {
            l.addAll(fs);
        }
        String jq = lunar.getSolarTerm();
        if ("冬至".equals(jq)) {
            l.add(new Festival("元始天尊圣诞"));
        } else if ("夏至".equals(jq)) {
            l.add(new Festival("灵宝天尊圣诞"));
        }
        // 八节日
        String f = BA_JIE.get(jq);
        if (null != f) {
            l.add(new Festival(f));
        }
        // 八会日
        f = BA_HUI.get(lunar.getDayInGanZhi());
        if (null != f) {
            l.add(new Festival(f));
        }
        return l;
    }

    private boolean isDayIn(String[] days) {
        String md = getMonth() + Symbol.MINUS + getDay();
        for (String d : days) {
            if (md.equals(d)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否三会日
     *
     * @return true/false
     */
    public boolean isDaySanHui() {
        return isDayIn(SAN_HUI);
    }

    /**
     * 是否三元日
     *
     * @return true/false
     */
    public boolean isDaySanYuan() {
        return isDayIn(SAN_YUAN);
    }

    /**
     * 是否八节日
     *
     * @return true/false
     */
    public boolean isDayBaJie() {
        return BA_JIE.containsKey(lunar.getSolarTerm());
    }

    /**
     * 是否五腊日
     *
     * @return true/false
     */
    public boolean isDayWuLa() {
        return isDayIn(WU_LA);
    }

    /**
     * 是否八会日
     *
     * @return true/false
     */
    public boolean isDayBaHui() {
        return BA_HUI.containsKey(lunar.getDayInGanZhi());
    }

    /**
     * 是否明戊日
     *
     * @return true/false
     */
    public boolean isDayMingWu() {
        return "戊".equals(lunar.getDayGan());
    }

    /**
     * 是否暗戊日
     *
     * @return true/false
     */
    public boolean isDayAnWu() {
        return lunar.getDayZhi().equals(AN_WU[Math.abs(getMonth()) - 1]);
    }

    /**
     * 是否戊日
     *
     * @return true/false
     */
    public boolean isDayWu() {
        return isDayMingWu() || isDayAnWu();
    }

    /**
     * 是否天赦日
     *
     * @return true/false
     */
    public boolean isDayTianShe() {
        boolean ret = false;
        String mz = lunar.getMonthZhi();
        String dgz = lunar.getDayInGanZhi();
        if ("寅卯辰".contains(mz)) {
            if ("戊寅".equals(dgz)) {
                ret = true;
            }
        } else if ("巳午未".contains(mz)) {
            if ("甲午".equals(dgz)) {
                ret = true;
            }
        } else if ("申酉戌".contains(mz)) {
            if ("戊申".equals(dgz)) {
                ret = true;
            }
        } else if ("亥子丑".contains(mz)) {
            if ("甲子".equals(dgz)) {
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return String.format("%s年%s月%s", getYearInChinese(), getMonthInChinese(), getDayInChinese());
    }

    public String toFullString() {
        return String.format("道歷%s年，天運%s年，%s月，%s日。%s月%s日，%s時。", getYearInChinese(), lunar.getYearInGanZhi(), lunar.getMonthInGanZhi(), lunar.getDayInGanZhi(), getMonthInChinese(), getDayInChinese(), lunar.getTimeZhi());
    }

    /**
     * 道历节日
     */
    public static class Festival {

        /**
         * 名称
         */
        private final String name;

        /**
         * 备注
         */
        private final String remark;

        public Festival(String name, String remark) {
            this.name = name;
            this.remark = null == remark ? Normal.EMPTY : remark;
        }

        public Festival(String name) {
            this(name, null);
        }

        public String getName() {
            return name;
        }

        public String getRemark() {
            return remark;
        }

        @Override
        public String toString() {
            return name;
        }

        public String toFullString() {
            StringBuilder s = new StringBuilder();
            s.append(name);
            if (null != remark && remark.length() > 0) {
                s.append("[");
                s.append(remark);
                s.append("]");
            }
            return s.toString();
        }

    }

}
