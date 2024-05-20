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
package org.miaixz.bus.core.lang;

/**
 * 语言枚举
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum I18n {

    /**
     * 自动匹配
     */
    AUTO_DETECT(Normal.EMPTY, "自动匹配"),
    EN("en", " 英文"),
    EN_US("en_US", " 英文 (美国)"),
    AR("ar", " 阿拉伯文"),
    AR_AE("ar_AE", " 阿拉伯文 (阿拉伯联合酋长国)"),
    AR_BH("ar_BH", " 阿拉伯文 (巴林)"),
    AR_DZ("ar_DZ", " 阿拉伯文 (阿尔及利亚)"),
    AR_EG("ar_EG", " 阿拉伯文 (埃及)"),
    AR_IQ("ar_IQ", " 阿拉伯文 (伊拉克)"),
    AR_JO("ar_JO", " 阿拉伯文 (约旦)"),
    AR_KW("ar_KW", " 阿拉伯文 (科威特)"),
    AR_LB("ar_LB", " 阿拉伯文 (黎巴嫩)"),
    AR_LY("ar_LY", " 阿拉伯文 (利比亚)"),
    AR_MA("ar_MA", " 阿拉伯文 (摩洛哥)"),
    AR_OM("ar_OM", " 阿拉伯文 (阿曼)"),
    AR_QA("ar_QA", " 阿拉伯文 (卡塔尔)"),
    AR_SA("ar_SA", " 阿拉伯文 (沙特阿拉伯)"),
    AR_SD("ar_SD", " 阿拉伯文 (苏丹)"),
    AR_SY("ar_SY", " 阿拉伯文 (叙利亚)"),
    AR_TN("ar_TN", " 阿拉伯文 (突尼斯)"),
    AR_YE("ar_YE", " 阿拉伯文 (也门)"),
    BE("be", " 白俄罗斯文"),
    BE_BY("be_BY", " 白俄罗斯文 (白俄罗斯)"),
    BG("bg", " 保加利亚文"),
    BG_BG("bg_BG", " 保加利亚文 (保加利亚)"),
    CA("ca", " 加泰罗尼亚文"),
    CA_ES("ca_ES", " 加泰罗尼亚文 (西班牙)"),
    CA_ES_EURO("ca_ES_EURO", " 加泰罗尼亚文 (西班牙,Euro)"),
    CS("cs", " 捷克文"),
    CS_CZ("cs_CZ", " 捷克文 (捷克共和国)"),
    DA("da", " 丹麦文"),
    DA_DK("da_DK", " 丹麦文 (丹麦)"),
    DE("de", " 德文"),
    DE_AT("de_AT", " 德文 (奥地利)"),
    DE_AT_EURO("de_AT_EURO", " 德文 (奥地利,Euro)"),
    DE_CH("de_CH", " 德文 (瑞士)"),
    DE_DE("de_DE", " 德文 (德国)"),
    DE_DE_EURO("de_DE_EURO", " 德文 (德国,Euro)"),
    DE_LU("de_LU", " 德文 (卢森堡)"),
    DE_LU_EURO("de_LU_EURO", " 德文 (卢森堡,Euro)"),
    EL("el", " 希腊文"),
    EL_GR("el_GR", " 希腊文 (希腊)"),
    EN_AU("en_AU", " 英文 (澳大利亚)"),
    EN_CA("en_CA", " 英文 (加拿大)"),
    EN_GB("en_GB", " 英文 (英国)"),
    EN_IE("en_IE", " 英文 (爱尔兰)"),
    EN_IE_EURO("en_IE_EURO", " 英文 (爱尔兰,Euro)"),
    EN_NZ("en_NZ", " 英文 (新西兰)"),
    EN_ZA("en_ZA", " 英文 (南非)"),
    ES("es", " 西班牙文"),
    ES_BO("es_BO", " 西班牙文 (玻利维亚)"),
    ES_AR("es_AR", " 西班牙文 (阿根廷)"),
    ES_CL("es_CL", " 西班牙文 (智利)"),
    ES_CO("es_CO", " 西班牙文 (哥伦比亚)"),
    ES_CR("es_CR", " 西班牙文 (哥斯达黎加)"),
    ES_DO("es_DO", " 西班牙文 (多米尼加共和国)"),
    ES_EC("es_EC", " 西班牙文 (厄瓜多尔)"),
    ES_ES("es_ES", " 西班牙文 (西班牙)"),
    ES_ES_EURO("es_ES_EURO", " 西班牙文 (西班牙,Euro)"),
    ES_GT("es_GT", " 西班牙文 (危地马拉)"),
    ES_HN("es_HN", " 西班牙文 (洪都拉斯)"),
    ES_MX("es_MX", " 西班牙文 (墨西哥)"),
    ES_NI("es_NI", " 西班牙文 (尼加拉瓜)"),
    ET("et", " 爱沙尼亚文"),
    ES_PA("es_PA", " 西班牙文 (巴拿马)"),
    ES_PE("es_PE", " 西班牙文 (秘鲁)"),
    ES_PR("es_PR", " 西班牙文 (波多黎哥)"),
    ES_PY("es_PY", " 西班牙文 (巴拉圭)"),
    ES_SV("es_SV", " 西班牙文 (萨尔瓦多)"),
    ES_UY("es_UY", " 西班牙文 (乌拉圭)"),
    ES_VE("es_VE", " 西班牙文 (委内瑞拉)"),
    ET_EE("et_EE", " 爱沙尼亚文 (爱沙尼亚)"),
    FI("fi", " 芬兰文"),
    FI_FI("fi_FI", " 芬兰文 (芬兰)"),
    FI_FI_EURO("fi_FI_EURO", " 芬兰文 (芬兰,Euro)"),
    FR("fr", " 法文"),
    FR_BE("fr_BE", " 法文 (比利时)"),
    FR_BE_EURO("fr_BE_EURO", " 法文 (比利时,Euro)"),
    FR_CA("fr_CA", " 法文 (加拿大)"),
    FR_CH("fr_CH", " 法文 (瑞士)"),
    FR_FR("fr_FR", " 法文 (法国)"),
    FR_FR_EURO("fr_FR_EURO", " 法文 (法国,Euro)"),
    FR_LU("fr_LU", " 法文 (卢森堡)"),
    FR_LU_EURO("fr_LU_EURO", " 法文 (卢森堡,Euro)"),
    HR("hr", " 克罗地亚文"),
    HR_HR("hr_HR", " 克罗地亚文 (克罗地亚)"),
    HU("hu", " 匈牙利文"),
    HU_HU("hu_HU", " 匈牙利文 (匈牙利)"),
    IS("is", " 冰岛文"),
    IS_IS("is_IS", " 冰岛文 (冰岛)"),
    IT("it", " 意大利文"),
    IT_CH("it_CH", " 意大利文 (瑞士)"),
    IT_IT("it_IT", " 意大利文 (意大利)"),
    IT_IT_EURO("it_IT_EURO", " 意大利文 (意大利,Euro)"),
    IW("iw", " 希伯来文"),
    IW_IL("iw_IL", " 希伯来文 (以色列)"),
    JA("ja", " 日文"),
    JA_JP("ja_JP", " 日文 (日本)"),
    KO("ko", " 朝鲜文"),
    KO_KR("ko_KR", " 朝鲜文 (南朝鲜)"),
    LT("lt", " 立陶宛文"),
    LT_LT("lt_LT", " 立陶宛文 (立陶宛)"),
    LV("lv", " 拉托维亚文(列托)"),
    LV_LV("lv_LV", " 拉托维亚文(列托) (拉脱维亚)"),
    MK("mk", " 马其顿文"),
    MK_MK("mk_MK", " 马其顿文 (马其顿王国)"),
    NL("nl", " 荷兰文"),
    NL_BE("nl_BE", " 荷兰文 (比利时)"),
    NL_BE_EURO("nl_BE_EURO", " 荷兰文 (比利时,Euro)"),
    NL_NL("nl_NL", " 荷兰文 (荷兰)"),
    NL_NL_EURO("nl_NL_EURO", " 荷兰文 (荷兰,Euro)"),
    NO("no", " 挪威文"),
    NO_NO("no_NO", " 挪威文 (挪威)"),
    NO_NO_NY("no_NO_NY", " 挪威文 (挪威,Nynorsk)"),
    PL("pl", " 波兰文"),
    PL_PL("pl_PL", " 波兰文 (波兰)"),
    PT("pt", " 葡萄牙文"),
    PT_BR("pt_BR", " 葡萄牙文 (巴西)"),
    PT_PT("pt_PT", " 葡萄牙文 (葡萄牙)"),
    PT_PT_EURO("pt_PT_EURO", " 葡萄牙文 (葡萄牙,Euro)"),
    RO("ro", " 罗马尼亚文"),
    RO_RO("ro_RO", " 罗马尼亚文 (罗马尼亚)"),
    RU("ru", " 俄文"),
    RU_RU("ru_RU", " 俄文 (俄罗斯)"),
    SH("sh", " 塞波尼斯-克罗地亚文"),
    SH_YU("sh_YU", " 塞波尼斯-克罗地亚文 (南斯拉夫)"),
    SK("sk", " 斯洛伐克文"),
    SK_SK("sk_SK", " 斯洛伐克文 (斯洛伐克)"),
    SL("sl", " 斯洛文尼亚文"),
    SL_SI("sl_SI", " 斯洛文尼亚文 (斯洛文尼亚)"),
    SQ("sq", " 阿尔巴尼亚文"),
    SQ_AL("sq_AL", " 阿尔巴尼亚文 (阿尔巴尼亚)"),
    SR("sr", " 塞尔维亚文"),
    SR_YU("sr_YU", " 塞尔维亚文 (南斯拉夫)"),
    SV("sv", " 瑞典文"),
    SV_SE("sv_SE", " 瑞典文 (瑞典)"),
    TH("th", " 泰文"),
    TH_TH("th_TH", " 泰文 (泰国)"),
    TR("tr", " 土耳其文"),
    TR_TR("tr_TR", " 土耳其文 (土耳其)"),
    UK("uk", " 乌克兰文"),
    UK_UA("uk_UA", " 乌克兰文 (乌克兰)"),
    ZH("zh", " 中文"),
    ZH_CN("zh_CN", " 中文 (中国大陆)"),
    ZH_HK("zh_HK", " 中文 (中国香港)"),
    ZH_TW("zh_TW", " 中文 (中国台湾)");

    /**
     * 语言类型
     */
    private final String lang;

    /**
     * 描述
     */
    private final String desc;

    I18n(String lang, String desc) {
        this.lang = lang;
        this.desc = desc;
    }

    /**
     * 显示语言信息
     *
     * @return 字符串结果
     */
    public String lang() {
        return this.lang;
    }

    /**
     * 显示语言信息
     *
     * @return 字符串结果
     */
    public String desc() {
        return this.desc;
    }

}
