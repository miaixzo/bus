/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang;

/**
 * Enumeration for various time zones, providing both English and Chinese names.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ZoneId {

    /**
     * Coordinated Universal Time (UTC).
     */
    UTC("World Standard Time", "世界标准时间"),
    /**
     * Greenwich Mean Time (GMT).
     */
    GMT("Universal Time", "世界时"),
    /**
     * Australia/Darwin time zone.
     */
    ACT("Australia/Darwin", "澳洲/达尔文"),
    /**
     * Australia/Sydney time zone.
     */
    AET("Australia/Sydney", "澳洲/悉尼"),
    /**
     * America/Argentina/Buenos_Aires time zone.
     */
    AGT("America/Argentina/Buenos_Aires", "美洲/阿根廷/布宜诺斯艾利斯"),
    /**
     * Africa/Cairo time zone.
     */
    ART("Africa/Cairo", "非洲/开罗"),
    /**
     * America/Anchorage time zone.
     */
    AST("America/Anchorage", "美洲/安克雷奇"),
    /**
     * America/Sao_Paulo time zone.
     */
    BET("America/Sao_Paulo", "美洲/圣保罗"),
    /**
     * Asia/Dhaka time zone.
     */
    BST("Asia/Dhaka", "亚洲/达卡"),
    /**
     * Africa/Harare time zone.
     */
    CAT("Africa/Harare", "非洲/哈拉雷"),
    /**
     * America/St_Johns time zone.
     */
    CNT("America/St_Johns", "美洲/圣约翰"),
    /**
     * America/Chicago time zone.
     */
    CST("America/Chicago", "美洲/芝加哥"),
    /**
     * Asia/Shanghai time zone with +08:00 offset.
     */
    CTT("+08:00", "Asia/Shanghai", "亚洲/上海"),
    /**
     * Africa/Addis_Ababa time zone.
     */
    EAT("Africa/Addis_Ababa", "非洲/亚的斯亚贝巴"),
    /**
     * Europe/Paris time zone.
     */
    ECT("Europe/Paris", "欧洲/巴黎"),
    /**
     * America/Indiana/Indianapolis time zone.
     */
    IET("America/Indiana/Indianapolis", "美洲/印第安纳州/印第安纳波利斯"),
    /**
     * Asia/Kolkata time zone.
     */
    IST("Asia/Kolkata", "亚洲/加尔各答"),
    /**
     * Asia/Tokyo time zone.
     */
    JST("Asia/Tokyo", "亚洲/东京"),
    /**
     * Pacific/Apia time zone.
     */
    MIT("Pacific/Apia", "太平洋/阿皮亚"),
    /**
     * Asia/Yerevan time zone.
     */
    NET("Asia/Yerevan", "亚洲/埃里温"),
    /**
     * Pacific/Auckland time zone.
     */
    NST("Pacific/Auckland", "太平洋/奥克兰"),
    /**
     * Asia/Karachi time zone.
     */
    PLT("Asia/Karachi", "亚洲/卡拉奇"),
    /**
     * America/Phoenix time zone.
     */
    PNT("America/Phoenix", "美洲/凤凰城"),
    /**
     * America/Puerto_Rico time zone.
     */
    PRT("America/Puerto_Rico", "美洲/波多黎各"),
    /**
     * America/Los_Angeles time zone.
     */
    PST("America/Los_Angeles", "美洲/洛杉矶"),
    /**
     * Pacific/Guadalcanal time zone.
     */
    SST("Pacific/Guadalcanal", "太平洋/瓜达尔卡纳尔岛"),
    /**
     * Asia/Ho_Chi_Minh time zone with +08:00 offset.
     */
    VST("+08:00", "Asia/Ho_Chi_Minh", "亚洲/胡志明市"),
    /**
     * North American Eastern Standard Time (EST) with -05:00 offset.
     */
    EST("-05:00", "EST", "北美东部标准时间"),
    /**
     * North American Mountain Daylight Time (MDT) with -07:00 offset.
     */
    MST("-07:00", "MDT", "北美山地标准时间"),
    /**
     * Hawaii-Aleutian Standard Time (HST) with -10:00 offset.
     */
    HST("-10:00", "HST", "夏威夷-阿留申标准时区");

    /**
     * The system time zone identifier.
     */
    private String zoneId;
    /**
     * The English name of the time zone.
     */
    private String enName;
    /**
     * The Chinese name of the time zone.
     */
    private String cnName;

    /**
     * Constructs a {@code ZoneId} enum constant with a time zone identifier.
     *
     * @param zoneId The time zone identifier.
     */
    ZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * Constructs a {@code ZoneId} enum constant with English and Chinese names.
     *
     * @param enName The English name of the time zone.
     * @param cnName The Chinese name of the time zone.
     */
    ZoneId(String enName, String cnName) {
        this.enName = enName;
        this.cnName = cnName;
    }

    /**
     * Constructs a {@code ZoneId} enum constant with a time zone identifier, English name, and Chinese name.
     *
     * @param zoneId The time zone identifier.
     * @param enName The English name of the time zone.
     * @param cnName The Chinese name of the time zone.
     */
    ZoneId(String zoneId, String enName, String cnName) {
        this.zoneId = zoneId;
        this.enName = enName;
        this.cnName = cnName;
    }

    /**
     * Returns the system time zone identifier.
     *
     * @return The time zone identifier.
     */
    public String getZoneId() {
        return zoneId;
    }

    /**
     * Returns the English name of the time zone.
     *
     * @return The English name.
     */
    public String getEnName() {
        return enName;
    }

    /**
     * Returns the Chinese name of the time zone.
     *
     * @return The Chinese name.
     */
    public String getCnName() {
        return cnName;
    }

}
