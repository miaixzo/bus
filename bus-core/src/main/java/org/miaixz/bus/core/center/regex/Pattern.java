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
package org.miaixz.bus.core.center.regex;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Regex;

/**
 * 常用正则表达式集合，更多正则见:
 * <a href="https://any86.github.io/any-rule/">https://any86.github.io/any-rule/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Pattern {

    /**
     * 英文字母 、数字和下划线
     */
    public static final java.util.regex.Pattern GENERAL_PATTERN = java.util.regex.Pattern.compile(Regex.GENERAL);
    /**
     * 数字
     */
    public static final java.util.regex.Pattern NUMBERS_PATTERN = java.util.regex.Pattern.compile(Regex.NUMBERS);
    /**
     * 字母
     */
    public static final java.util.regex.Pattern WORD_PATTERN = java.util.regex.Pattern.compile(Regex.WORD);
    /**
     * 非数字
     */
    public static final java.util.regex.Pattern NOT_NUMBERS_PATTERN = java.util.regex.Pattern.compile(Regex.NOT_NUMBERS);
    /**
     * 从非数字开始
     */
    public static final java.util.regex.Pattern WITH_NOT_NUMBERS_PATTERN = java.util.regex.Pattern.compile(Regex.WITH_NOT_NUMBERS);
    /**
     * 空格
     */
    public static final java.util.regex.Pattern SPACES_PATTERN = java.util.regex.Pattern.compile(Regex.SPACES);
    /**
     * 空格冒号空格
     */
    public static final java.util.regex.Pattern SPACES_COLON_SPACE_PATTERN = java.util.regex.Pattern.compile(Regex.SPACES_COLON_SPACE);
    /**
     * 用于检查十六进制字符串的有效性
     */
    public static final java.util.regex.Pattern VALID_HEX_PATTERN = java.util.regex.Pattern.compile(Regex.VALID_HEX);
    /**
     * 单个中文汉字
     * 参照维基百科汉字Unicode范围(<a href="https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97">https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97</a> 页面右侧)
     */
    public static final java.util.regex.Pattern CHINESE_PATTERN = java.util.regex.Pattern.compile(Regex.CHINESE);
    /**
     * 中文汉字
     */
    public static final java.util.regex.Pattern CHINESES_PATTERN = java.util.regex.Pattern.compile(Regex.CHINESES);
    /**
     * 分组
     */
    public static final java.util.regex.Pattern GROUP_VAR_PATTERN = java.util.regex.Pattern.compile(Regex.GROUP_VAR);
    /**
     * 快速区分IP地址和主机名
     */
    public static final java.util.regex.Pattern IP_ADDRESS_PATTERN = java.util.regex.Pattern.compile(Regex.IP_ADDRESS);
    /**
     * IP v4
     * 采用分组方式便于解析地址的每一个段
     */
    public static final java.util.regex.Pattern IPV4_PATTERN = java.util.regex.Pattern.compile(Regex.IPV4);
    /**
     * IP v6
     */
    public static final java.util.regex.Pattern IPV6_PATTERN = java.util.regex.Pattern.compile(Regex.IPV6);
    /**
     * 货币
     */
    public static final java.util.regex.Pattern MONEY_PATTERN = java.util.regex.Pattern.compile(Regex.MONEY);
    /**
     * 邮件，符合RFC 5322规范，注意email 要宽松一点
     * 正则来自：<a href="http://emailregex.com/">http://emailregex.com/</a>
     * 参考：
     * <ul>
     *     <li>https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address/44317754</li>
     *     <li>https://stackoverflow.com/questions/201323/how-can-i-validate-an-email-address-using-a-regular-expression</li>
     * </ul>
     */
    public static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(Regex.EMAIL, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * 移动电话
     * eg: 中国大陆： +86 180 5690 2500，2位区域码标示+13位数字
     */
    public static final java.util.regex.Pattern MOBILE_PATTERN = java.util.regex.Pattern.compile(Regex.MOBILE);
    /**
     * 中国香港移动电话
     * eg: 中国香港： +852 5100 6590， 三位区域码+10位数字, 中国香港手机号码8位数
     */
    public static final java.util.regex.Pattern MOBILE_HK_PATTERN = java.util.regex.Pattern.compile(Regex.MOBILE_HK);
    /**
     * 中国台湾移动电话
     * eg: 中国台湾： +886 09 60 000000， 三位区域码+号码以数字09开头 + 8位数字, 中国台湾手机号码10位数
     */
    public static final java.util.regex.Pattern MOBILE_TW_PATTERN = java.util.regex.Pattern.compile(Regex.MOBILE_TW);
    /**
     * 中国澳门移动电话
     * eg: 中国澳门： +853 68 00000， 三位区域码 +号码以数字6开头 + 7位数字, 中国台湾手机号码8位数
     */
    public static final java.util.regex.Pattern MOBILE_MO_PATTERN = java.util.regex.Pattern.compile(Regex.MOBILE_MO);
    /**
     * 座机号码
     */
    public static final java.util.regex.Pattern TEL_PATTERN = java.util.regex.Pattern.compile(Regex.TEL);
    /**
     * 座机号码+400+800电话
     *
     * @see <a href="https://baike.baidu.com/item/800">800</a>
     */
    public static final java.util.regex.Pattern TEL_400_800_PATTERN = java.util.regex.Pattern.compile(Regex.TEL_400_800);
    /**
     * 18位身份证号码
     */
    public static final java.util.regex.Pattern CITIZEN_ID_PATTERN = java.util.regex.Pattern.compile(Regex.CITIZEN_ID);
    /**
     * 邮编，兼容港澳台
     */
    public static final java.util.regex.Pattern ZIP_CODE_PATTERN = java.util.regex.Pattern.compile(Regex.ZIP_CODE);
    /**
     * 生日
     */
    public static final java.util.regex.Pattern BIRTHDAY_PATTERN = java.util.regex.Pattern.compile(Regex.BIRTHDAY);
    /**
     * URI
     * 定义见：<a href="https://www.ietf.org/rfc/rfc3986.html#appendix-B">https://www.ietf.org/rfc/rfc3986.html#appendix-B</a>
     */
    public static final java.util.regex.Pattern URI_PATTERN = java.util.regex.Pattern.compile(Regex.URI);
    /**
     * URL
     */
    public static final java.util.regex.Pattern URL_PATTERN = java.util.regex.Pattern.compile(Regex.URL);
    /**
     * Protocol URL（来自：<a href="http://urlregex.com/">http://urlregex.com/</a>）
     * 此正则同时支持FTP、File等协议的URL
     */
    public static final java.util.regex.Pattern URL_HTTP_PATTERN = java.util.regex.Pattern.compile(Regex.URL_HTTP, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * 中文字、英文字母、数字和下划线
     */
    public static final java.util.regex.Pattern GENERAL_WITH_CHINESE_PATTERN = java.util.regex.Pattern.compile(Regex.GENERAL_WITH_CHINESE);
    /**
     * UUID
     */
    public static final java.util.regex.Pattern UUID_PATTERN = java.util.regex.Pattern.compile(Regex.UUID, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * 不带横线的UUID
     */
    public static final java.util.regex.Pattern UUID_SIMPLE_PATTERN = java.util.regex.Pattern.compile(Regex.UUID_SIMPLE);
    /**
     * MAC地址正则
     */
    public static final java.util.regex.Pattern MAC_ADDRESS_PATTERN = java.util.regex.Pattern.compile(Regex.MAC_ADDRESS, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * 16进制字符串
     */
    public static final java.util.regex.Pattern HEX_PATTERN = java.util.regex.Pattern.compile(Regex.HEX);
    /**
     * 时间正则
     */
    public static final java.util.regex.Pattern TIME_PATTERN = java.util.regex.Pattern.compile(Regex.TIME);
    /**
     * 中国车牌号码（兼容新能源车牌）
     */
    public static final java.util.regex.Pattern PLATE_NUMBER_PATTERN = java.util.regex.Pattern.compile(Regex.PLATE_NUMBER);

    /**
     * 统一社会信用代码
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     */
    public static final java.util.regex.Pattern CREDIT_CODE_PATTERN = java.util.regex.Pattern.compile(Regex.CREDIT_CODE);
    /**
     * 车架号（车辆识别代号由世界制造厂识别代号(WMI、车辆说明部分(VDS)车辆指示部分(VIS)三部分组成，共 17 位字码。）
     * 别名：车辆识别代号、车辆识别码、车架号、十七位码
     * 标准号：GB 16735-2019
     * 标准官方地址：https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=E2EBF667F8C032B1EDFD6DF9C1114E02
     * 对年产量大于或等于1 000 辆的完整车辆和/或非完整车辆制造厂：
     * <pre>
     *   第一部分为世界制造厂识别代号(WMI)，3位
     *   第二部分为车辆说明部分(VDS)，     6位
     *   第三部分为车辆指示部分(VIS)，     8位
     * </pre>
     * <p>
     * 对年产量小于 1 000 辆的完整车辆和/或非完整车辆制造厂：
     * <pre>
     *   第一部分为世界制造广识别代号(WMI),3位;
     *   第二部分为车辆说明部分(VDS)，6位;
     *   第三部分的三、四、五位与第一部分的三位字码起构成世界制造厂识别代号(WMI),其余五位为车辆指示部分(VIS)，8位。
     * </pre>
     *
     * <pre>
     *   eg:LDC210P23A1306189
     *   eg:LSJA24U61JG269201
     *   eg:LBV5S3102ESJ20935
     * </pre>
     */
    public static final java.util.regex.Pattern CAR_VIN_PATTERN = java.util.regex.Pattern.compile(Regex.CAR_VIN);

    /**
     * 驾驶证  别名：驾驶证档案编号、行驶证编号
     * eg:530201950258
     * 12位数字字符串
     * 仅限：中国驾驶证档案编号
     */
    public static final java.util.regex.Pattern CAR_DRIVING_LICENCE_PATTERN = java.util.regex.Pattern.compile(Regex.CAR_DRIVING_LICENCE);
    /**
     * 中文姓名
     * 维吾尔族姓名里面的点是 · 输入法中文状态下，键盘左上角数字1前面的那个符号；
     * 错误字符：{@code ．.。．.}
     * 正确维吾尔族姓名：
     * <pre>
     * 霍加阿卜杜拉·麦提喀斯木
     * 玛合萨提别克·哈斯木别克
     * 阿布都热依木江·艾斯卡尔
     * 阿卜杜尼亚孜·毛力尼亚孜
     * </pre>
     * <pre>
     * ----------
     * 错误示例：大  小                reason: 有空格
     * 错误示例：乐逍遥0               reason: 数字
     * 错误示例：依帕古丽-艾则孜        reason: 特殊符号
     * 错误示例：牙力空.买提萨力        reason: 新疆人的点不对
     * 错误示例：王二小2002-3-2        reason: 有数字、特殊符号
     * 错误示例：霍金(科学家）          reason: 有括号
     * 错误示例：寒冷:冬天             reason: 有特殊符号
     * 错误示例：大                   reason: 少于2位
     * ----------
     * </pre>
     * 总结中文姓名：2-60位，只能是中文和维吾尔族的点·
     * 放宽汉字范围：如生僻字
     */
    public static final java.util.regex.Pattern CHINESE_NAME_PATTERN = java.util.regex.Pattern.compile(Regex.CHINESE_NAME);

    /**
     * Pattern池
     */
    private static final WeakConcurrentMap<RegexWithFlag, java.util.regex.Pattern> POOL = new WeakConcurrentMap<>();

    /**
     * 先从Pattern池中查找正则对应的{@link java.util.regex.Pattern}，找不到则编译正则表达式并入池。
     *
     * @param regex 正则表达式
     * @return {@link java.util.regex.Pattern}
     */
    public static java.util.regex.Pattern get(final String regex) {
        return get(regex, 0);
    }

    /**
     * 先从Pattern池中查找正则对应的{@link java.util.regex.Pattern}，找不到则编译正则表达式并入池。
     *
     * @param regex 正则表达式
     * @param flags 正则标识位集合 {@link java.util.regex.Pattern}
     * @return {@link java.util.regex.Pattern}
     */
    public static java.util.regex.Pattern get(final String regex, final int flags) {
        final RegexWithFlag regexWithFlag = new RegexWithFlag(regex, flags);
        return POOL.computeIfAbsent(regexWithFlag, (key) -> java.util.regex.Pattern.compile(regex, flags));
    }

    /**
     * 移除缓存
     *
     * @param regex 正则
     * @param flags 标识
     * @return 移除的{@link Pattern}，可能为{@code null}
     */
    public static java.util.regex.Pattern remove(final String regex, final int flags) {
        return POOL.remove(new RegexWithFlag(regex, flags));
    }

    /**
     * 清空缓存池
     */
    public static void clear() {
        POOL.clear();
    }

}
