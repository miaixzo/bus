/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              ~
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
package org.miaixz.bus.core.center.date.culture.solar;

import org.miaixz.bus.core.center.date.culture.Galaxy;
import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;
import org.miaixz.bus.core.xyz.EnumKit;

/**
 * 节气
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarTerms extends Samsara {

    public static final String[] NAMES = X.get("name");

    /**
     * 粗略的儒略日
     */
    protected double cursoryJulianDay;

    public SolarTerms(int year, int index) {
        super(NAMES, index);
        initByYear(year, index);
    }

    public SolarTerms(int year, String name) {
        super(NAMES, name);
        initByYear(year, index);
    }

    public SolarTerms(double cursoryJulianDay, int index) {
        super(NAMES, index);
        this.cursoryJulianDay = cursoryJulianDay;
    }

    public static SolarTerms fromIndex(int year, int index) {
        return new SolarTerms(year, index);
    }

    public static SolarTerms fromName(int year, String name) {
        return new SolarTerms(year, name);
    }

    protected void initByYear(int year, int offset) {
        double jd = Math.floor((year - 2000) * 365.2422 + 180);
        // 355是2000.12冬至，得到较靠近jd的冬至估计值
        double w = Math.floor((jd - 355 + 183) / 365.2422) * 365.2422 + 355;
        if (Galaxy.calcQi(w) > jd) {
            w -= 365.2422;
        }
        cursoryJulianDay = Galaxy.calcQi(w + 15.2184 * offset);
    }

    public SolarTerms next(int n) {
        return new SolarTerms(cursoryJulianDay + 15.2184 * n, nextIndex(n));
    }

    /**
     * 是否节
     *
     * @return true/false
     */
    public boolean isJie() {
        return index % 2 == 1;
    }

    /**
     * 是否气
     *
     * @return true/false
     */
    public boolean isQi() {
        return index % 2 == 0;
    }

    /**
     * 儒略日
     *
     * @return 儒略日
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromJulianDay(Galaxy.qiAccurate2(cursoryJulianDay) + JulianDay.J2000);
    }

    /**
     * 粗略的儒略日
     *
     * @return 儒略日数
     */
    public double getCursoryJulianDay() {
        return cursoryJulianDay;
    }

    public enum X {

        S_DZ(22, "冬至", "斗指子。太阳黄经为270°。冬至这一天，阳光几乎直射南回归线，我们北半球白昼最短，黑夜最长，开始进入数九寒天。天文学上规定这一天是北半球冬季的开始。而冬至以后，阳光直射位置逐渐向北移动，北半球的白天就逐渐长了，谚云：吃了冬至面，一天长一线。一候蚯蚓结；二候糜角解；三候水泉动。传说蚯蚓是阴曲阳伸的生物，此时阳气虽已生长，但阴气仍然十分强盛，土中的蚯蚓仍然蜷缩着身体；糜与鹿同科，却阴阳不同，古人认为糜的角朝后生，所以为阴，而冬至一阳生，糜感阴气渐退而解角；由于阳气初生，所以此时山中的泉水可以流动并且温热。"),
        S_XH(23, "小寒", "斗指子，太阳黄经为285°。小寒以后，开始进入寒冷季节。冷气积久而寒，小寒是天气寒冷但还没有到极点的意思。一候雁北乡，二候鹊始巢，三候雉始鸲。古人认为候鸟中大雁是顺阴阳而迁移，此时阳气已动，所以大雁开始向北迁移；此时北方到处可见到喜鹊，并且感觉到阳气而开始筑巢；第三候“雉鸲”的“鸲”为鸣叫的意思，雉在接近四九时会感阳气的生长而鸣叫。"),
        S_DH(24, "大寒", "斗指丑，太阳黄经为300°。大寒就是天气寒冷到了极点的意思。大寒前后是一年中最冷的季节。大寒正值三九刚过，四九之初。谚云：“三九四九冰上走”。一候鸡乳；二候征鸟厉疾；三候水泽腹坚。就是说到大寒节气便可以孵小鸡了；而鹰隼之类的征鸟，却正处于捕食能力极强的状态中，盘旋于空中到处寻找食物，以补充身体的能量抵御严寒；在一年的最后五天内，水域中的冰一直冻到水中央，且最结实、最厚。"),
        S_LC(1, "立春", "斗指东北。太阳黄经为315度。是二十四个节气的头一个节气。其含义是开始进入春天，“阳和起蛰，品物皆春”，过了立春，万物复苏生机勃勃，一年四季从此开始了。一候东风解冻，二候蜇虫始振，三候鱼陟负冰。说的是东风送暖，大地开始解冻。立春五日后，蜇居的虫类慢慢在洞中苏醒，再过五日，河里的冰开始溶化，鱼开始到水面上游动，此时水面上还有没完全溶解的碎冰片，如同被鱼负着一般浮在水面。"),
        S_YS(2, "雨水", "斗指壬。太阳黄经为330°。这时春风遍吹，冰雪融化，空气湿润，雨水增多，所以叫雨水。人们常说：“立春天渐暖，雨水送肥忙”。一候獭祭鱼；二候鸿雁来；三候草木萌劝。此节气，水獭开始捕鱼了，将鱼摆在岸边如同先祭后食的样子；五天过后，大雁开始从南方飞回北方；再过五天，在“润物细无声”的春雨中，草木随地中阳气的上腾而开始抽出嫩芽。从此，大地渐渐开始呈现出一派欣欣向荣的景象。"),
        S_JZ(3, "惊蛰", "斗指丁。太阳黄经为345°。这个节气表示“立春”以后天气转暖，春雷开始震响，蛰伏在泥土里的各种冬眠动物将苏醒过来开始活动起来，所以叫惊蛰。这个时期过冬的虫排卵也要开始孵化。我国部分地区进入了春耕季节。谚语云：“惊蛰过，暖和和，蛤蟆老角唱山歌。”“惊蛰一犁土，春分地气通。”“惊蛰没到雷先鸣，大雨似蛟龙。”一候桃始华；二候仓庚（黄鹂）鸣；三候鹰化为鸠。描述已是桃花红、李花白，黄莺鸣叫、燕飞来的时节，大部分地区都已进入了春耕。惊醒了蛰伏在泥土中冬眠的各种昆虫的时候，此时过冬的虫卵也要开始卵化，由此可见惊蛰是反映自然物候现象的一个节气。"),
        S_CF(4, "春分", "斗指壬。太阳黄经为0°。春分日太阳在赤道上方。这是春季90天的中分点，这一天南北两半球昼夜相等，所以叫春分。这天以后太阳直射位置便向北移，北半球昼长夜短。所以春分是北半球春季开始。我国大部分地区越冬作物进入春季生长阶段。各地农谚有：“春分在前，斗米斗钱”（广东）、“春分甲子雨绵绵，夏分甲子火烧天”（四川）、“春分有雨家家忙，先种瓜豆后插秧”（湖北）、“春分种菜，大暑摘瓜”（湖南）、“春分种麻种豆，秋分种麦种蒜”（安徽）。一候元鸟至；二候雷乃发声；三候始电。是说春分日后，燕子便从南方飞来了，下雨时天空便要打雷并发出闪电。"),
        S_QM(5, "清明", "斗指丁。太阳黄经为15°。此时气候清爽温暖，草木始发新枝芽，万物开始生长，农民忙于春耕春种。从前，在清明节这一天，有些人家都在门口插上杨柳条，还到郊外踏青，祭扫坟墓，这是古老的习俗。一候桐始华；二候田鼠化为鹌；三候虹始见。意思是在这个时节先是白桐花开放，接着喜阴的田鼠不见了，全回到了地下的洞中，然后是雨后的天空可以见到彩虹了。"),
        S_GY(6, "谷雨", "斗指癸。太阳黄经为30°。就是雨水生五谷的意思，由于雨水滋润大地五谷得以生长，所以，谷雨就是“雨生百谷”。谚云“谷雨前后，种瓜种豆”。一候萍始生；二候呜鸠拂其羽；三候为戴任降于桑。是说谷雨后降雨量增多，浮萍开始生长，接着布谷鸟便开始提醒人们播种了，然后是桑树上开始见到戴胜鸟。"),
        S_LX(7, "立夏", "斗指东南。太阳黄经为45°。是夏季的开始，从此进入夏天，万物旺盛大。习惯上把立夏当作是气温显著升高，炎暑将临，雷雨增多，农作物进入旺季生长的一个最重要节气。一候蝼蝈鸣；二候蚯蚓出；三候王瓜生。即说这一节气中首先可听到蜊蜊（即：蝼蛄）蛄在田间的呜叫声（一说是蛙声），接着大地上便可看到蚯蚓掘土，然后王瓜的蔓藤开始快速攀爬生长。"),
        S_XM(8, "小满", "斗指甲。太阳黄经为60°。从小满开始，大麦、冬小麦等夏收作物，已经结果、籽粒饱满，但尚未成熟，所以叫小满。一候苦菜秀；二候靡草死；三候麦秋至。是说小满节气中，苦菜已经枝叶繁茂；而喜阴的一些枝条细软的草类在强烈的阳光下开始枯死；此时麦子开始成熟。"),
        S_MZ(9, "芒种", "北斗指向己。太阳黄经为75°。这时最适合播种有芒的谷类作物，如晚谷、黍、稷等。如过了这个时候再种有芒和作物就不好成熟了。同时，“芒”指有芒作物如小麦、大麦等，“种”指种子。芒种即表明小麦等有芒作物成熟。芒种前后，我国中部的长江中、下游地区，雨量增多，气温升高，进入连绵阴雨的梅雨季节，空气非常潮湿，天气异常闷热，各种器具和衣物容易发霉，所以在我国长江中、下游地区也叫“霉雨”。一候螳螂生；二候鹏始鸣；三候反舌无声。在这一节气中，螳螂在去年深秋产的卵因感受到阴气初生而破壳生出小螳螂；喜阴的伯劳鸟开始在枝头出现，并且感阴而鸣；与此相反，能够学习其它鸟鸣叫的反舌鸟，却因感应到了阴气的出现而停止了鸣叫。"),
        S_XZ(10, "夏至", "北斗指向乙。太阳黄经为90°。太阳在黄经90°“夏至点”时，阳光几乎直射北回归线上空，北半球正午太阳最高。这一天是北半球白昼最长、黑夜最短的一天，从这一天起，进入炎热季节，天地万物在此时生长最旺盛。所以古时候又把这一天叫做日北至，意思是太阳运行到最北的一日。过了夏至，太阳逐渐向南移动，北半球白昼一天比一天缩短，黑夜一天比一天加长。一候鹿角解；二候蝉始鸣；三候半夏生。糜与鹿虽属同科，但古人认为，二者一属阴一属阳。鹿的角朝前生，所以属阳。夏至日阴气生而阳气始衰，所以阳性的鹿角便丌始脱落。而糜因属阴，所以在冬至日角才脱落；雄性的知了在夏至后因感阴气之生便鼓翼而鸣；半夏是一种喜阴的药草，因在仲夏的沼泽地或水田中出生所以得名。由此可见，在炎热的仲夏，一些喜阴的生物开始出现，而阳性的生物却开始衰退了。"),
        S_XS(11, "小暑", "斗指辛。太阳黄经为105°。天气已经很热了，但还不到最热的时候，所以叫小暑。此时，已是初伏前后。一候温风至；二候蟋蟀居宇；三候鹰始鸷。小暑时节大地上便不再有一丝凉风，而是所有的风中都带着热浪。"),
        S_DS(12, "大暑", "斗指丙。太阳黄经为120°。大暑是一年中最热的节气，正值勤二伏前后，长江流域的许多地方，经常出现40℃高温天气。要作好防暑降温工作。这个节气雨水多，在“小暑、大暑，淹死老鼠”的谚语，要注意防汛防涝。一候腐草为萤；二候土润溽暑；三候大雨时行。世上萤火虫约有二千多种，分水生与陆生两种，陆生的萤火虫产卵于枯草上，大暑时，萤火虫卵化而出，所以古人认为萤火虫是腐草变成的；第二候是说天气开始变得闷热，土地也很潮湿；第三候是说时常有大的雷雨会出现，这大雨使暑湿减弱，天气开始向立秋过渡。"),
        S_LQ(13, "立秋", "北斗指向西南。太阳黄经为135°。从这一天起秋天开始，秋高气爽，月明风清。此后，气温由最热逐渐下降一候凉风至；二候白露生；三候寒蝉鸣。是说立秋过后，刮风时人们会感觉到凉爽，此时的风已不同于暑天中的热风；接着，大地上早晨会有雾气产生；并且秋天感阴而鸣的寒蝉也开始鸣叫。"),
        S_CS(14, "处暑", "斗指戊。太阳黄经为150°。这时夏季火热已经到头了。暑气就要散了。它是温度下降的一个转折点。是气候变凉的象征，表示暑天终止。一候鹰乃祭鸟；二候天地始肃；三候禾乃登。此节气中老鹰开始大量捕猎鸟类；天地间万物开始凋零；“禾乃登”的“禾”指的是黍、稷、稻、粱类农作物的总称，“登”即成熟的意思。"),
        S_BL(15, "白露", "斗指癸。太阳黄经为165°。天气转凉，地面水汽结露。一候鸿雁来；二候元鸟归；三候群鸟养羞。说此节气正是鸿雁与燕子等候鸟南飞避寒，百鸟开始贮存干果粮食以备过冬。可见白露实际上是天气转凉的象征。"),
        S_QF(16, "秋分", "斗指已。太阳黄经为180°。秋分这一天同春分一样，阳光几乎直射赤道，昼夜几乎相等。从这一天起，阳光直射位置继续由赤道向南半球推移，北半球开始昼短夜长。依我国旧历的秋季论，这一天刚好是秋季九十天的一半，因而称秋分。但在天文学上规定，北半球的秋天是从秋分开始的。一候雷始收声；二候蛰虫坯户；三候水始涸。古人认为雷是因为阳气盛而发声，秋分后阴气开始旺盛，所以不再打雷了。"),
        S_HL(17, "寒露", "斗指甲。太阳黄经为195°。白露后，天气转凉，开始出现露水，到了寒露，则露水日多，且气温更低了。所以，有人说，寒是露之气，先白而后寒，是气候将逐渐转冷的意思。而水气则凝成白色露珠。一候鸿雁来宾；二候雀人大水为蛤；三候菊有黄华。此节气中鸿雁排成一字或人字形的队列大举南迁；深秋天寒，雀鸟都不见了，古人看到海边突然出现很多蛤蜊，并且贝壳的条纹及颜色与雀鸟很相似，所以便以为是雀鸟变成的；第三候的“菊始黄华”是说在此时菊花已普遍开放。"),
        S_SJ(18, "霜降", "斗指戌。太阳黄经为210°。天气已冷，开始有霜冻了，所以叫霜降。一候豺乃祭兽； 此节气中豺狼将捕获的猎物先陈列后再食用；二候草木黄落；大地上的树叶枯黄掉落；三候蜇虫咸俯；蜇虫也全在洞中不动不食，垂下头来进入冬眠状态中。"),
        S_LD(19, "立冬", "斗指乾。太阳黄经为225°。习惯上，我国人民把这一天当作冬季的开始。冬，作为终了之意，是指一年的田间操作结束了，作物收割之后要收藏起来的意思。立冬一过，我国黄河中、下游地区即将结冰，我国各地农民都将陆续地转入农田水利基本建设和其他农事活动中。一候水始冰；二候地始冻；三候雉人大水为蜃。此节气水已经能结成冰；土地也开始冻结；三候“雉人大水为蜃”中的雉即指野鸡一类的大鸟，蜃为大蛤，立冬后，野鸡一类的大鸟便不多见了，而海边却可以看到外壳与野鸡的线条及颜色相似的大蛤。所以古人认为雉到立冬后便变成大蛤了。"),
        S_XX(20, "小雪", "斗指己。太阳黄经为240°。气温下降，开始降雪，但还不到大雪纷飞的时节，所以叫小雪。小雪前后，黄河流域开始降雪（南方降雪还要晚两个节气）；而北方，已进入封冻季节。一候虹藏不见；二候天气上升地气下降；三候闭塞而成冬。由于天空中的阳气上升，地中的阴气下降，导致天地不通，阴阳不交，所以万物失去生机，天地闭塞而转入严寒的冬天。"),
        S_DX(21, "大雪", "斗指癸。太阳黄经为255°。大雪前后，黄河流域一带渐有积雪；而北方，已是“千里冰封，万里雪飘”的严冬了。一候鹃鸥不呜；二候虎始交；三候荔挺出。这是说此时因天气寒冷，寒号鸟也不再呜叫了；由于此时是阴气最盛时期，正所谓盛极而衰，阳气已有所萌动，所以老虎开始有求偶行为；“荔挺”为兰草的一种，也感到阳气的萌动而抽出新芽。");

        private static final X[] ENUMS = X.values();

        /**
         * 编码
         */
        private final int code;
        /**
         * 名称
         */
        private final String name;

        /**
         * 描述
         */
        private final String desc;

        X(final int code, final String name, final String desc) {
            this.code = code;
            this.name = name;
            this.desc = desc;
        }

        /**
         * 获取节气
         *
         * @param code 编码
         * @return this
         */
        public static X get(int code) {
            if (code < 1 || code > 24) {
                throw new IllegalArgumentException();
            }
            return ENUMS[code];
        }

        /**
         * 获取枚举属性信息
         *
         * @param fieldName 属性名称
         * @return the string[]
         */
        public static String[] get(String fieldName) {
            return EnumKit.getFieldValues(X.class, fieldName).toArray(String[]::new);
        }

        /**
         * @return 对应的名称
         */
        public String getName(final int code) {
            return this.name;
        }

        /**
         * @return 对应的编码
         */
        public long getCode() {
            return this.code;
        }

        /**
         * @return 对应的名称
         */
        public String getName() {
            return this.name;
        }

    }

}
