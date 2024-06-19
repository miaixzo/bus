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
package org.miaixz.bus.pay.metric.alipay;

import com.alipay.api.*;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import jakarta.servlet.http.HttpServletResponse;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.alipay.api.AliPayApi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * 支付宝支付相关接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliPayProvider extends AbstractProvider<Material, Context> {

    /**
     * 支付宝客户端
     */
    private AlipayClient client;

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

        this.client = new DefaultAlipayClient(getUrl(), this.context.getAppId(), this.context.getPrivateKey(), HTTP.JSON,
                Charset.DEFAULT_UTF_8, this.context.getPublicKey(), Algorithm.RSA2.getValue());
    }

    /**
     * 获取接口请求的 URL
     *
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * 获取接口请求的 URL
     *
     * @param complex {@link AliPayApi} 支付 API 接口枚举
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.ALIPAY.sandbox() : Registry.ALIPAY.service()).concat(complex.method());
    }

    /**
     * APP支付
     *
     * @param model     {@link AlipayTradeAppPayModel}
     * @param notifyUrl 异步通知 URL
     * @return {@link AlipayTradeAppPayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeAppPayResponse appPayToResponse(AlipayTradeAppPayModel model, String notifyUrl) throws AlipayApiException {
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return sdkExecute(request);
    }

    /**
     * APP支付
     *
     * @param model        {@link AlipayTradeAppPayModel}
     * @param notifyUrl    异步通知 URL
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradeAppPayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeAppPayResponse appPayToResponse(AlipayTradeAppPayModel model, String notifyUrl, String appAuthToken) throws AlipayApiException {
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.putOtherTextParam("app_auth_token", appAuthToken);
        return sdkExecute(request);
    }

    /**
     * WAP支付
     *
     * @param response  {@link HttpServletResponse}
     * @param model     {@link AlipayTradeWapPayModel}
     * @param returnUrl 同步通知URL
     * @param notifyUrl 异步通知URL
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void wapPay(HttpServletResponse response, AlipayTradeWapPayModel model, String returnUrl, String notifyUrl) throws AlipayApiException, IOException {
        String form = wapPayStr(model, returnUrl, notifyUrl);
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        PrintWriter out = response.getWriter();
        out.write(form);
        out.flush();
        out.close();
    }

    /**
     * WAP支付
     *
     * @param response     {@link HttpServletResponse}
     * @param model        {@link AlipayTradeWapPayModel}
     * @param returnUrl    异步通知URL
     * @param notifyUrl    同步通知URL
     * @param appAuthToken 应用授权token
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void wapPay(HttpServletResponse response, AlipayTradeWapPayModel model, String returnUrl, String notifyUrl, String appAuthToken) throws AlipayApiException, IOException {
        String form = wapPayStr(model, returnUrl, notifyUrl, appAuthToken);
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        PrintWriter out = response.getWriter();
        out.write(form);
        out.flush();
        out.close();
    }

    /**
     * WAP支付
     * 为了解决 Filter 中使用 OutputStream getOutputStream() 和 PrintWriter getWriter() 冲突异常问题
     *
     * @param response     {@link HttpServletResponse}
     * @param model        {@link AlipayTradeWapPayModel}
     * @param returnUrl    异步通知URL
     * @param notifyUrl    同步通知URL
     * @param appAuthToken 应用授权token
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void wapPayByOutputStream(HttpServletResponse response, AlipayTradeWapPayModel model, String returnUrl, String notifyUrl, String appAuthToken) throws AlipayApiException, IOException {
        String form = wapPayStr(model, returnUrl, notifyUrl, appAuthToken);
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        OutputStream out = response.getOutputStream();
        out.write(form.getBytes(Charset.DEFAULT_UTF_8));
        response.getOutputStream().flush();
    }

    /**
     * WAP支付
     * 为了解决 Filter 中使用 OutputStream getOutputStream() 和 PrintWriter getWriter() 冲突异常问题
     *
     * @param response  {@link HttpServletResponse}
     * @param model     {@link AlipayTradeWapPayModel}
     * @param returnUrl 异步通知URL
     * @param notifyUrl 同步通知URL
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void wapPayByOutputStream(HttpServletResponse response, AlipayTradeWapPayModel model, String returnUrl, String notifyUrl) throws AlipayApiException, IOException {
        String form = wapPayStr(model, returnUrl, notifyUrl);
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        OutputStream out = response.getOutputStream();
        out.write(form.getBytes(Charset.DEFAULT_UTF_8));
        response.getOutputStream().flush();
    }

    /**
     * WAP支付
     *
     * @param model     {@link AlipayTradeWapPayModel}
     * @param returnUrl 异步通知URL
     * @param notifyUrl 同步通知URL
     * @return {String}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public String wapPayStr(AlipayTradeWapPayModel model, String returnUrl, String notifyUrl) throws AlipayApiException {
        AlipayTradeWapPayRequest aliPayRequest = new AlipayTradeWapPayRequest();
        aliPayRequest.setReturnUrl(returnUrl);
        aliPayRequest.setNotifyUrl(notifyUrl);
        aliPayRequest.setBizModel(model);
        return pageExecute(aliPayRequest).getBody();
    }

    /**
     * WAP支付
     *
     * @param model        {@link AlipayTradeWapPayModel}
     * @param returnUrl    异步通知URL
     * @param notifyUrl    同步通知URL
     * @param appAuthToken 应用授权token
     * @return {String}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public String wapPayStr(AlipayTradeWapPayModel model, String returnUrl, String notifyUrl, String appAuthToken) throws AlipayApiException {
        AlipayTradeWapPayRequest aliPayRequest = new AlipayTradeWapPayRequest();
        aliPayRequest.setReturnUrl(returnUrl);
        aliPayRequest.setNotifyUrl(notifyUrl);
        aliPayRequest.setBizModel(model);
        aliPayRequest.putOtherTextParam("app_auth_token", appAuthToken);
        return pageExecute(aliPayRequest).getBody();
    }

    /**
     * 统一收单交易支付接口接口
     * 适用于:条形码支付、声波支付等
     *
     * @param model     {@link AlipayTradePayModel}
     * @param notifyUrl 异步通知URL
     * @return {@link AlipayTradePayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePayResponse tradePayToResponse(AlipayTradePayModel model, String notifyUrl) throws AlipayApiException {
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        // 填充业务参数
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return doExecute(request);
    }

    /**
     * 统一收单交易支付接口接口
     * 适用于:条形码支付、声波支付等
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradePayModel}
     * @param notifyUrl    异步通知URL
     * @return {@link AlipayTradePayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePayResponse tradePayToResponse(Boolean certModel, AlipayTradePayModel model, String notifyUrl) throws AlipayApiException {
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        // 填充业务参数
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单交易支付接口接口
     * 适用于:条形码支付、声波支付等
     *
     * @param model        {AlipayTradePayModel}
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return {AlipayTradePayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePayResponse tradePayToResponse(AlipayTradePayModel model, String notifyUrl, String appAuthToken) throws AlipayApiException {
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.putOtherTextParam("app_auth_token", appAuthToken);
        return doExecute(request);
    }

    /**
     * 统一收单交易支付接口接口
     * 适用于:条形码支付、声波支付等
     *
     * @param certModel    是否证书模式
     * @param model        {AlipayTradePayModel}
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return {AlipayTradePayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePayResponse tradePayToResponse(Boolean certModel, AlipayTradePayModel model, String notifyUrl, String appAuthToken) throws AlipayApiException {
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.putOtherTextParam("app_auth_token", appAuthToken);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单线下交易预创建
     * 适用于：扫码支付等
     *
     * @param model     {@link AlipayTradePrecreateModel}
     * @param notifyUrl 异步通知URL
     * @return {@link AlipayTradePrecreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePrecreateResponse tradePrecreatePayToResponse(AlipayTradePrecreateModel model, String notifyUrl) throws AlipayApiException {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return doExecute(request);
    }

    /**
     * 统一收单线下交易预创建
     * 适用于：扫码支付等
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradePrecreateModel}
     * @param notifyUrl    异步通知URL
     * @return {@link AlipayTradePrecreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePrecreateResponse tradePrecreatePayToResponse(Boolean certModel, AlipayTradePrecreateModel model, String notifyUrl) throws AlipayApiException {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单线下交易预创建
     * 适用于：扫码支付等
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradePrecreateModel}
     * @param notifyUrl    异步通知URL
     * @return {@link AlipayTradePrecreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePrecreateResponse tradePrecreatePayToResponse(Boolean certModel, AlipayTradePrecreateModel model, String notifyUrl, String appAuthToken) throws AlipayApiException {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return doExecute(certModel, request, appAuthToken);
    }

    /**
     * 统一收单线下交易预创建
     * 适用于：扫码支付等
     *
     * @param model        {@link AlipayTradePrecreateModel}
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradePrecreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePrecreateResponse tradePrecreatePayToResponse(AlipayTradePrecreateModel model, String notifyUrl, String appAuthToken) throws AlipayApiException {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return execute(request, null, appAuthToken);
    }

    /**
     * 单笔转账到支付宝账户
     *
     * @param model {@link AlipayFundTransToaccountTransferModel}
     * @return {@link AlipayFundTransToaccountTransferResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundTransToaccountTransferResponse transferToResponse(AlipayFundTransToaccountTransferModel model) throws AlipayApiException {
        AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 单笔转账到支付宝账户
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundTransToaccountTransferModel}
     * @return {@link AlipayFundTransToaccountTransferResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundTransToaccountTransferResponse transferToResponse(Boolean certModel, AlipayFundTransToaccountTransferModel model) throws AlipayApiException {
        AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 转账查询接口
     *
     * @param model {@link AlipayFundTransOrderQueryModel}
     * @return {@link AlipayFundTransOrderQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundTransOrderQueryResponse transferQueryToResponse(AlipayFundTransOrderQueryModel model) throws AlipayApiException {
        AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 转账查询接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundTransOrderQueryModel}
     * @return {@link AlipayFundTransOrderQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundTransOrderQueryResponse transferQueryToResponse(Boolean certModel, AlipayFundTransOrderQueryModel model) throws AlipayApiException {
        AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 统一转账接口
     *
     * @param model        model {@link AlipayFundTransUniTransferModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayFundTransUniTransferResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundTransUniTransferResponse uniTransferToResponse(AlipayFundTransUniTransferModel model, String appAuthToken) throws AlipayApiException {
        AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
        request.setBizModel(model);
        if (!StringKit.isEmpty(appAuthToken)) {
            request.putOtherTextParam("app_auth_token", appAuthToken);
        }
        return doExecute(request);
    }

    /**
     * 统一转账接口
     *
     * @param certModel    是否证书模式
     * @param model        model {@link AlipayFundTransUniTransferModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayFundTransUniTransferResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundTransUniTransferResponse uniTransferToResponse(Boolean certModel, AlipayFundTransUniTransferModel model, String appAuthToken) throws AlipayApiException {
        AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
        request.setBizModel(model);
        if (!StringKit.isEmpty(appAuthToken)) {
            request.putOtherTextParam("app_auth_token", appAuthToken);
        }
        return doExecute(certModel, request);
    }

    /**
     * 转账业务单据查询接口
     *
     * @param model        model {@link AlipayFundTransCommonQueryModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayFundTransCommonQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundTransCommonQueryResponse transCommonQueryToResponse(AlipayFundTransCommonQueryModel model, String appAuthToken) throws AlipayApiException {
        AlipayFundTransCommonQueryRequest request = new AlipayFundTransCommonQueryRequest();
        request.setBizModel(model);
        if (!StringKit.isEmpty(appAuthToken)) {
            request.putOtherTextParam("app_auth_token", appAuthToken);
        }
        return doExecute(request);
    }

    /**
     * 转账业务单据查询接口
     *
     * @param certModel    是否证书模式
     * @param model        model {@link AlipayFundTransCommonQueryModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayFundTransCommonQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundTransCommonQueryResponse transCommonQueryToResponse(Boolean certModel, AlipayFundTransCommonQueryModel model, String appAuthToken) throws AlipayApiException {
        AlipayFundTransCommonQueryRequest request = new AlipayFundTransCommonQueryRequest();
        request.setBizModel(model);
        if (!StringKit.isEmpty(appAuthToken)) {
            request.putOtherTextParam("app_auth_token", appAuthToken);
        }
        return doExecute(certModel, request);
    }

    /**
     * 支付宝资金账户资产查询接口
     *
     * @param model        model {@link AlipayFundAccountQueryModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayFundAccountQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAccountQueryResponse accountQueryToResponse(AlipayFundAccountQueryModel model, String appAuthToken) throws AlipayApiException {
        AlipayFundAccountQueryRequest request = new AlipayFundAccountQueryRequest();
        request.setBizModel(model);
        if (!StringKit.isEmpty(appAuthToken)) {
            request.putOtherTextParam("app_auth_token", appAuthToken);
        }
        return doExecute(request);
    }

    /**
     * 支付宝资金账户资产查询接口
     *
     * @param certModel    是否证书模式
     * @param model        model {@link AlipayFundAccountQueryModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayFundAccountQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAccountQueryResponse accountQueryToResponse(Boolean certModel, AlipayFundAccountQueryModel model, String appAuthToken) throws AlipayApiException {
        AlipayFundAccountQueryRequest request = new AlipayFundAccountQueryRequest();
        request.setBizModel(model);
        if (!StringKit.isEmpty(appAuthToken)) {
            request.putOtherTextParam("app_auth_token", appAuthToken);
        }
        return doExecute(certModel, request);
    }

    /**
     * 统一收单线下交易查询接口
     *
     * @param model {@link AlipayTradeQueryModel}
     * @return {@link AlipayTradeQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeQueryResponse tradeQueryToResponse(AlipayTradeQueryModel model) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 统一收单线下交易查询接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeQueryModel}
     * @return {@link AlipayTradeQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeQueryResponse tradeQueryToResponse(Boolean certModel, AlipayTradeQueryModel model) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单线下交易查询接口
     *
     * @param model        {@link AlipayTradeQueryModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradeQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeQueryResponse tradeQueryToResponse(AlipayTradeQueryModel model, String appAuthToken) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        return execute(request, null, appAuthToken);
    }

    /**
     * 统一收单交易撤销接口
     *
     * @param model        {@link AlipayTradeCancelModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradeCancelResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCancelResponse tradeCancelToResponse(AlipayTradeCancelModel model, String appAuthToken) throws AlipayApiException {
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizModel(model);
        return execute(request, null, appAuthToken);
    }

    /**
     * 统一收单交易撤销接口
     *
     * @param model {@link AlipayTradeCancelModel}
     * @return {@link AlipayTradeCancelResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCancelResponse tradeCancelToResponse(AlipayTradeCancelModel model) throws AlipayApiException {
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 统一收单交易撤销接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeCancelModel}
     * @return {@link AlipayTradeCancelResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCancelResponse tradeCancelToResponse(Boolean certModel, AlipayTradeCancelModel model) throws AlipayApiException {
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单交易关闭接口
     *
     * @param model        {@link AlipayTradeCloseModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradeCloseResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCloseResponse tradeCloseToResponse(AlipayTradeCloseModel model, String appAuthToken) throws AlipayApiException {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);
        return execute(request, null, appAuthToken);

    }

    /**
     * 统一收单交易关闭接口
     *
     * @param model {@link AlipayTradeCloseModel}
     * @return {@link AlipayTradeCloseResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCloseResponse tradeCloseToResponse(AlipayTradeCloseModel model) throws AlipayApiException {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 统一收单交易关闭接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeCloseModel}
     * @return {@link AlipayTradeCloseResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCloseResponse tradeCloseToResponse(Boolean certModel, AlipayTradeCloseModel model) throws AlipayApiException {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单交易创建接口
     *
     * @param model     {@link AlipayTradeCreateModel}
     * @param notifyUrl 异步通知URL
     * @return {@link AlipayTradeCreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCreateResponse tradeCreateToResponse(AlipayTradeCreateModel model, String notifyUrl) throws AlipayApiException {
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return doExecute(request);
    }

    /**
     * 统一收单交易创建接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeCreateModel}
     * @param notifyUrl    异步通知URL
     * @return {@link AlipayTradeCreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCreateResponse tradeCreateToResponse(Boolean certModel, AlipayTradeCreateModel model, String notifyUrl) throws AlipayApiException {
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单交易创建接口
     *
     * @param model        {@link AlipayTradeCreateModel}
     * @param notifyUrl    异步通知URL
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradeCreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeCreateResponse tradeCreateToResponse(AlipayTradeCreateModel model, String notifyUrl, String appAuthToken) throws AlipayApiException {
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        return execute(request, null, appAuthToken);
    }

    /**
     * 统一收单交易退款接口
     *
     * @param model {@link AlipayTradeRefundModel}
     * @return {@link AlipayTradeRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRefundResponse tradeRefundToResponse(AlipayTradeRefundModel model) throws AlipayApiException {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 统一收单交易退款接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeRefundModel}
     * @return {@link AlipayTradeRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRefundResponse tradeRefundToResponse(Boolean certModel, AlipayTradeRefundModel model) throws AlipayApiException {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单交易退款接口
     *
     * @param model        {@link AlipayTradeRefundModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradeRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRefundResponse tradeRefundToResponse(AlipayTradeRefundModel model, String appAuthToken) throws AlipayApiException {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        return execute(request, null, appAuthToken);
    }

    /**
     * 统一收单退款页面接口
     *
     * @param model {@link AlipayTradePageRefundModel}
     * @return {@link AlipayTradePageRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePageRefundResponse tradeRefundToResponse(AlipayTradePageRefundModel model) throws AlipayApiException {
        AlipayTradePageRefundRequest request = new AlipayTradePageRefundRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 统一收单退款页面接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradePageRefundModel}
     * @return {@link AlipayTradePageRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePageRefundResponse tradeRefundToResponse(Boolean certModel, AlipayTradePageRefundModel model) throws AlipayApiException {
        AlipayTradePageRefundRequest request = new AlipayTradePageRefundRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单退款页面接口
     *
     * @param model        {@link AlipayTradePageRefundModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradePageRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradePageRefundResponse tradeRefundToResponse(AlipayTradePageRefundModel model, String appAuthToken) throws AlipayApiException {
        AlipayTradePageRefundRequest request = new AlipayTradePageRefundRequest();
        request.setBizModel(model);
        return execute(request, null, appAuthToken);
    }

    /**
     * 统一收单交易退款查询
     *
     * @param model {@link AlipayTradeFastpayRefundQueryModel}
     * @return {@link AlipayTradeFastpayRefundQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeFastpayRefundQueryResponse tradeRefundQueryToResponse(AlipayTradeFastpayRefundQueryModel model) throws AlipayApiException {
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 统一收单交易退款查询
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeFastpayRefundQueryModel}
     * @return {@link AlipayTradeFastpayRefundQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeFastpayRefundQueryResponse tradeRefundQueryToResponse(Boolean certModel, AlipayTradeFastpayRefundQueryModel model) throws AlipayApiException {
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单交易退款查询
     *
     * @param model        {@link AlipayTradeFastpayRefundQueryModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradeFastpayRefundQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeFastpayRefundQueryResponse tradeRefundQueryToResponse(AlipayTradeFastpayRefundQueryModel model, String appAuthToken) throws AlipayApiException {
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);
        return execute(request, null, appAuthToken);
    }

    /**
     * 查询对账单下载地址
     *
     * @param model {@link AlipayDataDataserviceBillDownloadurlQueryModel}
     * @return 对账单下载地址
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public String billDownloadUrlQuery(AlipayDataDataserviceBillDownloadurlQueryModel model) throws AlipayApiException {
        AlipayDataDataserviceBillDownloadurlQueryResponse response = billDownloadUrlQueryToResponse(model);
        return response.getBillDownloadUrl();
    }

    /**
     * 查询对账单下载地址
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayDataDataserviceBillDownloadurlQueryModel}
     * @return 对账单下载地址
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public String billDownloadUrlQuery(Boolean certModel, AlipayDataDataserviceBillDownloadurlQueryModel model) throws AlipayApiException {
        AlipayDataDataserviceBillDownloadurlQueryResponse response = billDownloadUrlQueryToResponse(certModel, model);
        return response.getBillDownloadUrl();
    }

    /**
     * 查询对账单下载地址
     *
     * @param model {@link AlipayDataDataserviceBillDownloadurlQueryModel}
     * @return {@link AlipayDataDataserviceBillDownloadurlQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayDataDataserviceBillDownloadurlQueryResponse billDownloadUrlQueryToResponse(AlipayDataDataserviceBillDownloadurlQueryModel model) throws AlipayApiException {
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 查询对账单下载地址
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayDataDataserviceBillDownloadurlQueryModel}
     * @return {@link AlipayDataDataserviceBillDownloadurlQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayDataDataserviceBillDownloadurlQueryResponse billDownloadUrlQueryToResponse(Boolean certModel, AlipayDataDataserviceBillDownloadurlQueryModel model) throws AlipayApiException {
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 查询对账单下载地址
     *
     * @param model        {@link AlipayDataDataserviceBillDownloadurlQueryModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayDataDataserviceBillDownloadurlQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayDataDataserviceBillDownloadurlQueryResponse billDownloadUrlQueryToResponse(AlipayDataDataserviceBillDownloadurlQueryModel model, String appAuthToken) throws AlipayApiException {
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        request.setBizModel(model);
        request.putOtherTextParam("app_auth_token", appAuthToken);
        return doExecute(request);
    }

    /**
     * 查询对账单下载地址
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayDataDataserviceBillDownloadurlQueryModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayDataDataserviceBillDownloadurlQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayDataDataserviceBillDownloadurlQueryResponse billDownloadUrlQueryToResponse(Boolean certModel, AlipayDataDataserviceBillDownloadurlQueryModel model, String appAuthToken) throws AlipayApiException {
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        request.setBizModel(model);
        request.putOtherTextParam("app_auth_token", appAuthToken);
        return doExecute(certModel, request);
    }

    /**
     * 统一收单交易结算接口
     *
     * @param model        {@link AlipayTradeOrderSettleModel}
     * @param appAuthToken 应用授权token
     * @return {@link AlipayTradeOrderSettleResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeOrderSettleResponse tradeOrderSettleToResponse(AlipayTradeOrderSettleModel model, String appAuthToken) throws AlipayApiException {
        AlipayTradeOrderSettleRequest request = new AlipayTradeOrderSettleRequest();
        request.setBizModel(model);
        return execute(request, null, appAuthToken);
    }

    /**
     * 统一收单交易结算接口
     *
     * @param model {@link AlipayTradeOrderSettleModel}
     * @return {@link AlipayTradeOrderSettleResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeOrderSettleResponse tradeOrderSettleToResponse(AlipayTradeOrderSettleModel model) throws AlipayApiException {
        AlipayTradeOrderSettleRequest request = new AlipayTradeOrderSettleRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 统一收单交易结算接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeOrderSettleModel}
     * @return {@link AlipayTradeOrderSettleResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeOrderSettleResponse tradeOrderSettleToResponse(Boolean certModel, AlipayTradeOrderSettleModel model) throws AlipayApiException {
        AlipayTradeOrderSettleRequest request = new AlipayTradeOrderSettleRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 电脑网站支付(PC支付)
     *
     * @param response  {@link HttpServletResponse}
     * @param model     {@link AlipayTradePagePayModel}
     * @param notifyUrl 异步通知URL
     * @param returnUrl 同步通知URL
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void tradePage(HttpServletResponse response, AlipayTradePagePayModel model, String notifyUrl, String returnUrl) throws AlipayApiException, IOException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        String form = pageExecute(request).getBody();
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        PrintWriter out = response.getWriter();
        out.write(form);
        out.flush();
        out.close();
    }

    /**
     * 电脑网站支付(PC支付)
     *
     * @param response  {@link HttpServletResponse}
     * @param method    GET/POST GET 返回url,POST 返回 FORM <a href="https://opensupport.alipay.com/support/helpcenter/192/201602488772?ant_source=antsupport">参考文章</a>
     * @param model     {@link AlipayTradePagePayModel}
     * @param notifyUrl 异步通知URL
     * @param returnUrl 同步通知URL
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void tradePage(HttpServletResponse response, String method, AlipayTradePagePayModel model, String notifyUrl, String returnUrl) throws AlipayApiException, IOException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        String form = pageExecute(request, method).getBody();
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        PrintWriter out = response.getWriter();
        out.write(form);
        out.flush();
        out.close();
    }

    /**
     * 电脑网站支付(PC支付)
     *
     * @param response     {@link HttpServletResponse}
     * @param model        {@link AlipayTradePagePayModel}
     * @param notifyUrl    异步通知URL
     * @param returnUrl    同步通知URL
     * @param appAuthToken 应用授权token
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void tradePage(HttpServletResponse response, AlipayTradePagePayModel model, String notifyUrl, String returnUrl, String appAuthToken) throws AlipayApiException, IOException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        request.putOtherTextParam("app_auth_token", appAuthToken);
        String form = pageExecute(request).getBody();
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        PrintWriter out = response.getWriter();
        out.write(form);
        out.flush();
        out.close();
    }

    /**
     * 电脑网站支付(PC支付)
     *
     * @param response  {@link HttpServletResponse}
     * @param model     {@link AlipayTradePagePayModel}
     * @param notifyUrl 异步通知URL
     * @param returnUrl 同步通知URL
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void tradePageByOutputStream(HttpServletResponse response, AlipayTradePagePayModel model, String notifyUrl, String returnUrl) throws AlipayApiException, IOException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        String form = pageExecute(request).getBody();
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        OutputStream out = response.getOutputStream();
        out.write(form.getBytes(Charset.DEFAULT_UTF_8));
        response.getOutputStream().flush();
    }

    /**
     * 电脑网站支付(PC支付)
     *
     * @param response     {@link HttpServletResponse}
     * @param model        {@link AlipayTradePagePayModel}
     * @param notifyUrl    异步通知URL
     * @param returnUrl    同步通知URL
     * @param appAuthToken 应用授权token
     * @throws AlipayApiException 支付宝 Api 异常
     * @throws IOException        IO 异常
     */
    public void tradePageByOutputStream(HttpServletResponse response, AlipayTradePagePayModel model, String notifyUrl, String returnUrl, String appAuthToken) throws AlipayApiException, IOException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        request.putOtherTextParam("app_auth_token", appAuthToken);
        String form = pageExecute(request).getBody();
        response.setContentType("text/html;charset=" + Charset.DEFAULT_UTF_8);
        OutputStream out = response.getOutputStream();
        out.write(form.getBytes(Charset.DEFAULT_UTF_8));
        response.getOutputStream().flush();
    }

    /**
     * 资金预授权冻结接口
     *
     * @param model {@link AlipayFundAuthOrderFreezeModel}
     * @return {@link AlipayFundAuthOrderFreezeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOrderFreezeResponse authOrderFreezeToResponse(AlipayFundAuthOrderFreezeModel model) throws AlipayApiException {
        AlipayFundAuthOrderFreezeRequest request = new AlipayFundAuthOrderFreezeRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 资金预授权冻结接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundAuthOrderFreezeModel}
     * @return {@link AlipayFundAuthOrderFreezeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOrderFreezeResponse authOrderFreezeToResponse(Boolean certModel, AlipayFundAuthOrderFreezeModel model) throws AlipayApiException {
        AlipayFundAuthOrderFreezeRequest request = new AlipayFundAuthOrderFreezeRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 资金授权解冻接口
     *
     * @param model {@link AlipayFundAuthOrderUnfreezeModel}
     * @return {@link AlipayFundAuthOrderUnfreezeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOrderUnfreezeResponse authOrderUnfreezeToResponse(AlipayFundAuthOrderUnfreezeModel model) throws AlipayApiException {
        AlipayFundAuthOrderUnfreezeRequest request = new AlipayFundAuthOrderUnfreezeRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 资金授权解冻接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundAuthOrderUnfreezeModel}
     * @return {@link AlipayFundAuthOrderUnfreezeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOrderUnfreezeResponse authOrderUnfreezeToResponse(Boolean certModel, AlipayFundAuthOrderUnfreezeModel model) throws AlipayApiException {
        AlipayFundAuthOrderUnfreezeRequest request = new AlipayFundAuthOrderUnfreezeRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 资金预授权冻结接口
     *
     * @param model {@link AlipayFundAuthOrderVoucherCreateModel}
     * @return {@link AlipayFundAuthOrderVoucherCreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOrderVoucherCreateResponse authOrderVoucherCreateToResponse(AlipayFundAuthOrderVoucherCreateModel model) throws AlipayApiException {
        AlipayFundAuthOrderVoucherCreateRequest request = new AlipayFundAuthOrderVoucherCreateRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 资金预授权冻结接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundAuthOrderVoucherCreateModel}
     * @return {@link AlipayFundAuthOrderVoucherCreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOrderVoucherCreateResponse authOrderVoucherCreateToResponse(Boolean certModel, AlipayFundAuthOrderVoucherCreateModel model) throws AlipayApiException {
        AlipayFundAuthOrderVoucherCreateRequest request = new AlipayFundAuthOrderVoucherCreateRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 资金授权撤销接口
     *
     * @param model {@link AlipayFundAuthOperationCancelModel}
     * @return {@link AlipayFundAuthOperationCancelResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOperationCancelResponse authOperationCancelToResponse(AlipayFundAuthOperationCancelModel model) throws AlipayApiException {
        AlipayFundAuthOperationCancelRequest request = new AlipayFundAuthOperationCancelRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 资金授权撤销接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundAuthOperationCancelModel}
     * @return {@link AlipayFundAuthOperationCancelResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOperationCancelResponse authOperationCancelToResponse(Boolean certModel, AlipayFundAuthOperationCancelModel model) throws AlipayApiException {
        AlipayFundAuthOperationCancelRequest request = new AlipayFundAuthOperationCancelRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 资金授权操作查询接口
     *
     * @param model {@link AlipayFundAuthOperationDetailQueryModel}
     * @return {@link AlipayFundAuthOperationDetailQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOperationDetailQueryResponse authOperationDetailQueryToResponse(AlipayFundAuthOperationDetailQueryModel model) throws AlipayApiException {
        AlipayFundAuthOperationDetailQueryRequest request = new AlipayFundAuthOperationDetailQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 资金授权操作查询接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundAuthOperationDetailQueryModel}
     * @return {@link AlipayFundAuthOperationDetailQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundAuthOperationDetailQueryResponse authOperationDetailQueryToResponse(Boolean certModel, AlipayFundAuthOperationDetailQueryModel model) throws AlipayApiException {
        AlipayFundAuthOperationDetailQueryRequest request = new AlipayFundAuthOperationDetailQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 红包无线支付接口
     *
     * @param model {@link AlipayFundCouponOrderAppPayModel}
     * @return {@link AlipayFundCouponOrderAppPayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderAppPayResponse fundCouponOrderAppPayToResponse(AlipayFundCouponOrderAppPayModel model) throws AlipayApiException {
        AlipayFundCouponOrderAppPayRequest request = new AlipayFundCouponOrderAppPayRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 红包无线支付接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundCouponOrderAppPayModel}
     * @return {@link AlipayFundCouponOrderAppPayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderAppPayResponse fundCouponOrderAppPayToResponse(Boolean certModel, AlipayFundCouponOrderAppPayModel model) throws AlipayApiException {
        AlipayFundCouponOrderAppPayRequest request = new AlipayFundCouponOrderAppPayRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 红包页面支付接口
     *
     * @param model {@link AlipayFundCouponOrderPagePayModel}
     * @return {@link AlipayFundCouponOrderPagePayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderPagePayResponse fundCouponOrderPagePayToResponse(AlipayFundCouponOrderPagePayModel model) throws AlipayApiException {
        AlipayFundCouponOrderPagePayRequest request = new AlipayFundCouponOrderPagePayRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 红包页面支付接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundCouponOrderPagePayModel}
     * @return {@link AlipayFundCouponOrderPagePayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderPagePayResponse fundCouponOrderPagePayToResponse(Boolean certModel, AlipayFundCouponOrderPagePayModel model) throws AlipayApiException {
        AlipayFundCouponOrderPagePayRequest request = new AlipayFundCouponOrderPagePayRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 红包协议支付接口
     *
     * @param model {@link AlipayFundCouponOrderAgreementPayModel}
     * @return {@link AlipayFundCouponOrderAgreementPayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderAgreementPayResponse fundCouponOrderAgreementPayToResponse(AlipayFundCouponOrderAgreementPayModel model) throws AlipayApiException {
        AlipayFundCouponOrderAgreementPayRequest request = new AlipayFundCouponOrderAgreementPayRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 红包协议支付接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundCouponOrderAgreementPayModel}
     * @return {@link AlipayFundCouponOrderAgreementPayResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderAgreementPayResponse fundCouponOrderAgreementPayToResponse(Boolean certModel, AlipayFundCouponOrderAgreementPayModel model) throws AlipayApiException {
        AlipayFundCouponOrderAgreementPayRequest request = new AlipayFundCouponOrderAgreementPayRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 红包打款接口
     *
     * @param model {@link AlipayFundCouponOrderDisburseModel}
     * @return {@link AlipayFundCouponOrderDisburseResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderDisburseResponse fundCouponOrderDisburseToResponse(AlipayFundCouponOrderDisburseModel model) throws AlipayApiException {
        AlipayFundCouponOrderDisburseRequest request = new AlipayFundCouponOrderDisburseRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 红包打款接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundCouponOrderDisburseModel}
     * @return {@link AlipayFundCouponOrderDisburseResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderDisburseResponse fundCouponOrderDisburseToResponse(Boolean certModel, AlipayFundCouponOrderDisburseModel model) throws AlipayApiException {
        AlipayFundCouponOrderDisburseRequest request = new AlipayFundCouponOrderDisburseRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 红包退回接口
     *
     * @param model {@link AlipayFundCouponOrderRefundModel}
     * @return {@link AlipayFundCouponOrderRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderRefundResponse fundCouponOrderRefundToResponse(AlipayFundCouponOrderRefundModel model) throws AlipayApiException {
        AlipayFundCouponOrderRefundRequest request = new AlipayFundCouponOrderRefundRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 红包退回接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundCouponOrderRefundModel}
     * @return {@link AlipayFundCouponOrderRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOrderRefundResponse fundCouponOrderRefundToResponse(Boolean certModel, AlipayFundCouponOrderRefundModel model) throws AlipayApiException {
        AlipayFundCouponOrderRefundRequest request = new AlipayFundCouponOrderRefundRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 红包退回接口
     *
     * @param model {@link AlipayFundCouponOperationQueryModel}
     * @return {@link AlipayFundCouponOperationQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOperationQueryResponse fundCouponOperationQueryToResponse(AlipayFundCouponOperationQueryModel model) throws AlipayApiException {
        AlipayFundCouponOperationQueryRequest request = new AlipayFundCouponOperationQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 红包退回接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayFundCouponOperationQueryModel}
     * @return {@link AlipayFundCouponOperationQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayFundCouponOperationQueryResponse fundCouponOperationQueryToResponse(Boolean certModel, AlipayFundCouponOperationQueryModel model) throws AlipayApiException {
        AlipayFundCouponOperationQueryRequest request = new AlipayFundCouponOperationQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 应用授权 URL 拼装
     *
     * @param appId       应用编号
     * @param redirectUri 回调 URI
     * @return 应用授权 URL
     * @throws UnsupportedEncodingException 编码异常
     */
    public String getOauth2Url(String appId, String redirectUri) throws UnsupportedEncodingException {
        return new StringBuffer().append("https://openauth.alipay.com/oauth2/appToAppAuth.htm?app_id=").append(appId).append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8")).toString();
    }

    /**
     * 使用 app_auth_code 换取 app_auth_token
     *
     * @param model {@link AlipayOpenAuthTokenAppModel}
     * @return {@link AlipayOpenAuthTokenAppResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayOpenAuthTokenAppResponse openAuthTokenAppToResponse(AlipayOpenAuthTokenAppModel model) throws AlipayApiException {
        AlipayOpenAuthTokenAppRequest request = new AlipayOpenAuthTokenAppRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 使用 app_auth_code 换取 app_auth_token
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayOpenAuthTokenAppModel}
     * @return {@link AlipayOpenAuthTokenAppResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayOpenAuthTokenAppResponse openAuthTokenAppToResponse(Boolean certModel, AlipayOpenAuthTokenAppModel model) throws AlipayApiException {
        AlipayOpenAuthTokenAppRequest request = new AlipayOpenAuthTokenAppRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 查询授权信息
     *
     * @param model {@link AlipayOpenAuthTokenAppQueryModel}
     * @return {@link AlipayOpenAuthTokenAppQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayOpenAuthTokenAppQueryResponse openAuthTokenAppQueryToResponse(AlipayOpenAuthTokenAppQueryModel model) throws AlipayApiException {
        AlipayOpenAuthTokenAppQueryRequest request = new AlipayOpenAuthTokenAppQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 查询授权信息
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayOpenAuthTokenAppQueryModel}
     * @return {@link AlipayOpenAuthTokenAppQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayOpenAuthTokenAppQueryResponse openAuthTokenAppQueryToResponse(Boolean certModel, AlipayOpenAuthTokenAppQueryModel model) throws AlipayApiException {
        AlipayOpenAuthTokenAppQueryRequest request = new AlipayOpenAuthTokenAppQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 地铁购票发码
     *
     * @param model {@link AlipayCommerceCityfacilitatorVoucherGenerateModel}
     * @return {@link AlipayCommerceCityfacilitatorVoucherGenerateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceCityfacilitatorVoucherGenerateResponse voucherGenerateToResponse(AlipayCommerceCityfacilitatorVoucherGenerateModel model) throws AlipayApiException {
        AlipayCommerceCityfacilitatorVoucherGenerateRequest request = new AlipayCommerceCityfacilitatorVoucherGenerateRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 地铁购票发码
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayCommerceCityfacilitatorVoucherGenerateModel}
     * @return {@link AlipayCommerceCityfacilitatorVoucherGenerateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceCityfacilitatorVoucherGenerateResponse voucherGenerateToResponse(Boolean certModel, AlipayCommerceCityfacilitatorVoucherGenerateModel model) throws AlipayApiException {
        AlipayCommerceCityfacilitatorVoucherGenerateRequest request = new AlipayCommerceCityfacilitatorVoucherGenerateRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 地铁购票发码退款
     *
     * @param model {@link AlipayCommerceCityfacilitatorVoucherRefundModel}
     * @return {@link AlipayCommerceCityfacilitatorVoucherRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceCityfacilitatorVoucherRefundResponse metroRefundToResponse(AlipayCommerceCityfacilitatorVoucherRefundModel model) throws AlipayApiException {
        AlipayCommerceCityfacilitatorVoucherRefundRequest request = new AlipayCommerceCityfacilitatorVoucherRefundRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 地铁购票发码退款
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayCommerceCityfacilitatorVoucherRefundModel}
     * @return {@link AlipayCommerceCityfacilitatorVoucherRefundResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceCityfacilitatorVoucherRefundResponse metroRefundToResponse(Boolean certModel, AlipayCommerceCityfacilitatorVoucherRefundModel model) throws AlipayApiException {
        AlipayCommerceCityfacilitatorVoucherRefundRequest request = new AlipayCommerceCityfacilitatorVoucherRefundRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 地铁车站数据查询
     *
     * @param model {@link AlipayCommerceCityfacilitatorStationQueryModel}
     * @return {@link AlipayCommerceCityfacilitatorStationQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceCityfacilitatorStationQueryResponse stationQueryToResponse(AlipayCommerceCityfacilitatorStationQueryModel model) throws AlipayApiException {
        AlipayCommerceCityfacilitatorStationQueryRequest request = new AlipayCommerceCityfacilitatorStationQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 地铁车站数据查询
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayCommerceCityfacilitatorStationQueryModel}
     * @return {@link AlipayCommerceCityfacilitatorStationQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceCityfacilitatorStationQueryResponse stationQueryToResponse(Boolean certModel, AlipayCommerceCityfacilitatorStationQueryModel model) throws AlipayApiException {
        AlipayCommerceCityfacilitatorStationQueryRequest request = new AlipayCommerceCityfacilitatorStationQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 核销码批量查询
     *
     * @param model {@link AlipayCommerceCityfacilitatorVoucherBatchqueryModel}
     * @return {@link AlipayCommerceCityfacilitatorVoucherBatchqueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceCityfacilitatorVoucherBatchqueryResponse voucherBatchqueryToResponse(AlipayCommerceCityfacilitatorVoucherBatchqueryModel model) throws AlipayApiException {
        AlipayCommerceCityfacilitatorVoucherBatchqueryRequest request = new AlipayCommerceCityfacilitatorVoucherBatchqueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 核销码批量查询
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayCommerceCityfacilitatorVoucherBatchqueryModel}
     * @return {@link AlipayCommerceCityfacilitatorVoucherBatchqueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceCityfacilitatorVoucherBatchqueryResponse voucherBatchqueryToResponse(Boolean certModel, AlipayCommerceCityfacilitatorVoucherBatchqueryModel model) throws AlipayApiException {
        AlipayCommerceCityfacilitatorVoucherBatchqueryRequest request = new AlipayCommerceCityfacilitatorVoucherBatchqueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }


    public void batchTrans(Map<String, String> params, String privateKey, String signType, HttpServletResponse response) throws IOException {
        params.put("service", "batch_trans_notify");
        params.put("_input_charset", "UTF-8");
        params.put("pay_date", DateKit.format(new Date(), "YYYYMMDD"));
        Map<String, String> param = AliPayBuilder.buildRequestPara(params, privateKey, signType);
        response.sendRedirect(Registry.ALIPAY.service().concat(AliPayBuilder.createLinkString(param)));
    }

    /**
     * 生活缴费查询账单
     *
     * @param orderType       支付宝订单类型
     * @param merchantOrderNo 业务流水号
     * @return {@link AlipayEbppBillGetResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayEbppBillGetResponse ebppBillGet(String orderType, String merchantOrderNo) throws AlipayApiException {
        AlipayEbppBillGetRequest request = new AlipayEbppBillGetRequest();
        request.setOrderType(orderType);
        request.setMerchantOrderNo(merchantOrderNo);
        return doExecute(request);
    }

    /**
     * 生活缴费查询账单
     *
     * @param certModel       是否证书模式
     * @param orderType       支付宝订单类型
     * @param merchantOrderNo 业务流水号
     * @return {@link AlipayEbppBillGetResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayEbppBillGetResponse ebppBillGet(Boolean certModel, String orderType, String merchantOrderNo) throws AlipayApiException {
        AlipayEbppBillGetRequest request = new AlipayEbppBillGetRequest();
        request.setOrderType(orderType);
        request.setMerchantOrderNo(merchantOrderNo);
        return doExecute(certModel, request);
    }

    /**
     * H5刷脸认证初始化
     *
     * @param model {@link ZolozIdentificationUserWebInitializeModel}
     * @return {@link ZolozIdentificationUserWebInitializeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozIdentificationUserWebInitializeResponse identificationUserWebInitialize(ZolozIdentificationUserWebInitializeModel model) throws AlipayApiException {
        ZolozIdentificationUserWebInitializeRequest request = new ZolozIdentificationUserWebInitializeRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * H5刷脸认证初始化
     *
     * @param certModel    是否证书模式
     * @param model        {@link ZolozIdentificationUserWebInitializeModel}
     * @return {@link ZolozIdentificationUserWebInitializeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozIdentificationUserWebInitializeResponse identificationUserWebInitialize(Boolean certModel, ZolozIdentificationUserWebInitializeModel model) throws AlipayApiException {
        ZolozIdentificationUserWebInitializeRequest request = new ZolozIdentificationUserWebInitializeRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * H5刷脸认证查询
     *
     * @param model {@link ZolozIdentificationUserWebQueryModel}
     * @return {@link ZolozIdentificationUserWebQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozIdentificationUserWebQueryResponse identificationUserWebInitialize(ZolozIdentificationUserWebQueryModel model) throws AlipayApiException {
        ZolozIdentificationUserWebQueryRequest request = new ZolozIdentificationUserWebQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * H5刷脸认证查询
     *
     * @param certModel    是否证书模式
     * @param model        {@link ZolozIdentificationUserWebQueryModel}
     * @return {@link ZolozIdentificationUserWebQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozIdentificationUserWebQueryResponse identificationUserWebInitialize(Boolean certModel, ZolozIdentificationUserWebQueryModel model) throws AlipayApiException {
        ZolozIdentificationUserWebQueryRequest request = new ZolozIdentificationUserWebQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 人脸入库
     *
     * @param model {@link ZolozAuthenticationCustomerFacemanageCreateModel}
     * @return {@link ZolozAuthenticationCustomerFacemanageCreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationCustomerFacemanageCreateResponse authenticationCustomerFaceManageCreate(ZolozAuthenticationCustomerFacemanageCreateModel model) throws AlipayApiException {
        ZolozAuthenticationCustomerFacemanageCreateRequest request = new ZolozAuthenticationCustomerFacemanageCreateRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 人脸入库
     *
     * @param certModel    是否证书模式
     * @param model        {@link ZolozAuthenticationCustomerFacemanageCreateModel}
     * @return {@link ZolozAuthenticationCustomerFacemanageCreateResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationCustomerFacemanageCreateResponse authenticationCustomerFaceManageCreate(Boolean certModel, ZolozAuthenticationCustomerFacemanageCreateModel model) throws AlipayApiException {
        ZolozAuthenticationCustomerFacemanageCreateRequest request = new ZolozAuthenticationCustomerFacemanageCreateRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 人脸出库
     *
     * @param model {@link ZolozAuthenticationCustomerFacemanageDeleteModel}
     * @return {@link ZolozAuthenticationCustomerFacemanageDeleteResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationCustomerFacemanageDeleteResponse authenticationCustomerFaceManageDelete(ZolozAuthenticationCustomerFacemanageDeleteModel model) throws AlipayApiException {
        ZolozAuthenticationCustomerFacemanageDeleteRequest request = new ZolozAuthenticationCustomerFacemanageDeleteRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 人脸出库
     *
     * @param certModel    是否证书模式
     * @param model        {@link ZolozAuthenticationCustomerFacemanageDeleteModel}
     * @return {@link ZolozAuthenticationCustomerFacemanageDeleteResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationCustomerFacemanageDeleteResponse authenticationCustomerFaceManageDelete(Boolean certModel, ZolozAuthenticationCustomerFacemanageDeleteModel model) throws AlipayApiException {
        ZolozAuthenticationCustomerFacemanageDeleteRequest request = new ZolozAuthenticationCustomerFacemanageDeleteRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 人脸 ftoken 查询消费接口
     *
     * @param model {@link ZolozAuthenticationCustomerFtokenQueryModel}
     * @return {@link ZolozAuthenticationCustomerFtokenQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationCustomerFtokenQueryResponse authenticationCustomerFTokenQuery(ZolozAuthenticationCustomerFtokenQueryModel model) throws AlipayApiException {
        ZolozAuthenticationCustomerFtokenQueryRequest request = new ZolozAuthenticationCustomerFtokenQueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 人脸 ftoken 查询消费接口
     *
     * @param certModel    是否证书模式
     * @param model        {@link ZolozAuthenticationCustomerFtokenQueryModel}
     * @return {@link ZolozAuthenticationCustomerFtokenQueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationCustomerFtokenQueryResponse authenticationCustomerFTokenQuery(Boolean certModel, ZolozAuthenticationCustomerFtokenQueryModel model) throws AlipayApiException {
        ZolozAuthenticationCustomerFtokenQueryRequest request = new ZolozAuthenticationCustomerFtokenQueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 人脸初始化刷脸付
     *
     * @param model {@link ZolozAuthenticationSmilepayInitializeModel}
     * @return {@link ZolozAuthenticationSmilepayInitializeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationSmilepayInitializeResponse authenticationSmilePayInitialize(ZolozAuthenticationSmilepayInitializeModel model) throws AlipayApiException {
        ZolozAuthenticationSmilepayInitializeRequest request = new ZolozAuthenticationSmilepayInitializeRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 人脸初始化刷脸付
     *
     * @param certModel    是否证书模式
     * @param model        {@link ZolozAuthenticationSmilepayInitializeModel}
     * @return {@link ZolozAuthenticationSmilepayInitializeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationSmilepayInitializeResponse authenticationSmilePayInitialize(Boolean certModel, ZolozAuthenticationSmilepayInitializeModel model) throws AlipayApiException {
        ZolozAuthenticationSmilepayInitializeRequest request = new ZolozAuthenticationSmilepayInitializeRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 人脸初始化唤起zim
     *
     * @param model {@link ZolozAuthenticationCustomerSmilepayInitializeModel}
     * @return {@link ZolozAuthenticationCustomerSmilepayInitializeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationCustomerSmilepayInitializeResponse authenticationCustomerSmilePayInitialize(ZolozAuthenticationCustomerSmilepayInitializeModel model) throws AlipayApiException {
        ZolozAuthenticationCustomerSmilepayInitializeRequest request = new ZolozAuthenticationCustomerSmilepayInitializeRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 人脸初始化唤起zim
     *
     * @param certModel    是否证书模式
     * @param model        {@link ZolozAuthenticationCustomerSmilepayInitializeModel}
     * @return {@link ZolozAuthenticationCustomerSmilepayInitializeResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public ZolozAuthenticationCustomerSmilepayInitializeResponse authenticationCustomerSmilePayInitialize(Boolean certModel, ZolozAuthenticationCustomerSmilepayInitializeModel model) throws AlipayApiException {
        ZolozAuthenticationCustomerSmilepayInitializeRequest request = new ZolozAuthenticationCustomerSmilepayInitializeRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 生态激励项目ISV代签约接口
     *
     * @return {@link AlipayCommerceAdContractSignResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceAdContractSignResponse commerceAdContractSign() throws AlipayApiException {
        AlipayCommerceAdContractSignRequest request = new AlipayCommerceAdContractSignRequest();
        return doExecute(request);
    }

    /**
     * 生态激励项目ISV代签约接口
     *
     * @param certModel    是否证书模式
     * @return {@link AlipayCommerceAdContractSignResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayCommerceAdContractSignResponse commerceAdContractSign(Boolean certModel) throws AlipayApiException {
        AlipayCommerceAdContractSignRequest request = new AlipayCommerceAdContractSignRequest();
        return doExecute(certModel, request);
    }

    /**
     * 分账关系绑定
     *
     * @param model {@link AlipayTradeRoyaltyRelationBindModel}
     * @return {@link AlipayTradeRoyaltyRelationBindResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRoyaltyRelationBindResponse tradeRoyaltyRelationBind(AlipayTradeRoyaltyRelationBindModel model) throws AlipayApiException {
        AlipayTradeRoyaltyRelationBindRequest request = new AlipayTradeRoyaltyRelationBindRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 分账关系绑定
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeRoyaltyRelationBindModel}
     * @return {@link AlipayTradeRoyaltyRelationBindResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRoyaltyRelationBindResponse tradeRoyaltyRelationBind(Boolean certModel, AlipayTradeRoyaltyRelationBindModel model) throws AlipayApiException {
        AlipayTradeRoyaltyRelationBindRequest request = new AlipayTradeRoyaltyRelationBindRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 分账关系解绑
     *
     * @param model {@link AlipayTradeRoyaltyRelationUnbindModel}
     * @return {@link AlipayTradeRoyaltyRelationUnbindResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRoyaltyRelationUnbindResponse tradeRoyaltyRelationUnBind(AlipayTradeRoyaltyRelationUnbindModel model) throws AlipayApiException {
        AlipayTradeRoyaltyRelationUnbindRequest request = new AlipayTradeRoyaltyRelationUnbindRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 分账关系解绑
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeRoyaltyRelationUnbindModel}
     * @return {@link AlipayTradeRoyaltyRelationUnbindResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRoyaltyRelationUnbindResponse tradeRoyaltyRelationUnBind(Boolean certModel, AlipayTradeRoyaltyRelationUnbindModel model) throws AlipayApiException {
        AlipayTradeRoyaltyRelationUnbindRequest request = new AlipayTradeRoyaltyRelationUnbindRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    /**
     * 分账关系查询
     *
     * @param model {@link AlipayTradeRoyaltyRelationBatchqueryModel}
     * @return {@link AlipayTradeRoyaltyRelationBatchqueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRoyaltyRelationBatchqueryResponse tradeRoyaltyRelationBatchQuery(AlipayTradeRoyaltyRelationBatchqueryModel model) throws AlipayApiException {
        AlipayTradeRoyaltyRelationBatchqueryRequest request = new AlipayTradeRoyaltyRelationBatchqueryRequest();
        request.setBizModel(model);
        return doExecute(request);
    }

    /**
     * 分账关系查询
     *
     * @param certModel    是否证书模式
     * @param model        {@link AlipayTradeRoyaltyRelationBatchqueryModel}
     * @return {@link AlipayTradeRoyaltyRelationBatchqueryResponse}
     * @throws AlipayApiException 支付宝 Api 异常
     */
    public AlipayTradeRoyaltyRelationBatchqueryResponse tradeRoyaltyRelationBatchQuery(Boolean certModel, AlipayTradeRoyaltyRelationBatchqueryModel model) throws AlipayApiException {
        AlipayTradeRoyaltyRelationBatchqueryRequest request = new AlipayTradeRoyaltyRelationBatchqueryRequest();
        request.setBizModel(model);
        return doExecute(certModel, request);
    }

    public <T extends AlipayResponse> T doExecute(AlipayRequest<T> request) throws AlipayApiException {
        if (this.context.isCertMode()) {
            return certificateExecute(request);
        }
        return this.client.execute(request);
    }

    public <T extends AlipayResponse> T doExecute(Boolean certModel, AlipayRequest<T> request) throws AlipayApiException {
        if (this.client == null) {
            throw new IllegalStateException("aliPayClient 未被初始化");
        }
        if (certModel) {
            return certificateExecute(request);
        }
        return this.client.execute(request);
    }

    public <T extends AlipayResponse> T doExecute(Boolean certModel, AlipayRequest<T> request, String authToken) throws AlipayApiException {
        if (this.client == null) {
            throw new IllegalStateException("aliPayClient 未被初始化");
        }
        if (certModel) {
            return certificateExecute(request, authToken);
        }
        return execute(request, authToken);
    }

    public <T extends AlipayResponse> T doExecute(AlipayRequest<T> request, String authToken) throws AlipayApiException {
        if (this.client == null) {
            throw new IllegalStateException("aliPayClient 未被初始化");
        }
        if (this.context.isCertMode()) {
            return certificateExecute(request, authToken);
        }
        return execute(request, authToken);
    }

    public <T extends AlipayResponse> T execute(AlipayRequest<T> request, String authToken) throws AlipayApiException {
        return this.client.execute(request, authToken);
    }

    public <T extends AlipayResponse> T execute(AlipayRequest<T> request, String accessToken, String appAuthToken) throws AlipayApiException {
        return this.client.execute(request, accessToken, appAuthToken);
    }

    public <T extends AlipayResponse> T execute(AlipayRequest<T> request, String accessToken, String appAuthToken, String targetAppId) throws AlipayApiException {
        return this.client.execute(request, accessToken, appAuthToken, targetAppId);
    }

    public <T extends AlipayResponse> T pageExecute(AlipayRequest<T> request) throws AlipayApiException {
        return this.client.pageExecute(request);
    }

    public <T extends AlipayResponse> T pageExecute(AlipayRequest<T> request, String method) throws AlipayApiException {
        return this.client.pageExecute(request, method);
    }

    public <T extends AlipayResponse> T sdkExecute(AlipayRequest<T> request) throws AlipayApiException {
        return this.client.sdkExecute(request);
    }

    public <T extends AlipayResponse> T certificateExecute(AlipayRequest<T> request) throws AlipayApiException {
        return this.client.certificateExecute(request);
    }

    public <T extends AlipayResponse> T certificateExecute(AlipayRequest<T> request, String authToken) throws AlipayApiException {
        return this.client.certificateExecute(request, authToken);
    }

    public <T extends AlipayResponse> T certificateExecute(AlipayRequest<T> request, String accessToken, String appAuthToken) throws AlipayApiException {
        return this.client.certificateExecute(request, accessToken, appAuthToken);
    }

    public <T extends AlipayResponse> T certificateExecute(AlipayRequest<T> request, String accessToken, String appAuthToken, String targetAppId) throws AlipayApiException {
        return this.client.certificateExecute(request, accessToken, appAuthToken, targetAppId);
    }

}
