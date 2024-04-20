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
package org.miaixz.bus.core.date;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;

import java.util.*;

/**
 * 佛历
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Buddhist {

    /**
     * 佛历年
     */
    public static final int DEAD_YEAR = -543;
    /**
     * 观音斋日期
     */
    public static final String[] DAY_ZHAI_GUAN_YIN = {"1-8", "2-7", "2-9", "2-19", "3-3", "3-6", "3-13", "4-22", "5-3", "5-17", "6-16", "6-18", "6-19", "6-23", "7-13", "8-16", "9-19", "9-23", "10-2", "11-19", "11-24", "12-25"};
    /**
     * 27星宿，佛教从印度传入中国，印度把28星宿改为27星宿，把牛宿(牛金牛)纳入了女宿(女土蝠)。
     */
    public static final String[] XIU_27 = {"角", "亢", "氐", "房", "心", "尾", "箕", "斗", "女", "虚", "危", "室", "壁", "奎", "娄", "胃", "昴", "毕", "觜", "参", "井", "鬼", "柳", "星", "张", "翼", "轸"};
    /**
     * 纪念日
     */
    public static final Map<String, List<String>> OTHER_FESTIVAL = new HashMap<>() {
        private static final long serialVersionUID = -1;

        {
            put("1-1", Collections.nCopies(1, "弥勒菩萨圣诞"));
            put("1-6", Collections.nCopies(1, "定光佛圣诞"));
            put("2-8", Collections.nCopies(1, "释迦牟尼佛出家"));
            put("2-15", Collections.nCopies(1, "释迦牟尼佛涅槃"));
            put("2-19", Collections.nCopies(1, "观世音菩萨圣诞"));
            put("2-21", Collections.nCopies(1, "普贤菩萨圣诞"));
            put("3-16", Collections.nCopies(1, "准提菩萨圣诞"));
            put("4-4", Collections.nCopies(1, "文殊菩萨圣诞"));
            put("4-8", Collections.nCopies(1, "释迦牟尼佛圣诞"));
            put("4-15", Collections.nCopies(1, "佛吉祥日"));
            put("4-28", Collections.nCopies(1, "药王菩萨圣诞"));
            put("5-13", Collections.nCopies(1, "伽蓝菩萨圣诞"));
            put("6-3", Collections.nCopies(1, "韦驮菩萨圣诞"));
            put("6-19", Collections.nCopies(1, "观音菩萨成道"));
            put("7-13", Collections.nCopies(1, "大势至菩萨圣诞"));
            put("7-15", Collections.nCopies(1, "佛欢喜日"));
            put("7-24", Collections.nCopies(1, "龙树菩萨圣诞"));
            put("7-30", Collections.nCopies(1, "地藏菩萨圣诞"));
            put("8-15", Collections.nCopies(1, "月光菩萨圣诞"));
            put("8-22", Collections.nCopies(1, "燃灯佛圣诞"));
            put("9-9", Collections.nCopies(1, "摩利支天菩萨圣诞"));
            put("9-19", Collections.nCopies(1, "观世音菩萨出家"));
            put("9-30", Collections.nCopies(1, "药师琉璃光佛圣诞"));
            put("10-5", Collections.nCopies(1, "达摩祖师圣诞"));
            put("10-20", Collections.nCopies(1, "文殊菩萨出家"));
            put("11-17", Collections.nCopies(1, "阿弥陀佛圣诞"));
            put("11-19", Collections.nCopies(1, "日光菩萨圣诞"));
            put("12-8", Collections.nCopies(1, "释迦牟尼佛成道"));
            put("12-23", Collections.nCopies(1, "监斋菩萨圣诞"));
            put("12-29", Collections.nCopies(1, "华严菩萨圣诞"));
        }
    };
    private static final String DJ = "犯者夺纪";
    private static final String JS = "犯者减寿";
    private static final String SS = "犯者损寿";
    private static final String XL = "犯者削禄夺纪";
    private static final String JW = "犯者三年内夫妇俱亡";
    private static final Festival Y = new Festival("杨公忌");
    private static final Festival T = new Festival("四天王巡行", "", true);
    private static final Festival D = new Festival("斗降", DJ, true);
    private static final Festival S = new Festival("月朔", DJ, true);
    private static final Festival W = new Festival("月望", DJ, true);
    private static final Festival H = new Festival("月晦", JS, true);
    private static final Festival L = new Festival("雷斋日", JS, true);
    private static final Festival J = new Festival("九毒日", "犯者夭亡，奇祸不测");
    private static final Festival R = new Festival("人神在阴", "犯者得病", true, "宜先一日即戒");
    private static final Festival M = new Festival("司命奏事", JS, true, "如月小，即戒廿九");
    private static final Festival HH = new Festival("月晦", JS, true, "如月小，即戒廿九");
    /**
     * 因果犯忌
     */
    public static final Map<String, List<Festival>> FESTIVAL = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            put("1-1", Arrays.asList(new Festival("天腊，玉帝校世人神气禄命", XL), S));
            put("1-3", Arrays.asList(new Festival("万神都会", DJ), D));
            put("1-5", Collections.nCopies(1, new Festival("五虚忌")));
            put("1-6", Arrays.asList(new Festival("六耗忌"), L));
            put("1-7", Collections.nCopies(1, new Festival("上会日", SS)));
            put("1-8", Arrays.asList(new Festival("五殿阎罗天子诞", DJ), T));
            put("1-9", Collections.nCopies(1, new Festival("玉皇上帝诞", DJ)));
            put("1-13", Collections.nCopies(1, Y));
            put("1-14", Arrays.asList(new Festival("三元降", JS), T));
            put("1-15", Arrays.asList(new Festival("三元降", JS), new Festival("上元神会", DJ), W, T));
            put("1-16", Collections.nCopies(1, new Festival("三元降", JS)));
            put("1-19", Collections.nCopies(1, new Festival("长春真人诞")));
            put("1-23", Arrays.asList(new Festival("三尸神奏事"), T));
            put("1-25", Arrays.asList(H, new Festival("天地仓开日", "犯者损寿，子带疾")));
            put("1-27", Collections.nCopies(1, D));
            put("1-28", Collections.nCopies(1, R));
            put("1-29", Collections.nCopies(1, T));
            put("1-30", Arrays.asList(HH, M, T));
            put("2-1", Arrays.asList(new Festival("一殿秦广王诞", DJ), S));
            put("2-2", Arrays.asList(new Festival("万神都会", DJ), new Festival("福德土地正神诞", "犯者得祸")));
            put("2-3", Arrays.asList(new Festival("文昌帝君诞", XL), D));
            put("2-6", Arrays.asList(new Festival("东华帝君诞"), L));
            put("2-8", Arrays.asList(new Festival("释迦牟尼佛出家", DJ), new Festival("三殿宋帝王诞", DJ), new Festival("张大帝诞", DJ), T));
            put("2-11", Collections.nCopies(1, Y));
            put("2-14", Collections.nCopies(1, T));
            put("2-15", Arrays.asList(new Festival("释迦牟尼佛涅槃", XL), new Festival("太上老君诞", XL), new Festival("月望", XL, true), T));
            put("2-17", Collections.nCopies(1, new Festival("东方杜将军诞")));
            put("2-18", Arrays.asList(new Festival("四殿五官王诞", XL), new Festival("至圣先师孔子讳辰", XL)));
            put("2-19", Collections.nCopies(1, new Festival("观音大士诞", DJ)));
            put("2-21", Collections.nCopies(1, new Festival("普贤菩萨诞")));
            put("2-23", Collections.nCopies(1, T));
            put("2-25", Collections.nCopies(1, H));
            put("2-27", Collections.nCopies(1, D));
            put("2-28", Collections.nCopies(1, R));
            put("2-29", Collections.nCopies(1, T));
            put("2-30", Arrays.asList(HH, M, T));
            put("3-1", Arrays.asList(new Festival("二殿楚江王诞", DJ), S));
            put("3-3", Arrays.asList(new Festival("玄天上帝诞", DJ), D));
            put("3-6", Collections.nCopies(1, L));
            put("3-8", Arrays.asList(new Festival("六殿卞城王诞", DJ), T));
            put("3-9", Arrays.asList(new Festival("牛鬼神出", "犯者产恶胎"), Y));
            put("3-12", Collections.nCopies(1, new Festival("中央五道诞")));
            put("3-14", Collections.nCopies(1, T));
            put("3-15", Arrays.asList(new Festival("昊天上帝诞", DJ), new Festival("玄坛诞", DJ), W, T));
            put("3-16", Collections.nCopies(1, new Festival("准提菩萨诞", DJ)));
            put("3-19", Arrays.asList(new Festival("中岳大帝诞"), new Festival("后土娘娘诞"), new Festival("三茅降")));
            put("3-20", Arrays.asList(new Festival("天地仓开日", SS), new Festival("子孙娘娘诞")));
            put("3-23", Collections.nCopies(1, T));
            put("3-25", Collections.nCopies(1, H));
            put("3-27", Arrays.asList(new Festival("七殿泰山王诞"), D));
            put("3-28", Arrays.asList(R, new Festival("苍颉至圣先师诞", XL), new Festival("东岳大帝诞")));
            put("3-29", Collections.nCopies(1, T));
            put("3-30", Arrays.asList(HH, M, T));
            put("4-1", Arrays.asList(new Festival("八殿都市王诞", DJ), S));
            put("4-3", Collections.nCopies(1, D));
            put("4-4", Arrays.asList(new Festival("万神善会", "犯者失瘼夭胎"), new Festival("文殊菩萨诞")));
            put("4-6", Collections.nCopies(1, L));
            put("4-7", Arrays.asList(new Festival("南斗、北斗、西斗同降", JS), Y));
            put("4-8", Arrays.asList(new Festival("释迦牟尼佛诞", DJ), new Festival("万神善会", "犯者失瘼夭胎"), new Festival("善恶童子降", "犯者血死"), new Festival("九殿平等王诞"), T));
            put("4-14", Arrays.asList(new Festival("纯阳祖师诞", JS), T));
            put("4-15", Arrays.asList(W, new Festival("钟离祖师诞"), T));
            put("4-16", Collections.nCopies(1, new Festival("天地仓开日", SS)));
            put("4-17", Collections.nCopies(1, new Festival("十殿转轮王诞", DJ)));
            put("4-18", Arrays.asList(new Festival("天地仓开日", SS), new Festival("紫徽大帝诞", SS)));
            put("4-20", Collections.nCopies(1, new Festival("眼光圣母诞")));
            put("4-23", Collections.nCopies(1, T));
            put("4-25", Collections.nCopies(1, H));
            put("4-27", Collections.nCopies(1, D));
            put("4-28", Collections.nCopies(1, R));
            put("4-29", Collections.nCopies(1, T));
            put("4-30", Arrays.asList(HH, M, T));
            put("5-1", Arrays.asList(new Festival("南极长生大帝诞", DJ), S));
            put("5-3", Collections.nCopies(1, D));
            put("5-5", Arrays.asList(new Festival("地腊", XL), new Festival("五帝校定生人官爵", XL), J, Y));
            put("5-6", Arrays.asList(J, L));
            put("5-7", Collections.nCopies(1, J));
            put("5-8", Arrays.asList(new Festival("南方五道诞"), T));
            put("5-11", Arrays.asList(new Festival("天地仓开日", SS), new Festival("天下都城隍诞")));
            put("5-12", Collections.nCopies(1, new Festival("炳灵公诞")));
            put("5-13", Collections.nCopies(1, new Festival("关圣降", XL)));
            put("5-14", Arrays.asList(new Festival("夜子时为天地交泰", JW), T));
            put("5-15", Arrays.asList(W, J, T));
            put("5-16", Arrays.asList(new Festival("九毒日", JW), new Festival("天地元气造化万物之辰", JW)));
            put("5-17", Collections.nCopies(1, J));
            put("5-18", Collections.nCopies(1, new Festival("张天师诞")));
            put("5-22", Collections.nCopies(1, new Festival("孝娥神诞", DJ)));
            put("5-23", Collections.nCopies(1, T));
            put("5-25", Arrays.asList(J, H));
            put("5-26", Collections.nCopies(1, J));
            put("5-27", Arrays.asList(J, D));
            put("5-28", Collections.nCopies(1, R));
            put("5-29", Collections.nCopies(1, T));
            put("5-30", Arrays.asList(HH, M, T));
            put("6-1", Collections.nCopies(1, S));
            put("6-3", Arrays.asList(new Festival("韦驮菩萨圣诞"), D, Y));
            put("6-5", Collections.nCopies(1, new Festival("南赡部洲转大轮", SS)));
            put("6-6", Arrays.asList(new Festival("天地仓开日", SS), L));
            put("6-8", Collections.nCopies(1, T));
            put("6-10", Collections.nCopies(1, new Festival("金粟如来诞")));
            put("6-14", Collections.nCopies(1, T));
            put("6-15", Arrays.asList(W, T));
            put("6-19", Collections.nCopies(1, new Festival("观世音菩萨成道", DJ)));
            put("6-23", Arrays.asList(new Festival("南方火神诞", "犯者遭回禄"), T));
            put("6-24", Arrays.asList(new Festival("雷祖诞", XL), new Festival("关帝诞", XL)));
            put("6-25", Collections.nCopies(1, H));
            put("6-27", Collections.nCopies(1, D));
            put("6-28", Collections.nCopies(1, R));
            put("6-29", Collections.nCopies(1, T));
            put("6-30", Arrays.asList(HH, M, T));
            put("7-1", Arrays.asList(S, Y));
            put("7-3", Collections.nCopies(1, D));
            put("7-5", Collections.nCopies(1, new Festival("中会日", SS, false, "一作初七")));
            put("7-6", Collections.nCopies(1, L));
            put("7-7", Arrays.asList(new Festival("道德腊", XL), new Festival("五帝校生人善恶", XL), new Festival("魁星诞", XL)));
            put("7-8", Collections.nCopies(1, T));
            put("7-10", Collections.nCopies(1, new Festival("阴毒日", "", false, "大忌")));
            put("7-12", Collections.nCopies(1, new Festival("长真谭真人诞")));
            put("7-13", Collections.nCopies(1, new Festival("大势至菩萨诞", JS)));
            put("7-14", Arrays.asList(new Festival("三元降", JS), T));
            put("7-15", Arrays.asList(W, new Festival("三元降", DJ), new Festival("地官校籍", DJ), T));
            put("7-16", Collections.nCopies(1, new Festival("三元降", JS)));
            put("7-18", Collections.nCopies(1, new Festival("西王母诞", DJ)));
            put("7-19", Collections.nCopies(1, new Festival("太岁诞", DJ)));
            put("7-22", Collections.nCopies(1, new Festival("增福财神诞", XL)));
            put("7-23", Collections.nCopies(1, T));
            put("7-25", Collections.nCopies(1, H));
            put("7-27", Collections.nCopies(1, D));
            put("7-28", Collections.nCopies(1, R));
            put("7-29", Arrays.asList(Y, T));
            put("7-30", Arrays.asList(new Festival("地藏菩萨诞", DJ), HH, M, T));
            put("8-1", Arrays.asList(S, new Festival("许真君诞")));
            put("8-3", Arrays.asList(D, new Festival("北斗诞", XL), new Festival("司命灶君诞", "犯者遭回禄")));
            put("8-5", Collections.nCopies(1, new Festival("雷声大帝诞", DJ)));
            put("8-6", Collections.nCopies(1, L));
            put("8-8", Collections.nCopies(1, T));
            put("8-10", Collections.nCopies(1, new Festival("北斗大帝诞")));
            put("8-12", Collections.nCopies(1, new Festival("西方五道诞")));
            put("8-14", Collections.nCopies(1, T));
            put("8-15", Arrays.asList(W, new Festival("太明朝元", "犯者暴亡", false, "宜焚香守夜"), T));
            put("8-16", Collections.nCopies(1, new Festival("天曹掠刷真君降", "犯者贫夭")));
            put("8-18", Collections.nCopies(1, new Festival("天人兴福之辰", "", false, "宜斋戒，存想吉事")));
            put("8-23", Arrays.asList(new Festival("汉恒候张显王诞"), T));
            put("8-24", Collections.nCopies(1, new Festival("灶君夫人诞")));
            put("8-25", Collections.nCopies(1, H));
            put("8-27", Arrays.asList(D, new Festival("至圣先师孔子诞", XL), Y));
            put("8-28", Arrays.asList(R, new Festival("四天会事")));
            put("8-29", Collections.nCopies(1, T));
            put("8-30", Arrays.asList(new Festival("诸神考校", "犯者夺算"), HH, M, T));
            put("9-1", Arrays.asList(S, new Festival("南斗诞", XL), new Festival("北斗九星降世", DJ, false, "此九日俱宜斋戒")));
            put("9-3", Arrays.asList(D, new Festival("五瘟神诞")));
            put("9-6", Collections.nCopies(1, L));
            put("9-8", Collections.nCopies(1, T));
            put("9-9", Arrays.asList(new Festival("斗母诞", XL), new Festival("酆都大帝诞"), new Festival("玄天上帝飞升")));
            put("9-10", Collections.nCopies(1, new Festival("斗母降", DJ)));
            put("9-11", Collections.nCopies(1, new Festival("宜戒")));
            put("9-13", Collections.nCopies(1, new Festival("孟婆尊神诞")));
            put("9-14", Collections.nCopies(1, T));
            put("9-15", Arrays.asList(W, T));
            put("9-17", Collections.nCopies(1, new Festival("金龙四大王诞", "犯者遭水厄")));
            put("9-19", Arrays.asList(new Festival("日宫月宫会合", JS), new Festival("观世音菩萨诞", JS)));
            put("9-23", Collections.nCopies(1, T));
            put("9-25", Arrays.asList(H, Y));
            put("9-27", Collections.nCopies(1, D));
            put("9-28", Collections.nCopies(1, R));
            put("9-29", Collections.nCopies(1, T));
            put("9-30", Arrays.asList(new Festival("药师琉璃光佛诞", "犯者危疾"), HH, M, T));
            put("10-1", Arrays.asList(S, new Festival("民岁腊", DJ), new Festival("四天王降", "犯者一年内死")));
            put("10-3", Arrays.asList(D, new Festival("三茅诞")));
            put("10-5", Arrays.asList(new Festival("下会日", JS), new Festival("达摩祖师诞", JS)));
            put("10-6", Arrays.asList(L, new Festival("天曹考察", DJ)));
            put("10-8", Arrays.asList(new Festival("佛涅槃日", "", false, "大忌色欲"), T));
            put("10-10", Collections.nCopies(1, new Festival("四天王降", "犯者一年内死")));
            put("10-11", Collections.nCopies(1, new Festival("宜戒")));
            put("10-14", Arrays.asList(new Festival("三元降", JS), T));
            put("10-15", Arrays.asList(W, new Festival("三元降", DJ), new Festival("下元水府校籍", DJ), T));
            put("10-16", Arrays.asList(new Festival("三元降", JS), T));
            put("10-23", Arrays.asList(Y, T));
            put("10-25", Collections.nCopies(1, H));
            put("10-27", Arrays.asList(D, new Festival("北极紫徽大帝降")));
            put("10-28", Collections.nCopies(1, R));
            put("10-29", Collections.nCopies(1, T));
            put("10-30", Arrays.asList(HH, M, T));
            put("11-1", Collections.nCopies(1, S));
            put("11-3", Collections.nCopies(1, D));
            put("11-4", Collections.nCopies(1, new Festival("至圣先师孔子诞", XL)));
            put("11-6", Collections.nCopies(1, new Festival("西岳大帝诞")));
            put("11-8", Collections.nCopies(1, T));
            put("11-11", Arrays.asList(new Festival("天地仓开日", DJ), new Festival("太乙救苦天尊诞", DJ)));
            put("11-14", Collections.nCopies(1, T));
            put("11-15", Arrays.asList(new Festival("月望", "上半夜犯男死 下半夜犯女死"), new Festival("四天王巡行", "上半夜犯男死 下半夜犯女死")));
            put("11-17", Collections.nCopies(1, new Festival("阿弥陀佛诞")));
            put("11-19", Collections.nCopies(1, new Festival("太阳日宫诞", "犯者得奇祸")));
            put("11-21", Collections.nCopies(1, Y));
            put("11-23", Arrays.asList(new Festival("张仙诞", "犯者绝嗣"), T));
            put("11-25", Arrays.asList(new Festival("掠刷大夫降", "犯者遭大凶"), H));
            put("11-26", Collections.nCopies(1, new Festival("北方五道诞")));
            put("11-27", Collections.nCopies(1, D));
            put("11-28", Collections.nCopies(1, R));
            put("11-29", Collections.nCopies(1, T));
            put("11-30", Arrays.asList(HH, M, T));
            put("12-1", Collections.nCopies(1, S));
            put("12-3", Collections.nCopies(1, D));
            put("12-6", Arrays.asList(new Festival("天地仓开日", JS), L));
            put("12-7", Collections.nCopies(1, new Festival("掠刷大夫降", "犯者得恶疾")));
            put("12-8", Arrays.asList(new Festival("王侯腊", DJ), new Festival("释迦如来成佛之辰"), T, new Festival("初旬内戊日，亦名王侯腊", DJ)));
            put("12-12", Collections.nCopies(1, new Festival("太素三元君朝真")));
            put("12-14", Collections.nCopies(1, T));
            put("12-15", Arrays.asList(W, T));
            put("12-16", Collections.nCopies(1, new Festival("南岳大帝诞")));
            put("12-19", Collections.nCopies(1, Y));
            put("12-20", Collections.nCopies(1, new Festival("天地交道", "犯者促寿")));
            put("12-21", Collections.nCopies(1, new Festival("天猷上帝诞")));
            put("12-23", Arrays.asList(new Festival("五岳诞降"), T));
            put("12-24", Collections.nCopies(1, new Festival("司今朝天奏人善恶", "犯者得大祸")));
            put("12-25", Arrays.asList(new Festival("三清玉帝同降，考察善恶", "犯者得奇祸"), H));
            put("12-27", Collections.nCopies(1, D));
            put("12-28", Collections.nCopies(1, R));
            put("12-29", Arrays.asList(new Festival("华严菩萨诞"), T));
            put("12-30", Collections.nCopies(1, new Festival("诸神下降，察访善恶", "犯者男女俱亡")));
        }
    };
    /**
     * 每月初一的27星宿偏移
     */
    private static final int[] XIU_OFFSET = {11, 13, 15, 17, 19, 21, 24, 0, 2, 4, 7, 9};
    /**
     * 阴历
     */
    private final Lunar lunar;

    /**
     * 构造
     *
     * @param lunar 农历年
     */
    public Buddhist(Lunar lunar) {
        this.lunar = lunar;
    }

    /**
     * 获取27星宿
     *
     * @param month 佛历月
     * @param day   佛历日
     * @return 星宿
     */
    public static String getXiu(int month, int day) {
        return XIU_27[(XIU_OFFSET[Math.abs(month) - 1] + day - 1) % XIU_27.length];
    }

    /**
     * 日期转佛历
     *
     * @param lunar 农历年
     * @return this
     */
    public static Buddhist from(Lunar lunar) {
        return new Buddhist(lunar);
    }

    /**
     * 日期转佛历
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return this
     */
    public static Buddhist from(int year, int month, int day) {
        return from(year, month, day, 0, 0, 0);
    }

    /**
     * 日期转佛历
     *
     * @param year   年
     * @param month  月
     * @param day    日
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒
     * @return this
     */
    public static Buddhist from(int year, int month, int day, int hour, int minute, int second) {
        return from(Lunar.from(year + DEAD_YEAR - 1, month, day, hour, minute, second));
    }

    /**
     * 获取农历年
     *
     * @return the lunar
     */
    public Lunar getLunar() {
        return lunar;
    }

    /**
     * 获取年
     *
     * @return the int
     */
    public int getYear() {
        int sy = lunar.getSolar().getYear();
        int y = sy - DEAD_YEAR;
        if (sy == lunar.getYear()) {
            y++;
        }
        return y;
    }

    /**
     * 获取月
     *
     * @return the int
     */
    public int getMonth() {
        return lunar.getMonth();
    }

    /**
     * 获取天
     *
     * @return the int
     */
    public int getDay() {
        return lunar.getDay();
    }

    /**
     * 获取中国年
     *
     * @return the string
     */
    public String getYearInChinese() {
        String y = getYear() + "";
        StringBuilder s = new StringBuilder();
        for (int i = 0, j = y.length(); i < j; i++) {
            s.append(Fields.CN_NUMBER[y.charAt(i) - '0']);
        }
        return s.toString();
    }

    /**
     * 获取农历月
     *
     * @return the string
     */
    public String getMonthInChinese() {
        return lunar.getMonthInChinese();
    }

    /**
     * 获取农历天
     *
     * @return the string
     */
    public String getDayInChinese() {
        return lunar.getDayInChinese();
    }

    /**
     * 获取因果犯忌
     *
     * @return 因果犯忌
     */
    public List<Buddhist.Festival> getFestivals() {
        List<Buddhist.Festival> l = new ArrayList<>();
        List<Buddhist.Festival> fs = Buddhist.FESTIVAL.get(getMonth() + "-" + getDay());
        if (null != fs) {
            l.addAll(fs);
        }
        return l;
    }

    /**
     * 获取纪念日
     *
     * @return 纪念日
     */
    public List<String> getOtherFestivals() {
        List<String> l = new ArrayList<String>();
        List<String> fs = OTHER_FESTIVAL.get(getMonth() + "-" + getDay());
        if (null != fs) {
            l.addAll(fs);
        }
        return l;
    }

    /**
     * 是否月斋
     *
     * @return true/false
     */
    public boolean isMonthZhai() {
        int m = getMonth();
        return 1 == m || 5 == m || 9 == m;
    }

    /**
     * 是否杨公忌
     *
     * @return true/false
     */
    public boolean isDayYangGong() {
        for (Buddhist.Festival f : getFestivals()) {
            if ("杨公忌".equals(f.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否朔望斋
     *
     * @return true/false
     */
    public boolean isDayZhaiShuoWang() {
        int d = getDay();
        return 1 == d || 15 == d;
    }

    /**
     * 是否六斋日
     *
     * @return true/false
     */
    public boolean isDayZhaiSix() {
        int d = getDay();
        if (8 == d || 14 == d || 15 == d || 23 == d || 29 == d || 30 == d) {
            return true;
        } else if (28 == d) {
            Lunar.Month m = Lunar.Month.from(lunar.getYear(), getMonth());
            return null != m && 30 != m.getDayCount();
        }
        return false;
    }

    /**
     * 是否十斋日
     *
     * @return true/false
     */
    public boolean isDayZhaiTen() {
        int d = getDay();
        return 1 == d || 8 == d || 14 == d || 15 == d || 18 == d || 23 == d || 24 == d || 28 == d || 29 == d || 30 == d;
    }

    /**
     * 是否观音斋
     *
     * @return true/false
     */
    public boolean isDayZhaiGuanYin() {
        String k = getMonth() + "-" + getDay();
        for (String d : Buddhist.DAY_ZHAI_GUAN_YIN) {
            if (k.equals(d)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取星宿
     *
     * @return 星宿
     */
    public String getXiu() {
        return Buddhist.getXiu(getMonth(), getDay());
    }

    /**
     * 获取宿吉凶
     *
     * @return 吉/凶
     */
    public String getXiuLuck() {
        return Lunar.XIU_LUCK.get(getXiu());
    }

    /**
     * 获取宿歌诀
     *
     * @return 宿歌诀
     */
    public String getXiuSong() {
        return Lunar.XIU_SONG.get(getXiu());
    }

    /**
     * 获取政
     *
     * @return 政
     */
    public String getZheng() {
        return Lunar.ZHENG.get(getXiu());
    }

    /**
     * 获取动物
     *
     * @return 动物
     */
    public String getAnimal() {
        return Lunar.ANIMAL.get(getXiu());
    }

    /**
     * 获取宫
     *
     * @return 宫
     */
    public String getGong() {
        return Lunar.GONG.get(getXiu());
    }

    /**
     * 获取兽
     *
     * @return 兽
     */
    public String getShou() {
        return Lunar.SHOU.get(getGong());
    }

    @Override
    public String toString() {
        return getYearInChinese() + "年" + getMonthInChinese() + "月" + getDayInChinese();
    }

    /**
     * 转换为String
     *
     * @return the string
     */
    public String toFullString() {
        StringBuilder s = new StringBuilder();
        s.append(this);
        for (Buddhist.Festival f : getFestivals()) {
            s.append(" (");
            s.append(f);
            s.append(")");
        }
        return s.toString();
    }

    /**
     * 佛历因果犯忌
     */
    public static class Festival {

        /**
         * 是日何日，如：雷斋日
         */
        private final String name;

        /**
         * 犯之因果，如：犯者夺纪
         */
        private final String result;

        /**
         * 是否每月同
         */
        private final boolean everyMonth;

        /**
         * 备注，如：宜先一日即戒
         */
        private final String remark;

        /**
         * 构造
         *
         * @param name       名称
         * @param result     结果
         * @param everyMonth 月份
         * @param remark     备注
         */
        public Festival(String name, String result, boolean everyMonth, String remark) {
            this.name = name;
            this.result = null == result ? Normal.EMPTY : result;
            this.everyMonth = everyMonth;
            this.remark = null == remark ? Normal.EMPTY : remark;
        }

        /**
         * 构造
         *
         * @param name 名称
         */
        public Festival(String name) {
            this(name, null);
        }

        /**
         * 构造
         *
         * @param name   名称
         * @param result 结果
         */
        public Festival(String name, String result) {
            this(name, result, false);
        }

        /**
         * 构造
         *
         * @param name       名称
         * @param result     结果
         * @param everyMonth 月份
         */
        public Festival(String name, String result, boolean everyMonth) {
            this(name, result, everyMonth, null);
        }

        /**
         * 名称
         *
         * @return the string
         */
        public String getName() {
            return name;
        }

        /**
         * 结果
         *
         * @return the string
         */
        public String getResult() {
            return result;
        }

        /**
         * 是否为每个月
         *
         * @return the boolean
         */
        public boolean isEveryMonth() {
            return everyMonth;
        }

        /**
         * 备注
         *
         * @return the string
         */
        public String getRemark() {
            return remark;
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * 转换为字符串
         *
         * @return the string
         */
        public String toFullString() {
            StringBuilder s = new StringBuilder();
            s.append(name);
            if (null != result && result.length() > 0) {
                s.append(" ");
                s.append(result);
            }
            if (null != remark && remark.length() > 0) {
                s.append(" ");
                s.append(remark);
            }
            return s.toString();
        }

    }

}

