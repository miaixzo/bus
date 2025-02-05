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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * 微信支付 v3 版本接口-电子发票接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum InvoiceApi implements Matcher {

    /**
     * 创建电子发票卡券模板
     */
    CARD_TEMPLATE("/v3/new-tax-control-fapiao/card-template", "创建电子发票卡券模板"),

    /**
     * 开具电子发票
     */
    INVOICING("/v3/new-tax-control-fapiao/fapiao-applications", "开具电子发票"),

    /**
     * 上传电子发票文件
     */
    UPDATE_INVOICE_FILE("/v3/new-tax-control-fapiao/fapiao-applications/upload-fapiao-file", "上传电子发票文件"),

    /**
     * 查询电子发票
     */
    QUERY_INVOICE("/v3/new-tax-control-fapiao/fapiao-applications/%s", "查询电子发票"),

    /**
     * 获取发票下载信息
     */
    QUERY_INVOICE_DOWNLOAD_INFO("/v3/new-tax-control-fapiao/fapiao-applications/%s/fapiao-files", "获取发票下载信息"),

    /**
     * 将电子发票插入微信用户卡包
     */
    INSERT_CARDS("/v3/new-tax-control-fapiao/fapiao-applications/%s/insert-cards", "将电子发票插入微信用户卡包"),

    /**
     * 冲红电子发票
     */
    REVERSE("/v3/new-tax-control-fapiao/fapiao-applications/%s/reverse", "冲红电子发票"),

    /**
     * 获取商户开票基础信息
     */
    MERCHANT_BASE_INFO("/v3/new-tax-control-fapiao/merchant/base-information", "获取商户开票基础信息"),

    /**
     * 查询商户配置的开发选项/配置开发选项
     */
    MERCHANT_DEVELOPMENT_CONFIG("/v3/new-tax-control-fapiao/merchant/development-config", "查询商户配置的开发选项/配置开发选项"),

    /**
     * 获取商户可开具的商品和服务税收分类编码对照表
     */
    MERCHANT_TAX_CODES("/v3/new-tax-control-fapiao/merchant/tax-codes", "获取商户可开具的商品和服务税收分类编码对照表"),

    /**
     * 获取用户填写的抬头
     */
    USER_TITLE("/v3/new-tax-control-fapiao/user-title", "获取用户填写的抬头"),

    /**
     * 获取抬头填写链接
     */
    USER_TITLE_URL("/v3/new-tax-control-fapiao/user-title/title-url", "获取抬头填写链接");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    InvoiceApi(String method, String desc) {
        this.method = method;
        this.desc = desc;
    }

    /**
     * 交易类型
     *
     * @return the string
     */
    @Override
    public String type() {
        return this.name();
    }

    /**
     * 类型描述
     *
     * @return the string
     */
    @Override
    public String desc() {
        return this.desc;
    }

    /**
     * 接口方法
     *
     * @return the string
     */
    @Override
    public String method() {
        return this.method;
    }

}
