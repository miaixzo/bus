/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 
 * Enumeration for various internationalization (I18n) languages and locales. Each enum constant represents a specific
 * language or language-country combination, providing both a language code and a descriptive name.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum I18n {

    /**
     * Automatically detect the language.
     */
    AUTO_DETECT(Normal.EMPTY, "Auto Detect"),
    /**
     * English
     */
    EN("en", "English"),
    /**
     * English (United States)
     */
    EN_US("en_US", "English (United States)"),
    /**
     * Arabic
     */
    AR("ar", "Arabic"),
    /**
     * Arabic (United Arab Emirates)
     */
    AR_AE("ar_AE", "Arabic (United Arab Emirates)"),
    /**
     * Arabic (Bahrain)
     */
    AR_BH("ar_BH", "Arabic (Bahrain)"),
    /**
     * Arabic (Algeria)
     */
    AR_DZ("ar_DZ", "Arabic (Algeria)"),
    /**
     * Arabic (Egypt)
     */
    AR_EG("ar_EG", "Arabic (Egypt)"),
    /**
     * Arabic (Iraq)
     */
    AR_IQ("ar_IQ", "Arabic (Iraq)"),
    /**
     * Arabic (Jordan)
     */
    AR_JO("ar_JO", "Arabic (Jordan)"),
    /**
     * Arabic (Kuwait)
     */
    AR_KW("ar_KW", "Arabic (Kuwait)"),
    /**
     * Arabic (Lebanon)
     */
    AR_LB("ar_LB", "Arabic (Lebanon)"),
    /**
     * Arabic (Libya)
     */
    AR_LY("ar_LY", "Arabic (Libya)"),
    /**
     * Arabic (Morocco)
     */
    AR_MA("ar_MA", "Arabic (Morocco)"),
    /**
     * Arabic (Oman)
     */
    AR_OM("ar_OM", "Arabic (Oman)"),
    /**
     * Arabic (Qatar)
     */
    AR_QA("ar_QA", "Arabic (Qatar)"),
    /**
     * Arabic (Saudi Arabia)
     */
    AR_SA("ar_SA", "Arabic (Saudi Arabia)"),
    /**
     * Arabic (Sudan)
     */
    AR_SD("ar_SD", "Arabic (Sudan)"),
    /**
     * Arabic (Syria)
     */
    AR_SY("ar_SY", "Arabic (Syria)"),
    /**
     * Arabic (Tunisia)
     */
    AR_TN("ar_TN", "Arabic (Tunisia)"),
    /**
     * Arabic (Yemen)
     */
    AR_YE("ar_YE", "Arabic (Yemen)"),
    /**
     * Belarusian
     */
    BE("be", "Belarusian"),
    /**
     * Belarusian (Belarus)
     */
    BE_BY("be_BY", "Belarusian (Belarus)"),
    /**
     * Bulgarian
     */
    BG("bg", "Bulgarian"),
    /**
     * Bulgarian (Bulgaria)
     */
    BG_BG("bg_BG", "Bulgarian (Bulgaria)"),
    /**
     * Catalan
     */
    CA("ca", "Catalan"),
    /**
     * Catalan (Spain)
     */
    CA_ES("ca_ES", "Catalan (Spain)"),
    /**
     * Catalan (Spain, Euro)
     */
    CA_ES_EURO("ca_ES_EURO", "Catalan (Spain, Euro)"),
    /**
     * Czech
     */
    CS("cs", "Czech"),
    /**
     * Czech (Czech Republic)
     */
    CS_CZ("cs_CZ", "Czech (Czech Republic)"),
    /**
     * Danish
     */
    DA("da", "Danish"),
    /**
     * Danish (Denmark)
     */
    DA_DK("da_DK", "Danish (Denmark)"),
    /**
     * German
     */
    DE("de", "German"),
    /**
     * German (Austria)
     */
    DE_AT("de_AT", "German (Austria)"),
    /**
     * German (Austria, Euro)
     */
    DE_AT_EURO("de_AT_EURO", "German (Austria, Euro)"),
    /**
     * German (Switzerland)
     */
    DE_CH("de_CH", "German (Switzerland)"),
    /**
     * German (Germany)
     */
    DE_DE("de_DE", "German (Germany)"),
    /**
     * German (Germany, Euro)
     */
    DE_DE_EURO("de_DE_EURO", "German (Germany, Euro)"),
    /**
     * German (Luxembourg)
     */
    DE_LU("de_LU", "German (Luxembourg)"),
    /**
     * German (Luxembourg, Euro)
     */
    DE_LU_EURO("de_LU_EURO", "German (Luxembourg, Euro)"),
    /**
     * Greek
     */
    EL("el", "Greek"),
    /**
     * Greek (Greece)
     */
    EL_GR("el_GR", "Greek (Greece)"),
    /**
     * English (Australia)
     */
    EN_AU("en_AU", "English (Australia)"),
    /**
     * English (Canada)
     */
    EN_CA("en_CA", "English (Canada)"),
    /**
     * English (United Kingdom)
     */
    EN_GB("en_GB", "English (United Kingdom)"),
    /**
     * English (Ireland)
     */
    EN_IE("en_IE", "English (Ireland)"),
    /**
     * English (Ireland, Euro)
     */
    EN_IE_EURO("en_IE_EURO", "English (Ireland, Euro)"),
    /**
     * English (New Zealand)
     */
    EN_NZ("en_NZ", "English (New Zealand)"),
    /**
     * English (South Africa)
     */
    EN_ZA("en_ZA", "English (South Africa)"),
    /**
     * Spanish
     */
    ES("es", "Spanish"),
    /**
     * Spanish (Bolivia)
     */
    ES_BO("es_BO", "Spanish (Bolivia)"),
    /**
     * Spanish (Argentina)
     */
    ES_AR("es_AR", "Spanish (Argentina)"),
    /**
     * Spanish (Chile)
     */
    ES_CL("es_CL", "Spanish (Chile)"),
    /**
     * Spanish (Colombia)
     */
    ES_CO("es_CO", "Spanish (Colombia)"),
    /**
     * Spanish (Costa Rica)
     */
    ES_CR("es_CR", "Spanish (Costa Rica)"),
    /**
     * Spanish (Dominican Republic)
     */
    ES_DO("es_DO", "Spanish (Dominican Republic)"),
    /**
     * Spanish (Ecuador)
     */
    ES_EC("es_EC", "Spanish (Ecuador)"),
    /**
     * Spanish (Spain)
     */
    ES_ES("es_ES", "Spanish (Spain)"),
    /**
     * Spanish (Spain, Euro)
     */
    ES_ES_EURO("es_ES_EURO", "Spanish (Spain, Euro)"),
    /**
     * Spanish (Guatemala)
     */
    ES_GT("es_GT", "Spanish (Guatemala)"),
    /**
     * Spanish (Honduras)
     */
    ES_HN("es_HN", "Spanish (Honduras)"),
    /**
     * Spanish (Mexico)
     */
    ES_MX("es_MX", "Spanish (Mexico)"),
    /**
     * Spanish (Nicaragua)
     */
    ES_NI("es_NI", "Spanish (Nicaragua)"),
    /**
     * Estonian
     */
    ET("et", "Estonian"),
    /**
     * Spanish (Panama)
     */
    ES_PA("es_PA", "Spanish (Panama)"),
    /**
     * Spanish (Peru)
     */
    ES_PE("es_PE", "Spanish (Peru)"),
    /**
     * Spanish (Puerto Rico)
     */
    ES_PR("es_PR", "Spanish (Puerto Rico)"),
    /**
     * Spanish (Paraguay)
     */
    ES_PY("es_PY", "Spanish (Paraguay)"),
    /**
     * Spanish (El Salvador)
     */
    ES_SV("es_SV", "Spanish (El Salvador)"),
    /**
     * Spanish (Uruguay)
     */
    ES_UY("es_UY", "Spanish (Uruguay)"),
    /**
     * Spanish (Venezuela)
     */
    ES_VE("es_VE", "Spanish (Venezuela)"),
    /**
     * Estonian (Estonia)
     */
    ET_EE("et_EE", "Estonian (Estonia)"),
    /**
     * Finnish
     */
    FI("fi", "Finnish"),
    /**
     * Finnish (Finland)
     */
    FI_FI("fi_FI", "Finnish (Finland)"),
    /**
     * Finnish (Finland, Euro)
     */
    FI_FI_EURO("fi_FI_EURO", "Finnish (Finland, Euro)"),
    /**
     * French
     */
    FR("fr", "French"),
    /**
     * French (Belgium)
     */
    FR_BE("fr_BE", "French (Belgium)"),
    /**
     * French (Belgium, Euro)
     */
    FR_BE_EURO("fr_BE_EURO", "French (Belgium, Euro)"),
    /**
     * French (Canada)
     */
    FR_CA("fr_CA", "French (Canada)"),
    /**
     * French (Switzerland)
     */
    FR_CH("fr_CH", "French (Switzerland)"),
    /**
     * French (France)
     */
    FR_FR("fr_FR", "French (France)"),
    /**
     * French (France, Euro)
     */
    FR_FR_EURO("fr_FR_EURO", "French (France, Euro)"),
    /**
     * French (Luxembourg)
     */
    FR_LU("fr_LU", "French (Luxembourg)"),
    /**
     * French (Luxembourg, Euro)
     */
    FR_LU_EURO("fr_LU_EURO", "French (Luxembourg, Euro)"),
    /**
     * Croatian
     */
    HR("hr", "Croatian"),
    /**
     * Croatian (Croatia)
     */
    HR_HR("hr_HR", "Croatian (Croatia)"),
    /**
     * Hungarian
     */
    HU("hu", "Hungarian"),
    /**
     * Hungarian (Hungary)
     */
    HU_HU("hu_HU", "Hungarian (Hungary)"),
    /**
     * Icelandic
     */
    IS("is", "Icelandic"),
    /**
     * Icelandic (Iceland)
     */
    IS_IS("is_IS", "Icelandic (Iceland)"),
    /**
     * Italian
     */
    IT("it", "Italian"),
    /**
     * Italian (Switzerland)
     */
    IT_CH("it_CH", "Italian (Switzerland)"),
    /**
     * Italian (Italy)
     */
    IT_IT("it_IT", "Italian (Italy)"),
    /**
     * Italian (Italy, Euro)
     */
    IT_IT_EURO("it_IT_EURO", "Italian (Italy, Euro)"),
    /**
     * Hebrew
     */
    IW("iw", "Hebrew"),
    /**
     * Hebrew (Israel)
     */
    IW_IL("iw_IL", "Hebrew (Israel)"),
    /**
     * Japanese
     */
    JA("ja", "Japanese"),
    /**
     * Japanese (Japan)
     */
    JA_JP("ja_JP", "Japanese (Japan)"),
    /**
     * Korean
     */
    KO("ko", "Korean"),
    /**
     * Korean (South Korea)
     */
    KO_KR("ko_KR", "Korean (South Korea)"),
    /**
     * Lithuanian
     */
    LT("lt", "Lithuanian"),
    /**
     * Lithuanian (Lithuania)
     */
    LT_LT("lt_LT", "Lithuanian (Lithuania)"),
    /**
     * Latvian
     */
    LV("lv", "Latvian"),
    /**
     * Latvian (Latvia)
     */
    LV_LV("lv_LV", "Latvian (Latvia)"),
    /**
     * Macedonian
     */
    MK("mk", "Macedonian"),
    /**
     * Macedonian (Macedonia)
     */
    MK_MK("mk_MK", "Macedonian (Macedonia)"),
    /**
     * Dutch
     */
    NL("nl", "Dutch"),
    /**
     * Dutch (Belgium)
     */
    NL_BE("nl_BE", "Dutch (Belgium)"),
    /**
     * Dutch (Belgium, Euro)
     */
    NL_BE_EURO("nl_BE_EURO", "Dutch (Belgium, Euro)"),
    /**
     * Dutch (Netherlands)
     */
    NL_NL("nl_NL", "Dutch (Netherlands)"),
    /**
     * Dutch (Netherlands, Euro)
     */
    NL_NL_EURO("nl_NL_EURO", "Dutch (Netherlands, Euro)"),
    /**
     * Norwegian
     */
    NO("no", "Norwegian"),
    /**
     * Norwegian (Norway)
     */
    NO_NO("no_NO", "Norwegian (Norway)"),
    /**
     * Norwegian (Norway, Nynorsk)
     */
    NO_NO_NY("no_NO_NY", "Norwegian (Norway, Nynorsk)"),
    /**
     * Polish
     */
    PL("pl", "Polish"),
    /**
     * Polish (Poland)
     */
    PL_PL("pl_PL", "Polish (Poland)"),
    /**
     * Portuguese
     */
    PT("pt", "Portuguese"),
    /**
     * Portuguese (Brazil)
     */
    PT_BR("pt_BR", "Portuguese (Brazil)"),
    /**
     * Portuguese (Portugal)
     */
    PT_PT("pt_PT", "Portuguese (Portugal)"),
    /**
     * Portuguese (Portugal, Euro)
     */
    PT_PT_EURO("pt_PT_EURO", "Portuguese (Portugal, Euro)"),
    /**
     * Romanian
     */
    RO("ro", "Romanian"),
    /**
     * Romanian (Romania)
     */
    RO_RO("ro_RO", "Romanian (Romania)"),
    /**
     * Russian
     */
    RU("ru", "Russian"),
    /**
     * Russian (Russia)
     */
    RU_RU("ru_RU", "Russian (Russia)"),
    /**
     * Serbo-Croatian
     */
    SH("sh", "Serbo-Croatian"),
    /**
     * Serbo-Croatian (Yugoslavia)
     */
    SH_YU("sh_YU", "Serbo-Croatian (Yugoslavia)"),
    /**
     * Slovak
     */
    SK("sk", "Slovak"),
    /**
     * Slovak (Slovakia)
     */
    SK_SK("sk_SK", "Slovak (Slovakia)"),
    /**
     * Slovenian
     */
    SL("sl", "Slovenian"),
    /**
     * Slovenian (Slovenia)
     */
    SL_SI("sl_SI", "Slovenian (Slovenia)"),
    /**
     * Albanian
     */
    SQ("sq", "Albanian"),
    /**
     * Albanian (Albania)
     */
    SQ_AL("sq_AL", "Albanian (Albania)"),
    /**
     * Serbian
     */
    SR("sr", "Serbian"),
    /**
     * Serbian (Yugoslavia)
     */
    SR_YU("sr_YU", "Serbian (Yugoslavia)"),
    /**
     * Swedish
     */
    SV("sv", "Swedish"),
    /**
     * Swedish (Sweden)
     */
    SV_SE("sv_SE", "Swedish (Sweden)"),
    /**
     * Thai
     */
    TH("th", "Thai"),
    /**
     * Thai (Thailand)
     */
    TH_TH("th_TH", "Thai (Thailand)"),
    /**
     * Turkish
     */
    TR("tr", "Turkish"),
    /**
     * Turkish (Turkey)
     */
    TR_TR("tr_TR", "Turkish (Turkey)"),
    /**
     * Ukrainian
     */
    UK("uk", "Ukrainian"),
    /**
     * Ukrainian (Ukraine)
     */
    UK_UA("uk_UA", "Ukrainian (Ukraine)"),
    /**
     * Chinese
     */
    ZH("zh", "Chinese"),
    /**
     * Chinese (Mainland China)
     */
    ZH_CN("zh_CN", "Chinese (Mainland China)"),
    /**
     * Chinese (Hong Kong)
     */
    ZH_HK("zh_HK", "Chinese (Hong Kong)"),
    /**
     * Chinese (Taiwan)
     */
    ZH_TW("zh_TW", "Chinese (Taiwan)");

    /**
     * The language code (e.g., "en", "zh_CN").
     */
    private final String lang;

    /**
     * A descriptive name for the language or locale.
     */
    private final String desc;

    /**
     * Constructs an {@code I18n} enum constant.
     *
     * @param lang The language code.
     * @param desc The descriptive name.
     */
    I18n(String lang, String desc) {
        this.lang = lang;
        this.desc = desc;
    }

    /**
     * Returns the language code for this locale.
     *
     * @return The language code.
     */
    public String lang() {
        return this.lang;
    }

    /**
     * Returns the {@link Locale} represented by this enum.
     *
     * @return The matching locale.
     */
    public Locale toLocale() {
        if (this == AUTO_DETECT) {
            return Locale.getDefault();
        }
        return Locale.forLanguageTag(this.lang.replace('_', '-'));
    }

    /**
     * Returns the descriptive name for this locale.
     *
     * @return The descriptive name.
     */
    public String desc() {
        return this.desc;
    }

    /**
     * Gets a formatted string for the current i18n from a resource bundle.
     *
     * @param bundleName The name of the resource bundle.
     * @param key        The key for the desired string.
     * @param args       The message arguments.
     * @return The formatted string.
     */
    public String message(String bundleName, String key, Object... args) {
        return message(this, bundleName, key, args);
    }

    /**
     * Gets a formatted string for a specific i18n from a resource bundle.
     *
     * @param i18n       The i18n to use.
     * @param bundleName The name of the resource bundle.
     * @param key        The key for the desired string.
     * @param args       The message arguments.
     * @return The formatted string.
     */
    public static String message(I18n i18n, String bundleName, String key, Object... args) {
        Locale locale = i18n == null ? Locale.getDefault() : i18n.toLocale();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException e) {
            return MessageFormat.format(key, args);
        } catch (Exception e) {
            return MessageFormat.format(key, args);
        }
    }

}
