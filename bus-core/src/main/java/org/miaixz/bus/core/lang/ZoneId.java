/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 时区计算
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ZoneId {

    /**
     * "Worldwide","世界标准时间"
     */
    UTC("World Standard Time", "世界标准时间"),
    /**
     * "Universal Time","世界时"
     */
    GMT("Universal Time", "世界时"),
    /**
     * "Australia/Darwin","澳洲/达尔文"
     */
    ACT("Australia/Darwin", "澳洲/达尔文"),
    /**
     * "Australia/Sydney","澳洲/悉尼"
     */
    AET("Australia/Sydney", "澳洲/悉尼"),
    /**
     * "America/Argentina/Buenos_Aires","美洲/阿根廷/布宜诺斯艾利斯"
     */
    AGT("America/Argentina/Buenos_Aires", "美洲/阿根廷/布宜诺斯艾利斯"),
    /**
     * "Africa/Cairo","非洲/开罗"
     */
    ART("Africa/Cairo", "非洲/开罗"),
    /**
     * "America/Anchorage","美洲/安克雷奇"
     */
    AST("America/Anchorage", "美洲/安克雷奇"),
    /**
     * "America/Sao_Paulo","美洲/圣保罗"
     */
    BET("America/Sao_Paulo", "美洲/圣保罗"),
    /**
     * "Asia/Dhaka","亚洲/达卡"
     */
    BST("Asia/Dhaka", "亚洲/达卡"),
    /**
     * "Africa/Harare","非洲/哈拉雷"
     */
    CAT("Africa/Harare", "非洲/哈拉雷"),
    /**
     * "America/St_Johns","美洲/圣约翰"
     */
    CNT("America/St_Johns", "美洲/圣约翰"),
    /**
     * "America/Chicago","美洲/芝加哥"
     */
    CST("America/Chicago", "美洲/芝加哥"),
    /**
     * "+08:00","Asia/Shanghai", "亚洲/上海"
     */
    CTT("+08:00", "Asia/Shanghai", "亚洲/上海"),
    /**
     * "Africa/Addis_Ababa","非洲/亚的斯亚贝巴"
     */
    EAT("Africa/Addis_Ababa", "非洲/亚的斯亚贝巴"),
    /**
     * "Europe/Paris","欧洲/巴黎"
     */
    ECT("Europe/Paris", "欧洲/巴黎"),
    /**
     * "America/Indiana/Indianapolis","美洲/印第安纳州/印第安纳波利斯"
     */
    IET("America/Indiana/Indianapolis", "美洲/印第安纳州/印第安纳波利斯"),
    /**
     * "Asia/Kolkata","亚洲/加尔各答"
     */
    IST("Asia/Kolkata", "亚洲/加尔各答"),
    /**
     * "Asia/Tokyo","亚洲/东京"
     */
    JST("Asia/Tokyo", "亚洲/东京"),
    /**
     * "Pacific/Apia","太平洋/阿皮亚"
     */
    MIT("Pacific/Apia", "太平洋/阿皮亚"),
    /**
     * "Asia/Yerevan","亚洲/埃里温"
     */
    NET("Asia/Yerevan", "亚洲/埃里温"),
    /**
     * "Pacific/Auckland","太平洋/奥克兰"
     */
    NST("Pacific/Auckland", "太平洋/奥克兰"),
    /**
     * "Asia/Karachi","亚洲/卡拉奇"
     */
    PLT("Asia/Karachi", "亚洲/卡拉奇"),
    /**
     * "America/Phoenix","美洲/凤凰城"
     */
    PNT("America/Phoenix", "美洲/凤凰城"),
    /**
     * "America/Puerto_Rico","美洲/波多黎各"
     */
    PRT("America/Puerto_Rico", "美洲/波多黎各"),
    /**
     * "America/Los_Angeles","美洲/洛杉矶"
     */
    PST("America/Los_Angeles", "美洲/洛杉矶"),
    /**
     * "Pacific/Guadalcanal","太平洋/瓜达尔卡纳尔岛"
     */
    SST("Pacific/Guadalcanal", "太平洋/瓜达尔卡纳尔岛"),
    /**
     * "+08:00", "Asia/Ho_Chi_Minh", "亚洲/胡志明市"
     */
    VST("+08:00", "Asia/Ho_Chi_Minh", "亚洲/胡志明市"),
    /**
     * "-05:00", "EST", "北美东部标准时间"
     */
    EST("-05:00", "EST", "北美东部标准时间"),
    /**
     * "-07:00", "MDT","北美山地标准时间"
     */
    MST("-07:00", "MST", "北美山地标准时间"),
    /**
     * "-10:00", "HST", "夏威夷-阿留申标准时区"
     */
    HST("-10:00", "HST", "夏威夷-阿留申标准时区");

    /**
     * 系统时区
     */
    private String zoneId;
    /**
     * 英文名称
     */
    private String enName;
    /**
     * 中文名称
     */
    private String cnName;

    ZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    ZoneId(String enName, String cnName) {
        this.enName = enName;
        this.cnName = cnName;
    }

    ZoneId(String zoneId, String enName, String cnName) {
        this.zoneId = zoneId;
        this.enName = enName;
        this.cnName = cnName;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getEnName() {
        return enName;
    }

    public String getCnName() {
        return cnName;
    }

}
