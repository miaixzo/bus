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
package org.miaixz.bus.pay.metric.alipay;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.metric.AbstractProvider;

/**
 * 支付宝支付相关接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliPayProvider extends AbstractProvider<Material, Context> {

    private static final String CHARSET_UTF8 = "UTF-8";

    public AliPayProvider(Context context) {
        this(context, null);
    }

    public AliPayProvider(Context context, Complex complex) {
        this(context, complex, null);
    }

    public AliPayProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
        Assert.notBlank(this.context.getAppId(), "[appId] not defined");
        Assert.notBlank(this.context.getPrivateKey(), "[privateKey] not defined");
        Assert.notBlank(this.context.getPublicKey(), "[publicKey] not defined");
    }

    /**
     * 获取接口请求的 URL
     *
     * @return 返回完整的接口请求URL
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * 获取接口请求的 URL
     *
     * @param complex 支付 API 接口枚举
     * @return 返回完整的接口请求URL
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.ALIPAY.sandbox() : Registry.ALIPAY.service()).concat(complex.method());
    }

    /**
     * APP支付
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知 URL
     * @return 签名后的请求参数字符串
     */
    public String appPay(Map<String, String> model, String notifyUrl) {
        return buildAppRequest(model, notifyUrl, null, "alipay.trade.app.pay");
    }

    /**
     * APP支付（带应用授权）
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知 URL
     * @param appAuthToken 应用授权token
     * @return 签名后的请求参数字符串
     */
    public String appPayWithToken(Map<String, String> model, String notifyUrl, String appAuthToken) {
        return buildAppRequest(model, notifyUrl, appAuthToken, "alipay.trade.app.pay");
    }

    /**
     * WAP支付
     *
     * @param model     请求参数
     * @param returnUrl 同步通知URL
     * @param notifyUrl 异步通知URL
     * @return WAP支付的HTML表单
     */
    public String wapPay(Map<String, String> model, String returnUrl, String notifyUrl) {
        return buildWapPay(model, returnUrl, notifyUrl, null);
    }

    /**
     * WAP支付（带应用授权）
     *
     * @param model        请求参数
     * @param returnUrl    同步通知URL
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return WAP支付的HTML表单
     */
    public String wapPayWithToken(Map<String, String> model, String returnUrl, String notifyUrl, String appAuthToken) {
        return buildWapPay(model, returnUrl, notifyUrl, appAuthToken);
    }

    /**
     * WAP支付（OutputStream兼容）
     *
     * @param model     请求参数
     * @param returnUrl 同步通知URL
     * @param notifyUrl 异步通知URL
     * @return WAP支付的HTML表单
     */
    public String wapPayByOutput(Map<String, String> model, String returnUrl, String notifyUrl) {
        return buildWapPay(model, returnUrl, notifyUrl, null);
    }

    /**
     * WAP支付（OutputStream兼容，带应用授权）
     *
     * @param model        请求参数
     * @param returnUrl    同步通知URL
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return WAP支付的HTML表单
     */
    public String wapPayByOutputWithToken(Map<String, String> model, String returnUrl, String notifyUrl,
            String appAuthToken) {
        return buildWapPay(model, returnUrl, notifyUrl, appAuthToken);
    }

    /**
     * 统一收单交易支付（条形码、声波支付）
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @return API响应
     */
    public Map<String, Object> tradePay(Map<String, String> model, String notifyUrl) {
        return executeRequest(model, notifyUrl, null, "alipay.trade.pay");
    }

    /**
     * 统一收单交易支付（证书模式）
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @return API响应
     */
    public Map<String, Object> tradePayWithCert(Map<String, String> model, String notifyUrl) {
        return executeRequest(true, model, notifyUrl, null, "alipay.trade.pay");
    }

    /**
     * 统一收单交易支付（带应用授权）
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradePayWithToken(Map<String, String> model, String notifyUrl, String appAuthToken) {
        return executeRequest(model, notifyUrl, appAuthToken, "alipay.trade.pay");
    }

    /**
     * 统一收单交易支付（证书模式，带应用授权）
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradePayWithCertAndToken(Map<String, String> model, String notifyUrl,
            String appAuthToken) {
        return executeRequest(true, model, notifyUrl, appAuthToken, "alipay.trade.pay");
    }

    /**
     * 统一收单线下交易预创建（扫码支付）
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @return API响应
     */
    public Map<String, Object> tradePrecreate(Map<String, String> model, String notifyUrl) {
        return executeRequest(model, notifyUrl, null, "alipay.trade.precreate");
    }

    /**
     * 统一收单线下交易预创建（证书模式）
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @return API响应
     */
    public Map<String, Object> tradePrecreateWithCert(Map<String, String> model, String notifyUrl) {
        return executeRequest(true, model, notifyUrl, null, "alipay.trade.precreate");
    }

    /**
     * 统一收单线下交易预创建（带应用授权）
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradePrecreateWithToken(Map<String, String> model, String notifyUrl,
            String appAuthToken) {
        return executeRequest(model, notifyUrl, appAuthToken, "alipay.trade.precreate");
    }

    /**
     * 统一收单线下交易预创建（证书模式，带应用授权）
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradePrecreateWithCertAndToken(Map<String, String> model, String notifyUrl,
            String appAuthToken) {
        return executeRequest(true, model, notifyUrl, appAuthToken, "alipay.trade.precreate");
    }

    /**
     * 单笔转账到支付宝账户
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> transfer(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.trans.toaccount.transfer");
    }

    /**
     * 单笔转账到支付宝账户（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> transferWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.trans.toaccount.transfer");
    }

    /**
     * 转账查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> transferQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.trans.order.query");
    }

    /**
     * 转账查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> transferQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.trans.order.query");
    }

    /**
     * 统一转账
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> uniTransfer(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.fund.trans.uni.transfer");
    }

    /**
     * 统一转账（证书模式）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> uniTransferWithCert(Map<String, String> model, String appAuthToken) {
        return executeRequest(true, model, null, appAuthToken, "alipay.fund.trans.uni.transfer");
    }

    /**
     * 转账业务单据查询
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> transCommonQuery(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.fund.trans.common.query");
    }

    /**
     * 转账业务单据查询（证书模式）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> transCommonQueryWithCert(Map<String, String> model, String appAuthToken) {
        return executeRequest(true, model, null, appAuthToken, "alipay.fund.trans.common.query");
    }

    /**
     * 支付宝资金账户资产查询
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> accountQuery(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.fund.account.query");
    }

    /**
     * 支付宝资金账户资产查询（证书模式）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> accountQueryWithCert(Map<String, String> model, String appAuthToken) {
        return executeRequest(true, model, null, appAuthToken, "alipay.fund.account.query");
    }

    /**
     * 统一收单线下交易查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.query");
    }

    /**
     * 统一收单线下交易查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.query");
    }

    /**
     * 统一收单线下交易查询（带应用授权）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradeQueryWithToken(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.trade.query");
    }

    /**
     * 统一收单交易撤销
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradeCancel(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.trade.cancel");
    }

    /**
     * 统一收单交易撤销
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeCancelSimple(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.cancel");
    }

    /**
     * 统一收单交易撤销（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeCancelWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.cancel");
    }

    /**
     * 统一收单交易关闭
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradeClose(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.trade.close");
    }

    /**
     * 统一收单交易关闭
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeCloseSimple(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.close");
    }

    /**
     * 统一收单交易关闭（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeCloseWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.close");
    }

    /**
     * 统一收单交易创建
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @return API响应
     */
    public Map<String, Object> tradeCreate(Map<String, String> model, String notifyUrl) {
        return executeRequest(model, notifyUrl, null, "alipay.trade.create");
    }

    /**
     * 统一收单交易创建（证书模式）
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @return API响应
     */
    public Map<String, Object> tradeCreateWithCert(Map<String, String> model, String notifyUrl) {
        return executeRequest(true, model, notifyUrl, null, "alipay.trade.create");
    }

    /**
     * 统一收单交易创建（带应用授权）
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradeCreateWithToken(Map<String, String> model, String notifyUrl, String appAuthToken) {
        return executeRequest(model, notifyUrl, appAuthToken, "alipay.trade.create");
    }

    /**
     * 统一收单交易退款
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRefund(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.refund");
    }

    /**
     * 统一收单交易退款（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRefundWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.refund");
    }

    /**
     * 统一收单交易退款（带应用授权）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradeRefundWithToken(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.trade.refund");
    }

    /**
     * 统一收单退款页面
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradePageRefund(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.page.refund");
    }

    /**
     * 统一收单退款页面（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradePageRefundWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.page.refund");
    }

    /**
     * 统一收单退款页面（带应用授权）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradePageRefundWithToken(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.trade.page.refund");
    }

    /**
     * 统一收单交易退款查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRefundQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.fastpay.refund.query");
    }

    /**
     * 统一收单交易退款查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRefundQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.fastpay.refund.query");
    }

    /**
     * 统一收单交易退款查询（带应用授权）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradeRefundQueryWithToken(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.trade.fastpay.refund.query");
    }

    /**
     * 查询对账单下载地址
     *
     * @param model 请求参数
     * @return 对账单下载URL
     */
    public String billDownloadUrl(Map<String, String> model) {
        Map<String, Object> response = executeRequest(model, null, null,
                "alipay.data.dataservice.bill.downloadurl.query");
        return (String) response.get("bill_download_url");
    }

    /**
     * 查询对账单下载地址（证书模式）
     *
     * @param model 请求参数
     * @return 对账单下载URL
     */
    public String billDownloadUrlWithCert(Map<String, String> model) {
        Map<String, Object> response = executeRequest(true, model, null, null,
                "alipay.data.dataservice.bill.downloadurl.query");
        return (String) response.get("bill_download_url");
    }

    /**
     * 查询对账单下载地址
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> billDownloadUrlQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.data.dataservice.bill.downloadurl.query");
    }

    /**
     * 查询对账单下载地址（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> billDownloadUrlQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.data.dataservice.bill.downloadurl.query");
    }

    /**
     * 查询对账单下载地址（带应用授权）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> billDownloadUrlQueryWithToken(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.data.dataservice.bill.downloadurl.query");
    }

    /**
     * 查询对账单下载地址（证书模式，带应用授权）
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> billDownloadUrlQueryWithCertAndToken(Map<String, String> model, String appAuthToken) {
        return executeRequest(true, model, null, appAuthToken, "alipay.data.dataservice.bill.downloadurl.query");
    }

    /**
     * 统一收单交易结算
     *
     * @param model        请求参数
     * @param appAuthToken 应用授权token
     * @return API响应
     */
    public Map<String, Object> tradeOrderSettle(Map<String, String> model, String appAuthToken) {
        return executeRequest(model, null, appAuthToken, "alipay.trade.order.settle");
    }

    /**
     * 统一收单交易结算
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeOrderSettleSimple(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.order.settle");
    }

    /**
     * 统一收单交易结算（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeOrderSettleWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.order.settle");
    }

    /**
     * 电脑网站支付（PC支付）
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @param returnUrl 同步通知URL
     * @return HTML表单
     */
    public String tradePage(Map<String, String> model, String notifyUrl, String returnUrl) {
        return buildPagePay("POST", model, notifyUrl, returnUrl, null);
    }

    /**
     * 电脑网站支付（PC支付，指定方法）
     *
     * @param method    GET/POST
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @param returnUrl 同步通知URL
     * @return HTML表单或URL
     */
    public String tradePageWithMethod(String method, Map<String, String> model, String notifyUrl, String returnUrl) {
        return buildPagePay(method, model, notifyUrl, returnUrl, null);
    }

    /**
     * 电脑网站支付（PC支付，带应用授权）
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param returnUrl    同步通知URL
     * @param appAuthToken 应用授权token
     * @return HTML表单
     */
    public String tradePageWithToken(Map<String, String> model, String notifyUrl, String returnUrl,
            String appAuthToken) {
        return buildPagePay("POST", model, notifyUrl, returnUrl, appAuthToken);
    }

    /**
     * 电脑网站支付（PC支付，OutputStream兼容）
     *
     * @param model     请求参数
     * @param notifyUrl 异步通知URL
     * @param returnUrl 同步通知URL
     * @return HTML表单
     * @throws IOException IO异常
     */
    public String tradePageByOutput(Map<String, String> model, String notifyUrl, String returnUrl) throws IOException {
        return buildPagePay("POST", model, notifyUrl, returnUrl, null);
    }

    /**
     * 电脑网站支付（PC支付，OutputStream兼容，带应用授权）
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param returnUrl    同步通知URL
     * @param appAuthToken 应用授权token
     * @return HTML表单
     * @throws IOException IO异常
     */
    public String tradePageByOutputWithToken(Map<String, String> model, String notifyUrl, String returnUrl,
            String appAuthToken) throws IOException {
        return buildPagePay("POST", model, notifyUrl, returnUrl, appAuthToken);
    }

    /**
     * 资金预授权冻结
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOrderFreeze(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.order.freeze");
    }

    /**
     * 资金预授权冻结（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOrderFreezeWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.order.freeze");
    }

    /**
     * 资金授权解冻
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOrderUnfreeze(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.order.unfreeze");
    }

    /**
     * 资金授权解冻（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOrderUnfreezeWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.order.unfreeze");
    }

    /**
     * 资金预授权凭证创建
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOrderVoucherCreate(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.order.voucher.create");
    }

    /**
     * 资金预授权凭证创建（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOrderVoucherCreateWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.order.voucher.create");
    }

    /**
     * 资金授权撤销
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOperationCancel(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.operation.cancel");
    }

    /**
     * 资金授权撤销（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOperationCancelWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.operation.cancel");
    }

    /**
     * 资金授权操作查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOperationDetailQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.operation.detail.query");
    }

    /**
     * 资金授权操作查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authOperationDetailQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.operation.detail.query");
    }

    /**
     * 红包无线支付
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderAppPay(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.app.pay");
    }

    /**
     * 红包无线支付（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderAppPayWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.app.pay");
    }

    /**
     * 红包页面支付
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderPagePay(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.page.pay");
    }

    /**
     * 红包页面支付（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderPagePayWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.page.pay");
    }

    /**
     * 红包协议支付
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderAgreementPay(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.agreement.pay");
    }

    /**
     * 红包协议支付（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderAgreementPayWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.agreement.pay");
    }

    /**
     * 红包打款
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderDisburse(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.disburse");
    }

    /**
     * 红包打款（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderDisburseWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.disburse");
    }

    /**
     * 红包退回
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderRefund(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.refund");
    }

    /**
     * 红包退回（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOrderRefundWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.refund");
    }

    /**
     * 红包查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOperationQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.operation.query");
    }

    /**
     * 红包查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> fundCouponOperationQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.operation.query");
    }

    /**
     * 应用授权URL拼装
     *
     * @param appId       应用编号
     * @param redirectUri 回调URI
     * @return 应用授权URL
     * @throws java.io.UnsupportedEncodingException 编码异常
     */
    public String getOauth2Url(String appId, String redirectUri) throws java.io.UnsupportedEncodingException {
        return new StringBuffer().append("https://openauth.alipay.com/oauth2/appToAppAuth.htm?app_id=").append(appId)
                .append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8")).toString();
    }

    /**
     * 使用app_auth_code换取app_auth_token
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> openAuthTokenApp(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.open.auth.token.app");
    }

    /**
     * 使用app_auth_code换取app_auth_token（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> openAuthTokenAppWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.open.auth.token.app");
    }

    /**
     * 查询授权信息
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> openAuthTokenAppQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.open.auth.token.app.query");
    }

    /**
     * 查询授权信息（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> openAuthTokenAppQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.open.auth.token.app.query");
    }

    /**
     * 地铁购票发码
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> voucherGenerate(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.commerce.cityfacilitator.voucher.generate");
    }

    /**
     * 地铁购票发码（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> voucherGenerateWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.commerce.cityfacilitator.voucher.generate");
    }

    /**
     * 地铁购票发码退款
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> metroRefund(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.commerce.cityfacilitator.voucher.refund");
    }

    /**
     * 地铁购票发码退款（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> metroRefundWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.commerce.cityfacilitator.voucher.refund");
    }

    /**
     * 地铁车站数据查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> stationQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.commerce.cityfacilitator.station.query");
    }

    /**
     * 地铁车站数据查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> stationQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.commerce.cityfacilitator.station.query");
    }

    /**
     * 核销码批量查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> voucherBatchQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.commerce.cityfacilitator.voucher.batchquery");
    }

    /**
     * 核销码批量查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> voucherBatchQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.commerce.cityfacilitator.voucher.batchquery");
    }

    /**
     * 批量转账
     *
     * @param params     请求参数
     * @param privateKey 私钥
     * @param signType   签名类型
     * @return 签名后的参数
     */
    public Map<String, String> batchTrans(Map<String, String> params, String privateKey, String signType) {
        params.put("service", "batch_trans_notify");
        params.put("_input_charset", "UTF-8");
        params.put("pay_date", DateKit.format(new Date(), "YYYYMMDD"));
        return AliPayBuilder.buildRequestPara(params, privateKey, signType);
    }

    /**
     * 生活缴费查询账单
     *
     * @param orderType       支付宝订单类型
     * @param merchantOrderNo 业务流水号
     * @return API响应
     */
    public Map<String, Object> ebppBillGet(String orderType, String merchantOrderNo) {
        Map<String, String> model = new HashMap<>();
        model.put("order_type", orderType);
        model.put("merchant_order_no", merchantOrderNo);
        return executeRequest(model, null, null, "alipay.ebpp.bill.get");
    }

    /**
     * 生活缴费查询账单（证书模式）
     *
     * @param orderType       支付宝订单类型
     * @param merchantOrderNo 业务流水号
     * @return API响应
     */
    public Map<String, Object> ebppBillGetWithCert(String orderType, String merchantOrderNo) {
        Map<String, String> model = new HashMap<>();
        model.put("order_type", orderType);
        model.put("merchant_order_no", merchantOrderNo);
        return executeRequest(true, model, null, null, "alipay.ebpp.bill.get");
    }

    /**
     * H5刷脸认证初始化
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> identificationUserWebInit(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.identification.user.web.initialize");
    }

    /**
     * H5刷脸认证初始化（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> identificationUserWebInitWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.identification.user.web.initialize");
    }

    /**
     * H5刷脸认证查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> identificationUserWebQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.identification.user.web.query");
    }

    /**
     * H5刷脸认证查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> identificationUserWebQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.identification.user.web.query");
    }

    /**
     * 人脸入库
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationCustomerFaceManageCreate(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.customer.facemanage.create");
    }

    /**
     * 人脸入库（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationCustomerFaceManageCreateWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.customer.facemanage.create");
    }

    /**
     * 人脸出库
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationCustomerFaceManageDelete(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.customer.facemanage.delete");
    }

    /**
     * 人脸出库（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationCustomerFaceManageDeleteWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.customer.facemanage.delete");
    }

    /**
     * 人脸ftoken查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationCustomerFtokenQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.customer.ftoken.query");
    }

    /**
     * 人脸ftoken查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationCustomerFtokenQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.customer.ftoken.query");
    }

    /**
     * 人脸初始化刷脸付
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationSmilePayInitialize(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.smilepay.initialize");
    }

    /**
     * 人脸初始化刷脸付（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationSmilePayInitializeWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.smilepay.initialize");
    }

    /**
     * 人脸初始化唤起zim
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationCustomerSmilePayInitialize(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.customer.smilepay.initialize");
    }

    /**
     * 人脸初始化唤起zim（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> authenticationCustomerSmilePayInitializeWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.customer.smilepay.initialize");
    }

    /**
     * 生态激励项目ISV代签约
     *
     * @return API响应
     */
    public Map<String, Object> commerceAdContractSign() {
        return executeRequest(new HashMap<>(), null, null, "alipay.commerce.ad.contract.sign");
    }

    /**
     * 生态激励项目ISV代签约（证书模式）
     *
     * @return API响应
     */
    public Map<String, Object> commerceAdContractSignWithCert() {
        return executeRequest(true, new HashMap<>(), null, null, "alipay.commerce.ad.contract.sign");
    }

    /**
     * 分账关系绑定
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRoyaltyRelationBind(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.royalty.relation.bind");
    }

    /**
     * 分账关系绑定（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRoyaltyRelationBindWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.royalty.relation.bind");
    }

    /**
     * 分账关系解绑
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRoyaltyRelationUnbind(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.royalty.relation.unbind");
    }

    /**
     * 分账关系解绑（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRoyaltyRelationUnbindWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.royalty.relation.unbind");
    }

    /**
     * 分账关系查询
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRoyaltyRelationBatchQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.royalty.relation.batchquery");
    }

    /**
     * 分账关系查询（证书模式）
     *
     * @param model 请求参数
     * @return API响应
     */
    public Map<String, Object> tradeRoyaltyRelationBatchQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.royalty.relation.batchquery");
    }

    /**
     * 构建APP支付请求
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @param method       支付宝API方法
     * @return 签名后的请求参数字符串
     */
    private String buildAppRequest(Map<String, String> model, String notifyUrl, String appAuthToken, String method) {
        Map<String, String> params = buildCommonParams(method, notifyUrl, appAuthToken);
        params.put("biz_content", JsonKit.toJsonString(model));
        params = AliPayBuilder.buildRequestPara(params, context.getPrivateKey(), Algorithm.RSA2.getValue());
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(entry.getKey()).append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * 构建WAP支付表单
     *
     * @param model        请求参数
     * @param returnUrl    同步通知URL
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return HTML表单
     */
    private String buildWapPay(Map<String, String> model, String returnUrl, String notifyUrl, String appAuthToken) {
        Map<String, String> params = buildCommonParams("alipay.trade.wap.pay", notifyUrl, appAuthToken);
        params.put("return_url", returnUrl);
        params.put("biz_content", JsonKit.toJsonString(model));
        params = AliPayBuilder.buildRequestPara(params, context.getPrivateKey(), Algorithm.RSA2.getValue());
        StringBuilder form = new StringBuilder();
        form.append("<form id='alipay_form' action='").append(getUrl()).append("' method='POST'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            form.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
                    .append(entry.getValue()).append("'/>");
        }
        form.append("</form>");
        form.append("<script>document.getElementById('alipay_form').submit();</script>");
        return form.toString();
    }

    /**
     * 构建PC支付表单或URL
     *
     * @param method       GET/POST
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param returnUrl    同步通知URL
     * @param appAuthToken 应用授权token
     * @return HTML表单或URL
     */
    private String buildPagePay(String method, Map<String, String> model, String notifyUrl, String returnUrl,
            String appAuthToken) {
        Map<String, String> params = buildCommonParams("alipay.trade.page.pay", notifyUrl, appAuthToken);
        params.put("return_url", returnUrl);
        params.put("biz_content", JsonKit.toJsonString(model));
        params = AliPayBuilder.buildRequestPara(params, context.getPrivateKey(), Algorithm.RSA2.getValue());
        if ("GET".equalsIgnoreCase(method)) {
            StringBuilder url = new StringBuilder(getUrl()).append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
            }
            return url.substring(0, url.length() - 1);
        } else {
            StringBuilder form = new StringBuilder();
            form.append("<form id='alipay_form' action='").append(getUrl()).append("' method='POST'>");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                form.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
                        .append(entry.getValue()).append("'/>");
            }
            form.append("</form>");
            form.append("<script>document.getElementById('alipay_form').submit();</script>");
            return form.toString();
        }
    }

    /**
     * 构建通用请求参数
     *
     * @param method       支付宝API方法
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return 参数Map
     */
    private Map<String, String> buildCommonParams(String method, String notifyUrl, String appAuthToken) {
        Map<String, String> params = new HashMap<>();
        params.put("method", method);
        params.put("app_id", context.getAppId());
        params.put("timestamp", DateKit.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        params.put("charset", CHARSET_UTF8);
        params.put("version", "1.0");
        if (!StringKit.isEmpty(notifyUrl)) {
            params.put("notify_url", notifyUrl);
        }
        if (!StringKit.isEmpty(appAuthToken)) {
            params.put("app_auth_token", appAuthToken);
        }
        return params;
    }

    /**
     * 执行支付宝API请求
     *
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @param method       支付宝API方法
     * @return API响应
     */
    private Map<String, Object> executeRequest(Map<String, String> model, String notifyUrl, String appAuthToken,
            String method) {
        return executeRequest(context.isCertMode(), model, notifyUrl, appAuthToken, method);
    }

    /**
     * 执行支付宝API请求
     *
     * @param certModel    是否使用证书模式
     * @param model        请求参数
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @param method       支付宝API方法
     * @return API响应
     */
    private Map<String, Object> executeRequest(Boolean certModel, Map<String, String> model, String notifyUrl,
            String appAuthToken, String method) {
        Map<String, String> params = buildCommonParams(method, notifyUrl, appAuthToken);
        params.put("biz_content", JsonKit.toJsonString(model));
        params = AliPayBuilder.buildRequestPara(params, context.getPrivateKey(), Algorithm.RSA2.getValue());

        try {
            String response = certModel ? executeCertRequest(params) : executeHttpRequest(params);
            Map<String, Object> responseMap = JsonKit.toMap(response);
            if (responseMap == null) {
                throw new RuntimeException("Failed to parse response: " + response);
            }

            // Verify signature
            String sign = (String) responseMap.get("sign");
            Map<String, String> verifyParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : responseMap.entrySet()) {
                if (!"sign".equals(entry.getKey())) {
                    verifyParams.put(entry.getKey(), entry.getValue().toString());
                }
            }
            boolean isValid = AliPayBuilder.rsaCertCheckV1ByContent(verifyParams, context.getPublicKey(), CHARSET_UTF8,
                    Algorithm.RSA2.getValue());
            if (!isValid) {
                throw new RuntimeException("Signature verification failed");
            }

            return responseMap;
        } catch (Exception e) {
            throw new RuntimeException("API request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 执行HTTP请求
     *
     * @param params 请求参数
     * @return 响应字符串
     */
    private String executeHttpRequest(Map<String, String> params) {
        return Httpx.post(getUrl(), params);
    }

    /**
     * 执行证书模式HTTP请求
     *
     * @param params 请求参数
     * @return 响应字符串
     */
    private String executeCertRequest(Map<String, String> params) {
        throw new UnsupportedOperationException("Certificate mode not implemented");
    }

}