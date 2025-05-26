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
package org.miaixz.bus.core.center.date.culture.rabjung;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Zodiac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 藏历月，仅支持藏历1950年十二月至藏历2050年十二月
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabjungMonth extends Loops {

    public static final String[] NAMES = { "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月" };
    public static final String[] ALIAS = { "神变月", "苦行月", "具香月", "萨嘎月", "作净月", "明净月", "具醉月", "具贤月", "天降月", "持众月", "庄严月",
            "满意月" };

    /**
     * 藏历年
     */
    protected RabjungYear year;

    /**
     * 月
     */
    protected int month;

    /**
     * 是否闰月
     */
    protected boolean leap;

    /**
     * 位于当年的索引，0-12
     */
    protected int indexInYear;

    protected static final Map<Integer, int[]> DAYS = new HashMap<>();

    static {
        int y = 1950;
        int m = 11;
        String[] years = "2c>,182[>1:2TA4ZI=n1E2Bk1J2Ff3Mk503Oc62g=,172^>1:2XA1>2UE2Bo1I2Fj3Lo62Fb3Mf5,03N^72b=1:2]A1>2ZF1B2VI2Em1K2Fe,2Lh1R3Na603P\\:172Y>1;2UB2=m2Dq1J2Eh,2Kl1Q3Me603Pa:172^>1;2YA2=p1C2UI,2Dk2Jp3QEc3Mi603Pf:3L[72b?1:2]A1<2UB2XH,2Cn1I2Ei1L2Ie1Q3Na703Q\\:2`@1;2XA,4\\H;m1B2TI2Em1L2Ij1Q3Nf603Q`903QW:,2[@1;2TB2XI1E4TMAh2Io3RFe3Mj603Pc803Q[;,2^?1;2WA2>q1E2Bm1I2Fi1M2Hc3Of70,3P^82a>1:2[A1>2WE1B2TI2Fm1L2Hf3Ni6,03Oa703PZ:3`A62V>4]F;q1B4YJ>l2Eq1L2Gi3Ml5,03Nd603Q_9172[>1;2XB2>p1E2VK2Fl,1K2Fc3Mh603Pc9172`>1;2\\B1>2UD2=j2En,1J2Fg3Mm62Ib3Pj;3M_703R[:2`B1=2YB2=n,1C2TI2Fk1L2Ig1P3Nd703Q_:152X<2[A,2<q1B2WI2Ep1L2Il1Q3Ni703Qc9152[:2^@,1;2WB2>o1E2Bk1I2Fh1M2Ib3Pf803R^9,2a?1;2ZA1>2UE2Bp1I2Fl1M2If3Oi80,3Pa803QY:2^A1>2ZE1B4WJ>j2Fp1M2Hi1N2H`,3Od703Q]:162Y>1;2VB2?o1E4VM@h2Gl1M,2Hd3Ng603Qa9172^>1;2ZB1?2UE2@l2Fo1L,2Gg3Mk62H`3Pf:172c?3QY;2_B1>2YD2?o1E,2TK2Fj1M2Ie1P3Mb703R^;172X=2\\C1>,2TD2WJ2Fn1L2Ij1P3Ng703Rb:162[<2_B1=,2VC2>m1E4TMAh2Io3QFe3Nl82Ja3Qf:152_;0,3RU<2ZB1>2TE2Bn1I2Fj1M2Je3Pk:2K^3Ra:,03RY;2]A1>2XE1B2TI2Fo1M2Ii1P2Ka3Qd8,03R]:3bB62W>4]F:q1B2?n1F4VNAh2Il1O2Jd,3Pg803Q`:162\\=1;2XB1?2TF2Bl2Ho1N,2Ig3Nk703Qd9162`>1;2]B1?2XE2Ao1G2TM,2Hj1M2Id1P3M_603R\\;172W>2\\E1@2TE,2?i2Gm1M2Ih1P3Md603Ra;172[=28q1?2WD,2?m2Fq1M2Il1P3Mi72I^3Re:162_<172W=,2ZC2?q1E2Bk1I2Fh1M2Jd1Q3M^52b;16,2Y<2]B1>2VE2Bp1I2Fm1M2Jh1Q2Lb3Re:15,2\\;3aC62U>2[E1B4WJ>k1F4TNBg2Jl1P2Le3Qh9,03R`:172Z=1:2VB2?q1F2Bk2Ip1P2Jg,1P2J_3Qc:162^=1;2[B1?2WF2Bo1H2Bg2Ij,1O2Jc3Qg:3L\\62c>3QY;3aC72V?2[F1A2TG2Bj,2Hm1N2Jg1P3Mb603R_;182Z>1:2T@2WF2Am,2Gp1M2Ik1P3Mg603Rc;172^>192W?2ZE,2@p1F2Bj2Io3QEe1M2Jb1Q3M]72b=182Z>,2]D1?2VE2Bn1I2Fk1M2Jg1Q3Ma62e<172]=,172U>2YE1B2UI2Fp1N2Jk1Q3Me503M\\6,2`<172Y>3_F:2TB2?n1F2Cj2Jo3QDc2Lh1R,3L_52c;172]=1:2XB1?2UF2Cn1I2Eg2Kk1P,2Lb3Rf;162a=1:2]B1?2ZF1B2TH2Dj2Jm,1O2Kf1Q3M`603Q\\;182Y?2;q1A2WH2Cm,2Hq1O2Ji1P3Me603Qa;182]>1:2WA2[G2Ap,1G2Bi2Im1P3Mi72I_3Qf;3N\\72Eh1:2Z?29o,1@2UF2Bm1I2Fh1M2Je1Q3N`72f?3PY92]>19,2U?2YF2Bq1I2Fm1M2Jj1Q3Nd603O]72`=,182X?4]F:o1B4WI=k1F4UNCi2Jn3REc3Mh503N`6,2c<182\\>1:2VA2?q1F2Cm1J2Fg2Lk1R3Mc5,2f<172`=1:2[A1?2XF2Cq1I2Ek2Kn1R,2Lf1R3N_62d>3PZ:3aC72W?2;p1B2WI2Dn1J,2De2Ki1Q3Mc603Q_:182\\?1;2VB2<m2Cq1I,2Dh2Jl1P3Mg603Qd;182`?1;2ZA2<p1B,2UH2Cl1I2Ef3Mm82Jc1Q3N_703QY:2]@1;2UA,2XG2Bp1I2Fk1M2Jh1Q3Nc703Q]92`?1:,2X@4\\G:n1B2VI2Fp1M2Jl1R3Ng603P`82d>,192[?1;2UA2>o1F2Ck1J2Gg3Mk603Oc70,3OZ82_>1:2YA1?2VF2Cp1J2Fj1M2Gc3Nf5,03O^72b>1:2^B1?4[G;n1C2VJ2Fn1L2Gf,3Mi503Nb603Q]:172Y?1<2UB2>m2Eq1K2Fi,2Kl1R3Mf603Qa:182^?1;2YB2>q1D2VJ,2Dl1J2Fe3Mj603Qg;3N]72c@3QX;2]A1=2VB,2YI2Co1J2Fi1M2Je1Q3Nb703R]:2aA1<2XA,2<n1C2UI2Fn1M2Jj1Q3Nf703Q`903RX:,2[@1<2TB4YJ>l1E4UNBi1J2Ge3Mk703Pc803Q[9,2^?1;2XB2>q1E2Cn1J2Gj1M2Ic3Of70,3P^82b?1;2\\A1>2XF1C2UJ2Fm1M2Hf3Ni6,03Oa703Q[:3aB72W>1<2TC2?m2Fq1L2Gi3Ml5,03Ne703Q_:172\\>1<2XB2?q1E2WL2Fl,1L2Gd3Ni603Qd:172a?1;2\\B1>2VD2>k,2Eo1K2Gh1M2Ic1Q3N`703R\\;3aC62U=2YC2>o,1D2TJ2Fl1M2Jh1Q3Ne703R`:162Y<2\\B,1=2TC4XJ=j2Fp1M2Jm3QFc3Ni803Qc:152\\;2_A,1<2WB2>o1E2Bl1J2Gh1N2Jc3Qg903R^:,2b@1;2[B1>2VE2Cq1J2Gl1N2Jf3Pj80,3Qa803RZ;2_B1>4[F:o1C4XK?k2Fp1M2Ii1O2Ia,3Pd703R^:172Y>1<2VC2?p1F2Ai2Hl1M,2Hd3Oh703Qb:172^>1<2[C1?2UE2Al2Go,1L2Hg3Nl82Ia3Qg;3M]72e@3RZ;3`C72T>2YD2@o1E,2TK2Gk1M2Jf1Q3Nb703R^;172Y=2\\D1>,2TD4XK>i2Fo1M2Jj1Q3Ng703Rb;172\\<2`C1=,2WC2?n1F4VNBi1J2Gf1N2Kb3Rf:162_;15,2V<2ZB1?2TE2Bn1J2Gk1N2Kf1Q2L^3Rb:,152Z;2^B1>2YE1B2UJ2Go1N2Ji1P2Kb3Qd9,03R];172X>1;2TC2@n1G2Bi2Im1O2Jd,3Ph803Ra:172\\>1;2YC1@2UF2Bl2Hp1N,2Ig3Ol82J`3Qe:172a>1;4^C7q1?2XF2Ao1G2UN,2Hj1N2Jd1Q3N`703R];182X>2]F1@2TF,2@j2Gn1M2Jq1Q3Ne703Ra;172\\>192T?,2WE2@m1F4TMAf2Im3QEc3Nj82J`3Rf;172_=182W>,2ZD2?q1F2Bl1I2Gj1N2Ke1R3M_62b<17,2Z=2]C1?2WE2Bq1I2Gn1N2Ki1Q3Mb52e;16,2]<172V>4[F:o1B4XK?l1G4UOCh2Jl1Q2Le3Rh:,152`;172Z>1;2WB2@q1G2Cl2Ip1P2K_"
                .split(",", -1);
        for (String ys : years) {
            while (!ys.isEmpty()) {
                int len = ys.charAt(0) - '0';
                int[] data = new int[len];
                for (int i = 0; i < len; i++) {
                    data[i] = ys.charAt(i + 1) - '5' - 30;
                }
                DAYS.put(y * 13 + m, data);
                m++;
                ys = ys.substring(len + 1);
            }
            y++;
            m = 0;
        }
    }

    public RabjungMonth(RabjungYear year, int month) {
        if (month == 0 || month > 12 || month < -12) {
            throw new IllegalArgumentException(String.format("illegal rab-byung month: %d", month));
        }
        int y = year.getYear();
        if (y < 1950 || y > 2050) {
            throw new IllegalArgumentException(String.format("rab-byung year %d must between 1950 and 2050", y));
        }
        int m = Math.abs(month);
        if (y == 1950 && m < 12) {
            throw new IllegalArgumentException(String.format("month %d must be 12 in rab-byung year %d", month, y));
        }
        boolean leap = month < 0;
        int leapMonth = year.getLeapMonth();
        if (leap && m != leapMonth) {
            throw new IllegalArgumentException(String.format("illegal leap month %d in rab-byung year %d", m, y));
        }
        this.year = year;
        this.month = m;
        this.leap = leap;
        // 位于当年的索引
        int index = m - 1;
        if (leap || (leapMonth > 0 && m > leapMonth)) {
            index += 1;
        }
        indexInYear = index;
    }

    /**
     * 从藏历年月初始化
     *
     * @param year  藏历年
     * @param month 藏历月，闰月为负
     */
    public RabjungMonth(int year, int month) {
        this(RabjungYear.fromYear(year), month);
    }

    public RabjungMonth(int rabByungIndex, RabjungElement element, Zodiac zodiac, int month) {
        this(RabjungYear.fromElementZodiac(rabByungIndex, element, zodiac), month);
    }

    /**
     * 从藏历年月初始化
     *
     * @param year  藏历年
     * @param month 藏历月，闰月为负
     * @return 藏历月
     */
    public static RabjungMonth fromYm(int year, int month) {
        return new RabjungMonth(year, month);
    }

    public static RabjungMonth fromElementZodiac(int rabByungIndex, RabjungElement element, Zodiac zodiac, int month) {
        return new RabjungMonth(rabByungIndex, element, zodiac, month);
    }

    /**
     * 藏历年
     *
     * @return 藏历年
     */
    public RabjungYear getRabByungYear() {
        return year;
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return year.getYear();
    }

    /**
     * 月
     *
     * @return 月
     */
    public int getMonth() {
        return month;
    }

    /**
     * 月
     *
     * @return 月，当月为闰月时，返回负数
     */
    public int getMonthWithLeap() {
        return leap ? -month : month;
    }

    /**
     * 位于当年的索引(0-12)
     *
     * @return 索引
     */
    public int getIndexInYear() {
        return indexInYear;
    }

    /**
     * 是否闰月
     *
     * @return true/false
     */
    public boolean isLeap() {
        return leap;
    }

    /**
     * 名称
     *
     * @return 名称
     */
    public String getName() {
        return (leap ? "闰" : "") + NAMES[month - 1];
    }

    /**
     * 别名
     *
     * @return 别名
     */
    public String getAlias() {
        return (leap ? "闰" : "") + ALIAS[month - 1];
    }

    @Override
    public String toString() {
        return year + getName();
    }

    public RabjungMonth next(int n) {
        if (n == 0) {
            return fromYm(getYear(), getMonthWithLeap());
        }
        int m = indexInYear + 1 + n;
        RabjungYear y = year;
        int leapMonth = y.getLeapMonth();
        if (n > 0) {
            int monthCount = leapMonth > 0 ? 13 : 12;
            while (m > monthCount) {
                m -= monthCount;
                y = y.next(1);
                leapMonth = y.getLeapMonth();
                monthCount = leapMonth > 0 ? 13 : 12;
            }
        } else {
            while (m <= 0) {
                y = y.next(-1);
                leapMonth = y.getLeapMonth();
                m += leapMonth > 0 ? 13 : 12;
            }
        }
        boolean leap = false;
        if (leapMonth > 0) {
            if (m == leapMonth + 1) {
                leap = true;
            }
            if (m > leapMonth) {
                m--;
            }
        }
        return fromYm(y.getYear(), leap ? -m : m);
    }

    /**
     * 首日
     *
     * @return 藏历日
     */
    public RabjungDay getFirstDay() {
        return new RabjungDay(this, 1);
    }

    /**
     * 本月的藏历日列表
     *
     * @return 藏历日列表
     */
    public List<RabjungDay> getDays() {
        List<RabjungDay> l = new ArrayList<>();
        List<Integer> missDays = getMissDays();
        List<Integer> leapDays = getLeapDays();
        for (int i = 1; i <= 30; i++) {
            if (missDays.contains(i)) {
                continue;
            }
            l.add(new RabjungDay(this, i));
            if (leapDays.contains(i)) {
                l.add(new RabjungDay(this, -i));
            }
        }
        return l;
    }

    /**
     * 当月天数
     *
     * @return 数量
     */
    public int getDayCount() {
        return 30 + getLeapDays().size() - getMissDays().size();
    }

    /**
     * 特殊日子列表，闰日为正，缺日为负
     *
     * @return 特殊日子列表
     */
    public List<Integer> getSpecialDays() {
        List<Integer> l = new ArrayList<>();
        int[] days = DAYS.get(getYear() * 13 + getIndexInYear());
        if (null == days) {
            return l;
        }
        for (int d : days) {
            l.add(d);
        }
        return l;
    }

    /**
     * 闰日列表
     *
     * @return 闰日列表
     */
    public List<Integer> getLeapDays() {
        List<Integer> l = new ArrayList<>();
        List<Integer> days = getSpecialDays();
        for (int d : days) {
            if (d > 0) {
                l.add(d);
            }
        }
        return l;
    }

    /**
     * 缺日列表
     *
     * @return 缺日列表
     */
    public List<Integer> getMissDays() {
        List<Integer> l = new ArrayList<>();
        List<Integer> days = getSpecialDays();
        for (int d : days) {
            if (d < 0) {
                l.add(-d);
            }
        }
        return l;
    }

}
