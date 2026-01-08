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
package org.miaixz.bus.pay.metric.alipay;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Voucher;
import org.miaixz.bus.pay.metric.AbstractProvider;

/**
 * Alipay payment related interfaces
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliPayProvider extends AbstractProvider<Voucher, Context> {

    public AliPayProvider(Context context) {
        this(context, null);
    }

    public AliPayProvider(Context context, Complex complex) {
        this(context, complex, null);
    }

    public AliPayProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
        Assert.notBlank(this.context.getAppId(), "[appId] not defined");
        Assert.notBlank(this.context.getPrivateKey(), "[privateKey] not defined");
        Assert.notBlank(this.context.getPublicKey(), "[publicKey] not defined");
    }

    /**
     * Gets the API request URL
     *
     * @return the complete API request URL
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * Gets the API request URL
     *
     * @param complex the payment API interface enumeration
     * @return the complete API request URL
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.ALIPAY.sandbox() : Registry.ALIPAY.service()).concat(complex.method());
    }

    /**
     * APP payment
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @return the signed request parameter string
     */
    public String appPay(Map<String, String> model, String notifyUrl) {
        return buildAppRequest(model, notifyUrl, null, "alipay.trade.app.pay");
    }

    /**
     * APP payment with application authorization
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the signed request parameter string
     */
    public String appPayWithToken(Map<String, String> model, String notifyUrl, String authToken) {
        return buildAppRequest(model, notifyUrl, authToken, "alipay.trade.app.pay");
    }

    /**
     * WAP payment
     *
     * @param model     the request parameters
     * @param returnUrl the synchronous notification URL
     * @param notifyUrl the asynchronous notification URL
     * @return the HTML form for WAP payment
     */
    public String wapPay(Map<String, String> model, String returnUrl, String notifyUrl) {
        return buildWapPay(model, returnUrl, notifyUrl, null);
    }

    /**
     * WAP payment with application authorization
     *
     * @param model     the request parameters
     * @param returnUrl the synchronous notification URL
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the HTML form for WAP payment
     */
    public String wapPayWithToken(Map<String, String> model, String returnUrl, String notifyUrl, String authToken) {
        return buildWapPay(model, returnUrl, notifyUrl, authToken);
    }

    /**
     * WAP payment (OutputStream compatible)
     *
     * @param model     the request parameters
     * @param returnUrl the synchronous notification URL
     * @param notifyUrl the asynchronous notification URL
     * @return the HTML form for WAP payment
     */
    public String wapPayByOutput(Map<String, String> model, String returnUrl, String notifyUrl) {
        return buildWapPay(model, returnUrl, notifyUrl, null);
    }

    /**
     * WAP payment (OutputStream compatible, with application authorization)
     *
     * @param model     the request parameters
     * @param returnUrl the synchronous notification URL
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the HTML form for WAP payment
     */
    public String wapPayByOutputWithToken(
            Map<String, String> model,
            String returnUrl,
            String notifyUrl,
            String authToken) {
        return buildWapPay(model, returnUrl, notifyUrl, authToken);
    }

    /**
     * Unified payment transaction (barcode, sound wave payment)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @return the API response
     */
    public Map<String, Object> tradePay(Map<String, String> model, String notifyUrl) {
        return executeRequest(model, notifyUrl, null, "alipay.trade.pay");
    }

    /**
     * Unified payment transaction (certificate mode)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @return the API response
     */
    public Map<String, Object> tradePayWithCert(Map<String, String> model, String notifyUrl) {
        return executeRequest(true, model, notifyUrl, null, "alipay.trade.pay");
    }

    /**
     * Unified payment transaction with application authorization
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradePayWithToken(Map<String, String> model, String notifyUrl, String authToken) {
        return executeRequest(model, notifyUrl, authToken, "alipay.trade.pay");
    }

    /**
     * Unified payment transaction (certificate mode, with application authorization)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradePayWithCertAndToken(Map<String, String> model, String notifyUrl, String authToken) {
        return executeRequest(true, model, notifyUrl, authToken, "alipay.trade.pay");
    }

    /**
     * Unified offline transaction pre-create (QR code payment)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @return the API response
     */
    public Map<String, Object> tradePrecreate(Map<String, String> model, String notifyUrl) {
        return executeRequest(model, notifyUrl, null, "alipay.trade.precreate");
    }

    /**
     * Unified offline transaction pre-create (certificate mode)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @return the API response
     */
    public Map<String, Object> tradePrecreateWithCert(Map<String, String> model, String notifyUrl) {
        return executeRequest(true, model, notifyUrl, null, "alipay.trade.precreate");
    }

    /**
     * Unified offline transaction pre-create with application authorization
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradePrecreateWithToken(Map<String, String> model, String notifyUrl, String authToken) {
        return executeRequest(model, notifyUrl, authToken, "alipay.trade.precreate");
    }

    /**
     * Unified offline transaction pre-create (certificate mode, with application authorization)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradePrecreateWithCertAndToken(
            Map<String, String> model,
            String notifyUrl,
            String authToken) {
        return executeRequest(true, model, notifyUrl, authToken, "alipay.trade.precreate");
    }

    /**
     * Single transfer to Alipay account
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> transfer(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.trans.toaccount.transfer");
    }

    /**
     * Single transfer to Alipay account (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> transferWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.trans.toaccount.transfer");
    }

    /**
     * Transfer query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> transferQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.trans.order.query");
    }

    /**
     * Transfer query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> transferQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.trans.order.query");
    }

    /**
     * Unified transfer
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> uniTransfer(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.fund.trans.uni.transfer");
    }

    /**
     * Unified transfer (certificate mode)
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> uniTransferWithCert(Map<String, String> model, String authToken) {
        return executeRequest(true, model, null, authToken, "alipay.fund.trans.uni.transfer");
    }

    /**
     * Transfer business document query
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> transCommonQuery(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.fund.trans.common.query");
    }

    /**
     * Transfer business document query (certificate mode)
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> transCommonQueryWithCert(Map<String, String> model, String authToken) {
        return executeRequest(true, model, null, authToken, "alipay.fund.trans.common.query");
    }

    /**
     * Alipay fund account asset query
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> accountQuery(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.fund.account.query");
    }

    /**
     * Alipay fund account asset query (certificate mode)
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> accountQueryWithCert(Map<String, String> model, String authToken) {
        return executeRequest(true, model, null, authToken, "alipay.fund.account.query");
    }

    /**
     * Unified offline transaction query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.query");
    }

    /**
     * Unified offline transaction query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.query");
    }

    /**
     * Unified offline transaction query with application authorization
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradeQueryWithToken(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.trade.query");
    }

    /**
     * Unified transaction cancellation
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradeCancel(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.trade.cancel");
    }

    /**
     * Unified transaction cancellation (simple)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeCancelSimple(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.cancel");
    }

    /**
     * Unified transaction cancellation (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeCancelWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.cancel");
    }

    /**
     * Unified transaction close
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradeClose(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.trade.close");
    }

    /**
     * Unified transaction close (simple)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeCloseSimple(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.close");
    }

    /**
     * Unified transaction close (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeCloseWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.close");
    }

    /**
     * Unified transaction create
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @return the API response
     */
    public Map<String, Object> tradeCreate(Map<String, String> model, String notifyUrl) {
        return executeRequest(model, notifyUrl, null, "alipay.trade.create");
    }

    /**
     * Unified transaction create (certificate mode)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @return the API response
     */
    public Map<String, Object> tradeCreateWithCert(Map<String, String> model, String notifyUrl) {
        return executeRequest(true, model, notifyUrl, null, "alipay.trade.create");
    }

    /**
     * Unified transaction create with application authorization
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradeCreateWithToken(Map<String, String> model, String notifyUrl, String authToken) {
        return executeRequest(model, notifyUrl, authToken, "alipay.trade.create");
    }

    /**
     * Unified transaction refund
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRefund(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.refund");
    }

    /**
     * Unified transaction refund (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRefundWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.refund");
    }

    /**
     * Unified transaction refund with application authorization
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradeRefundWithToken(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.trade.refund");
    }

    /**
     * Unified refund page
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradePageRefund(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.page.refund");
    }

    /**
     * Unified refund page (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradePageRefundWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.page.refund");
    }

    /**
     * Unified refund page with application authorization
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradePageRefundWithToken(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.trade.page.refund");
    }

    /**
     * Unified transaction refund query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRefundQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.fastpay.refund.query");
    }

    /**
     * Unified transaction refund query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRefundQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.fastpay.refund.query");
    }

    /**
     * Unified transaction refund query with application authorization
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradeRefundQueryWithToken(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.trade.fastpay.refund.query");
    }

    /**
     * Queries the bill download URL
     *
     * @param model the request parameters
     * @return the bill download URL
     */
    public String billDownloadUrl(Map<String, String> model) {
        Map<String, Object> response = executeRequest(
                model,
                null,
                null,
                "alipay.data.dataservice.bill.downloadurl.query");
        return (String) response.get("bill_download_url");
    }

    /**
     * Queries the bill download URL (certificate mode)
     *
     * @param model the request parameters
     * @return the bill download URL
     */
    public String billDownloadUrlWithCert(Map<String, String> model) {
        Map<String, Object> response = executeRequest(
                true,
                model,
                null,
                null,
                "alipay.data.dataservice.bill.downloadurl.query");
        return (String) response.get("bill_download_url");
    }

    /**
     * Queries the bill download URL
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> billDownloadUrlQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.data.dataservice.bill.downloadurl.query");
    }

    /**
     * Queries the bill download URL (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> billDownloadUrlQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.data.dataservice.bill.downloadurl.query");
    }

    /**
     * Queries the bill download URL with application authorization
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> billDownloadUrlQueryWithToken(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.data.dataservice.bill.downloadurl.query");
    }

    /**
     * Queries the bill download URL (certificate mode, with application authorization)
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> billDownloadUrlQueryWithCertAndToken(Map<String, String> model, String authToken) {
        return executeRequest(true, model, null, authToken, "alipay.data.dataservice.bill.downloadurl.query");
    }

    /**
     * Unified transaction settlement
     *
     * @param model     the request parameters
     * @param authToken the application authorization token
     * @return the API response
     */
    public Map<String, Object> tradeOrderSettle(Map<String, String> model, String authToken) {
        return executeRequest(model, null, authToken, "alipay.trade.order.settle");
    }

    /**
     * Unified transaction settlement (simple)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeOrderSettleSimple(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.order.settle");
    }

    /**
     * Unified transaction settlement (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeOrderSettleWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.order.settle");
    }

    /**
     * PC website payment
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param returnUrl the synchronous notification URL
     * @return the HTML form
     */
    public String tradePage(Map<String, String> model, String notifyUrl, String returnUrl) {
        return buildPagePay("POST", model, notifyUrl, returnUrl, null);
    }

    /**
     * PC website payment with specified method
     *
     * @param method    GET/POST
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param returnUrl the synchronous notification URL
     * @return the HTML form or URL
     */
    public String tradePageWithMethod(String method, Map<String, String> model, String notifyUrl, String returnUrl) {
        return buildPagePay(method, model, notifyUrl, returnUrl, null);
    }

    /**
     * PC website payment with application authorization
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param returnUrl the synchronous notification URL
     * @param authToken the application authorization token
     * @return the HTML form
     */
    public String tradePageWithToken(Map<String, String> model, String notifyUrl, String returnUrl, String authToken) {
        return buildPagePay("POST", model, notifyUrl, returnUrl, authToken);
    }

    /**
     * PC website payment (OutputStream compatible)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param returnUrl the synchronous notification URL
     * @return the HTML form
     * @throws IOException if an I/O error occurs
     */
    public String tradePageByOutput(Map<String, String> model, String notifyUrl, String returnUrl) throws IOException {
        return buildPagePay("POST", model, notifyUrl, returnUrl, null);
    }

    /**
     * PC website payment (OutputStream compatible, with application authorization)
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param returnUrl the synchronous notification URL
     * @param authToken the application authorization token
     * @return the HTML form
     * @throws IOException if an I/O error occurs
     */
    public String tradePageByOutputWithToken(
            Map<String, String> model,
            String notifyUrl,
            String returnUrl,
            String authToken) throws IOException {
        return buildPagePay("POST", model, notifyUrl, returnUrl, authToken);
    }

    /**
     * Fund pre-authorization freeze
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOrderFreeze(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.order.freeze");
    }

    /**
     * Fund pre-authorization freeze (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOrderFreezeWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.order.freeze");
    }

    /**
     * Fund authorization unfreeze
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOrderUnfreeze(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.order.unfreeze");
    }

    /**
     * Fund authorization unfreeze (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOrderUnfreezeWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.order.unfreeze");
    }

    /**
     * Fund pre-authorization voucher create
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOrderVoucherCreate(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.order.voucher.create");
    }

    /**
     * Fund pre-authorization voucher create (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOrderVoucherCreateWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.order.voucher.create");
    }

    /**
     * Fund authorization cancel
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOperationCancel(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.operation.cancel");
    }

    /**
     * Fund authorization cancel (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOperationCancelWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.operation.cancel");
    }

    /**
     * Fund authorization operation detail query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOperationDetailQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.auth.operation.detail.query");
    }

    /**
     * Fund authorization operation detail query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authOperationDetailQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.auth.operation.detail.query");
    }

    /**
     * Red packet wireless payment
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderAppPay(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.app.pay");
    }

    /**
     * Red packet wireless payment (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderAppPayWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.app.pay");
    }

    /**
     * Red packet page payment
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderPagePay(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.page.pay");
    }

    /**
     * Red packet page payment (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderPagePayWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.page.pay");
    }

    /**
     * Red packet agreement payment
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderAgreementPay(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.agreement.pay");
    }

    /**
     * Red packet agreement payment (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderAgreementPayWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.agreement.pay");
    }

    /**
     * Red packet disburse
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderDisburse(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.disburse");
    }

    /**
     * Red packet disburse (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderDisburseWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.disburse");
    }

    /**
     * Red packet refund
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderRefund(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.order.refund");
    }

    /**
     * Red packet refund (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOrderRefundWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.order.refund");
    }

    /**
     * Red packet operation query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOperationQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.fund.coupon.operation.query");
    }

    /**
     * Red packet operation query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> fundCouponOperationQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.fund.coupon.operation.query");
    }

    /**
     * Application authorization URL assembly
     *
     * @param appId       the application ID
     * @param redirectUri the callback URI
     * @return the application authorization URL
     * @throws java.io.UnsupportedEncodingException if encoding is not supported
     */
    public String getOauth2Url(String appId, String redirectUri) throws java.io.UnsupportedEncodingException {
        return new StringBuffer().append("https://openauth.alipay.com/oauth2/appToAppAuth.htm?app_id=").append(appId)
                .append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8")).toString();
    }

    /**
     * Exchanges app_auth_code for app_auth_token
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> openAuthTokenApp(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.open.auth.token.app");
    }

    /**
     * Exchanges app_auth_code for app_auth_token (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> openAuthTokenAppWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.open.auth.token.app");
    }

    /**
     * Queries authorization information
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> openAuthTokenAppQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.open.auth.token.app.query");
    }

    /**
     * Queries authorization information (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> openAuthTokenAppQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.open.auth.token.app.query");
    }

    /**
     * Metro ticket code generation
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> voucherGenerate(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.commerce.cityfacilitator.voucher.generate");
    }

    /**
     * Metro ticket code generation (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> voucherGenerateWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.commerce.cityfacilitator.voucher.generate");
    }

    /**
     * Metro ticket code refund
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> metroRefund(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.commerce.cityfacilitator.voucher.refund");
    }

    /**
     * Metro ticket code refund (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> metroRefundWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.commerce.cityfacilitator.voucher.refund");
    }

    /**
     * Metro station data query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> stationQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.commerce.cityfacilitator.station.query");
    }

    /**
     * Metro station data query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> stationQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.commerce.cityfacilitator.station.query");
    }

    /**
     * Verification code batch query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> voucherBatchQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.commerce.cityfacilitator.voucher.batchquery");
    }

    /**
     * Verification code batch query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> voucherBatchQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.commerce.cityfacilitator.voucher.batchquery");
    }

    /**
     * Batch transfer
     *
     * @param params     the request parameters
     * @param privateKey the private key
     * @param signType   the signature type
     * @return the signed parameters
     */
    public Map<String, String> batchTrans(Map<String, String> params, String privateKey, String signType) {
        params.put("service", "batch_trans_notify");
        params.put("_input_charset", "UTF-8");
        params.put("pay_date", DateKit.format(new Date(), "YYYYMMDD"));
        return AliPayBuilder.buildRequestPara(params, privateKey, signType);
    }

    /**
     * Life utility bill payment query
     *
     * @param orderType       the Alipay order type
     * @param merchantOrderNo the business serial number
     * @return the API response
     */
    public Map<String, Object> ebppBillGet(String orderType, String merchantOrderNo) {
        Map<String, String> model = new HashMap<>();
        model.put("order_type", orderType);
        model.put("merchant_order_no", merchantOrderNo);
        return executeRequest(model, null, null, "alipay.ebpp.bill.get");
    }

    /**
     * Life utility bill payment query (certificate mode)
     *
     * @param orderType       the Alipay order type
     * @param merchantOrderNo the business serial number
     * @return the API response
     */
    public Map<String, Object> ebppBillGetWithCert(String orderType, String merchantOrderNo) {
        Map<String, String> model = new HashMap<>();
        model.put("order_type", orderType);
        model.put("merchant_order_no", merchantOrderNo);
        return executeRequest(true, model, null, null, "alipay.ebpp.bill.get");
    }

    /**
     * H5 face recognition initialization
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> identificationUserWebInit(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.identification.user.web.initialize");
    }

    /**
     * H5 face recognition initialization (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> identificationUserWebInitWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.identification.user.web.initialize");
    }

    /**
     * H5 face recognition query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> identificationUserWebQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.identification.user.web.query");
    }

    /**
     * H5 face recognition query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> identificationUserWebQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.identification.user.web.query");
    }

    /**
     * Face enrollment
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationCustomerFaceManageCreate(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.customer.facemanage.create");
    }

    /**
     * Face enrollment (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationCustomerFaceManageCreateWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.customer.facemanage.create");
    }

    /**
     * Face removal
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationCustomerFaceManageDelete(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.customer.facemanage.delete");
    }

    /**
     * Face removal (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationCustomerFaceManageDeleteWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.customer.facemanage.delete");
    }

    /**
     * Face ftoken query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationCustomerFtokenQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.customer.ftoken.query");
    }

    /**
     * Face ftoken query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationCustomerFtokenQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.customer.ftoken.query");
    }

    /**
     * Face smile pay initialize
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationSmilePayInitialize(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.smilepay.initialize");
    }

    /**
     * Face smile pay initialize (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationSmilePayInitializeWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.smilepay.initialize");
    }

    /**
     * Face customer smile pay initialize (to invoke zim)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationCustomerSmilePayInitialize(Map<String, String> model) {
        return executeRequest(model, null, null, "zoloz.authentication.customer.smilepay.initialize");
    }

    /**
     * Face customer smile pay initialize (certificate mode, to invoke zim)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> authenticationCustomerSmilePayInitializeWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "zoloz.authentication.customer.smilepay.initialize");
    }

    /**
     * Ecosystem incentive project ISV contract signing
     *
     * @return the API response
     */
    public Map<String, Object> commerceAdContractSign() {
        return executeRequest(new HashMap<>(), null, null, "alipay.commerce.ad.contract.sign");
    }

    /**
     * Ecosystem incentive project ISV contract signing (certificate mode)
     *
     * @return the API response
     */
    public Map<String, Object> commerceAdContractSignWithCert() {
        return executeRequest(true, new HashMap<>(), null, null, "alipay.commerce.ad.contract.sign");
    }

    /**
     * Royalty relation binding
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRoyaltyRelationBind(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.royalty.relation.bind");
    }

    /**
     * Royalty relation binding (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRoyaltyRelationBindWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.royalty.relation.bind");
    }

    /**
     * Royalty relation unbinding
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRoyaltyRelationUnbind(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.royalty.relation.unbind");
    }

    /**
     * Royalty relation unbinding (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRoyaltyRelationUnbindWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.royalty.relation.unbind");
    }

    /**
     * Royalty relation batch query
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRoyaltyRelationBatchQuery(Map<String, String> model) {
        return executeRequest(model, null, null, "alipay.trade.royalty.relation.batchquery");
    }

    /**
     * Royalty relation batch query (certificate mode)
     *
     * @param model the request parameters
     * @return the API response
     */
    public Map<String, Object> tradeRoyaltyRelationBatchQueryWithCert(Map<String, String> model) {
        return executeRequest(true, model, null, null, "alipay.trade.royalty.relation.batchquery");
    }

    /**
     * Builds APP payment request
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @param method    the Alipay API method
     * @return the signed request parameter string
     */
    private String buildAppRequest(Map<String, String> model, String notifyUrl, String authToken, String method) {
        Map<String, String> params = buildCommonParams(method, notifyUrl, authToken);
        params.put("biz_content", JsonKit.toJsonString(model));
        params = AliPayBuilder.buildRequestPara(params, context.getPrivateKey(), Algorithm.RSA2.getValue());
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), Charset.UTF_8))
                    .append("&");
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * Builds WAP payment form
     *
     * @param model     the request parameters
     * @param returnUrl the synchronous notification URL
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the HTML form
     */
    private String buildWapPay(Map<String, String> model, String returnUrl, String notifyUrl, String authToken) {
        Map<String, String> params = buildCommonParams("alipay.trade.wap.pay", notifyUrl, authToken);
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
     * Builds PC payment form or URL
     *
     * @param method    GET/POST
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param returnUrl the synchronous notification URL
     * @param authToken the application authorization token
     * @return the HTML form or URL
     */
    private String buildPagePay(
            String method,
            Map<String, String> model,
            String notifyUrl,
            String returnUrl,
            String authToken) {
        Map<String, String> params = buildCommonParams("alipay.trade.page.pay", notifyUrl, authToken);
        params.put("return_url", returnUrl);
        params.put("biz_content", JsonKit.toJsonString(model));
        params = AliPayBuilder.buildRequestPara(params, context.getPrivateKey(), Algorithm.RSA2.getValue());
        if ("GET".equalsIgnoreCase(method)) {
            StringBuilder url = new StringBuilder(getUrl()).append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), Charset.UTF_8))
                        .append("&");
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
     * Builds common request parameters
     *
     * @param method    the Alipay API method
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @return the parameter map
     */
    private Map<String, String> buildCommonParams(String method, String notifyUrl, String authToken) {
        Map<String, String> params = new HashMap<>();
        params.put("method", method);
        params.put("app_id", context.getAppId());
        params.put("timestamp", DateKit.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        params.put("charset", Charset.DEFAULT_UTF_8);
        params.put("version", "1.0");
        if (!StringKit.isEmpty(notifyUrl)) {
            params.put("notify_url", notifyUrl);
        }
        if (!StringKit.isEmpty(authToken)) {
            params.put("app_auth_token", authToken);
        }
        return params;
    }

    /**
     * Executes Alipay API request
     *
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @param method    the Alipay API method
     * @return the API response
     */
    private Map<String, Object> executeRequest(
            Map<String, String> model,
            String notifyUrl,
            String authToken,
            String method) {
        return executeRequest(context.isCertMode(), model, notifyUrl, authToken, method);
    }

    /**
     * Executes Alipay API request
     *
     * @param certModel whether to use certificate mode
     * @param model     the request parameters
     * @param notifyUrl the asynchronous notification URL
     * @param authToken the application authorization token
     * @param method    the Alipay API method
     * @return the API response
     */
    private Map<String, Object> executeRequest(
            Boolean certModel,
            Map<String, String> model,
            String notifyUrl,
            String authToken,
            String method) {
        Map<String, String> params = buildCommonParams(method, notifyUrl, authToken);
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
            boolean isValid = AliPayBuilder.rsaCertCheckV1ByContent(
                    verifyParams,
                    context.getPublicKey(),
                    Charset.DEFAULT_UTF_8,
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
     * Executes HTTP request
     *
     * @param params the request parameters
     * @return the response string
     */
    private String executeHttpRequest(Map<String, String> params) {
        return Httpx.post(getUrl(), params);
    }

    /**
     * Executes certificate mode HTTP request
     *
     * @param params the request parameters
     * @return the response string
     */
    private String executeCertRequest(Map<String, String> params) {
        throw new UnsupportedOperationException("Certificate mode not implemented");
    }

}
