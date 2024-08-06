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
package org.miaixz.bus.pay.metric.tenpay;

import java.io.InputStream;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.tenpay.api.TenpayApi;

/**
 * QQ 钱包支付
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TenpayProvider extends AbstractProvider<Material, Context> {

    public TenpayProvider(Context context) {
        super(context);
    }

    public TenpayProvider(Context context, Complex complex) {
        super(context, complex);
    }

    public TenpayProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
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
     * @param complex {@link TenpayApi} 支付 API 接口枚举
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.TENPAY.sandbox() : Registry.TENPAY.service()).concat(complex.method());
    }

    /**
     * 提交付款码支付
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String microPay(Map<String, String> params) {
        return doPost(TenpayApi.MICRO_PAY_URL, params);
    }

    /**
     * 统一下单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String unifiedOrder(Map<String, String> params) {
        return doPost(TenpayApi.UNIFIED_ORDER_URL, params);
    }

    /**
     * 订单查询
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String orderQuery(Map<String, String> params) {
        return doPost(TenpayApi.ORDER_QUERY_URL, params);
    }

    /**
     * 关闭订单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String closeOrder(Map<String, String> params) {
        return doPost(TenpayApi.CLOSE_ORDER_URL, params);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param cerPath  证书文件目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderReverse(Map<String, String> params, String cerPath, String certPass) {
        return doPost(TenpayApi.ORDER_REVERSE_URL, params, cerPath, certPass);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(TenpayApi.ORDER_REVERSE_URL, params, certFile, certPass);
    }

    /**
     * 申请退款
     *
     * @param params   请求参数
     * @param cerPath  证书文件目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderRefund(Map<String, String> params, String cerPath, String certPass) {
        return doPost(TenpayApi.ORDER_REFUND_URL, params, cerPath, certPass);
    }

    /**
     * 申请退款
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderRefund(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(TenpayApi.ORDER_REFUND_URL, params, certFile, certPass);
    }

    /**
     * 退款查询
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String refundQuery(Map<String, String> params) {
        return doPost(TenpayApi.REFUND_QUERY_URL, params);
    }

    /**
     * 对账单下载
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String downloadBill(Map<String, String> params) {
        return doPost(TenpayApi.DOWNLOAD_BILL_URL, params);
    }

    /**
     * 创建现金红包
     *
     * @param params   请求参数
     * @param cerPath  证书文件目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String createReadPack(Map<String, String> params, String cerPath, String certPass) {
        return doPost(TenpayApi.CREATE_READ_PACK_URL, params, cerPath, certPass);
    }

    /**
     * 创建现金红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String createReadPack(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(TenpayApi.CREATE_READ_PACK_URL, params, certFile, certPass);
    }

    /**
     * 查询红包详情
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String getHbInfo(Map<String, String> params) {
        return doPost(TenpayApi.GET_HB_INFO_URL, params);
    }

    /**
     * 下载红包对账单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String downloadHbBill(Map<String, String> params) {
        return doPost(TenpayApi.DOWNLOAD_HB_BILL_URL, params);
    }

    /**
     * 企业付款到余额
     *
     * @param params   请求参数
     * @param cerPath  证书文件目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String transfer(Map<String, String> params, String cerPath, String certPass) {
        return doPost(TenpayApi.TRANSFER_URL, params, cerPath, certPass);
    }

    /**
     * 企业付款到余额
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String transfer(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(TenpayApi.TRANSFER_URL, params, certFile, certPass);
    }

    /**
     * 查询企业付款
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String getTransferInfo(Map<String, String> params) {
        return doPost(TenpayApi.GET_TRANSFER_INFO_URL, params);
    }

    /**
     * 下载企业付款对账单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String downloadTransferBill(Map<String, String> params) {
        return doPost(TenpayApi.DOWNLOAD_TRANSFER_BILL_URL, params);
    }

    public String doPost(Complex complex, Map<String, String> params) {
        return post(getUrl(complex), XmlKit.mapToXmlString(params));
    }

    public String doPost(Complex complex, Map<String, String> params, String certPath, String certPass) {
        return post(getUrl(complex), XmlKit.mapToXmlString(params), certPath, certPass, null);
    }

    public String doPost(Complex complex, Map<String, String> params, InputStream certFile, String certPass) {
        return post(getUrl(complex), XmlKit.mapToXmlString(params), certFile, certPass, null);
    }

}
