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
package org.miaixz.bus.pay;

/**
 * 金融机构 排名不分先后，以三方支付服务商给出的数据为准。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Banking {

    /**
     * 招商银行
     */
    BK_1001("1001"),
    /**
     * 工商银行
     */
    BK_1002("1002"),
    /**
     * 建设银行
     */
    BK_1003("1003"),
    /**
     * 浦发银行
     */
    BK_1004("1004"),
    /**
     * 农业银行
     */
    BK_1005("1005"),
    /**
     * 民生银行
     */
    BK_1006("1006"),
    /**
     * 兴业银行
     */
    BK_1009("1009"),
    /**
     * 平安银行
     */
    BK_1010("1010"),
    /**
     * 交通银行
     */
    BK_1020("1020"),
    /**
     * 中信银行
     */
    BK_1021("1021"),
    /**
     * 光大银行
     */
    BK_1022("1022"),
    /**
     * 上海银行
     */
    BK_1024("1024"),
    /**
     * 华夏银行
     */
    BK_1025("1025"),
    /**
     * 中国银行
     */
    BK_1026("1026"),

    /**
     * 广发银行
     */
    BK_1027("1027"),
    /**
     * 南京银行
     */
    BK_1054("1054"),
    /**
     * 宁波银行
     */
    BK_1056("1056"),
    /**
     * 邮储银行
     */
    BK_1066("1066"),
    /**
     * 顺德农商银行
     */
    BK_4036("4036"),
    /**
     * 浙江泰隆商业银行
     */
    BK_4051("4051"),
    /**
     * 宁波鄞州农村商业银行
     */
    BK_4052("4052"),
    /**
     * 江苏紫金农村商业银行股份有限公司
     */
    BK_4072("4072"),
    /**
     * 深圳农村商业银行
     */
    BK_4076("4076"),
    /**
     * 陕西省农村信用社
     */
    BK_4108("4108"),
    /**
     * 广西壮族自治区农村信用社
     */
    BK_4113("4113"),
    /**
     * 河南省农村信用社
     */
    BK_4115("4115"),
    /**
     * 宁夏黄河农村商业银行
     */
    BK_4150("4150"),
    /**
     * 天津农村商业银行
     */
    BK_4153("4153"),
    /**
     * 山西省农村信用社
     */
    BK_4156("4156"),
    /**
     * 甘肃省农村信用社
     */
    BK_4157("4157"),
    /**
     * 安徽省农村信用社
     */
    BK_4166("4166"),
    /**
     * 长沙银行
     */
    BK_4216("4216"),
    /**
     * 江苏省农村信用社联合社
     */
    BK_4217("4217"),
    /**
     * 衡水银行
     */
    BK_4752("4752"),
    /**
     * 中原银行
     */
    BK_4753("4753"),
    /**
     * 长子县融汇村镇银行
     */
    BK_4755("4755"),
    /**
     * 长治银行
     */
    BK_4756("4756"),
    /**
     * 海口联合农村商业银行股份有限公司
     */
    BK_4758("4758"),
    /**
     * 企业银行（中国）
     */
    BK_4761("4761"),
    /**
     * 南洋商业银行（中国）有限公司
     */
    BK_4763("4763"),
    /**
     * 浙江省农村信用社联合社
     */
    BK_4764("4764"),
    /**
     * 枣庄银行股份有限公司
     */
    BK_4766("4766"),
    /**
     * 大同银行
     */
    BK_4767("4767"),
    /**
     * 北京中关村银行股份有限公司
     */
    BK_4769("4769"),
    /**
     * 星展银行（中国）有限公司
     */
    BK_4778("4778"),
    /**
     * 北京银行
     */
    BK_4836("4836");

    private final String code;

    Banking(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

}
