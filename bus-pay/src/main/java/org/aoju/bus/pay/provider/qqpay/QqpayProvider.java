package org.aoju.bus.pay.provider.qqpay;

import org.aoju.bus.pay.metric.HttpKit;
import org.aoju.bus.pay.metric.WxPayKit;

import java.io.InputStream;
import java.util.Map;

/**
 * QQ 钱包支付 API
 */
public class QqpayProvider {

    /**
     * 提交付款码支付
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String microPay(Map<String, String> params) {
        return doPost(ApiUrl.MICRO_PAY_URL, params);
    }

    /**
     * 统一下单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String unifiedOrder(Map<String, String> params) {
        return doPost(ApiUrl.UNIFIED_ORDER_URL, params);
    }

    /**
     * 订单查询
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String orderQuery(Map<String, String> params) {
        return doPost(ApiUrl.ORDER_QUERY_URL, params);
    }

    /**
     * 关闭订单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String closeOrder(Map<String, String> params) {
        return doPost(ApiUrl.CLOSE_ORDER_URL, params);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param cerPath  证书文件目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String orderReverse(Map<String, String> params, String cerPath, String certPass) {
        return doPost(ApiUrl.ORDER_REVERSE_URL, params, cerPath, certPass);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String orderReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(ApiUrl.ORDER_REVERSE_URL, params, certFile, certPass);
    }

    /**
     * 申请退款
     *
     * @param params   请求参数
     * @param cerPath  证书文件目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String orderRefund(Map<String, String> params, String cerPath, String certPass) {
        return doPost(ApiUrl.ORDER_REFUND_URL, params, cerPath, certPass);
    }

    /**
     * 申请退款
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String orderRefund(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(ApiUrl.ORDER_REFUND_URL, params, certFile, certPass);
    }

    /**
     * 退款查询
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String refundQuery(Map<String, String> params) {
        return doPost(ApiUrl.REFUND_QUERY_URL, params);
    }

    /**
     * 对账单下载
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String downloadBill(Map<String, String> params) {
        return doPost(ApiUrl.DOWNLOAD_BILL_URL, params);
    }

    /**
     * 创建现金红包
     *
     * @param params   请求参数
     * @param cerPath  证书文件目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String createReadPack(Map<String, String> params, String cerPath, String certPass) {
        return doPost(ApiUrl.CREATE_READ_PACK_URL, params, cerPath, certPass);
    }

    /**
     * 创建现金红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String createReadPack(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(ApiUrl.CREATE_READ_PACK_URL, params, certFile, certPass);
    }

    /**
     * 查询红包详情
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String getHbInfo(Map<String, String> params) {
        return doPost(ApiUrl.GET_HB_INFO_URL, params);
    }

    /**
     * 下载红包对账单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String downloadHbBill(Map<String, String> params) {
        return doPost(ApiUrl.DOWNLOAD_HB_BILL_URL, params);
    }

    /**
     * 企业付款到余额
     *
     * @param params   请求参数
     * @param cerPath  证书文件目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String transfer(Map<String, String> params, String cerPath, String certPass) {
        return doPost(ApiUrl.TRANSFER_URL, params, cerPath, certPass);
    }

    /**
     * 企业付款到余额
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String transfer(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(ApiUrl.TRANSFER_URL, params, certFile, certPass);
    }

    /**
     * 查询企业付款
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String getTransferInfo(Map<String, String> params) {
        return doPost(ApiUrl.GET_TRANSFER_INFO_URL, params);
    }

    /**
     * 下载企业付款对账单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String downloadTransferBill(Map<String, String> params) {
        return doPost(ApiUrl.DOWNLOAD_TRANSFER_BILL_URL, params);
    }


    public static String doPost(String url, Map<String, String> params) {
        return HttpKit.post(url, WxPayKit.toXml(params));
    }

    public static String doPost(String url, Map<String, String> params, String certPath, String certPass) {
        return HttpKit.post(url, WxPayKit.toXml(params), certPath, certPass);
    }

    public static String doPost(String url, Map<String, String> params, InputStream certFile, String certPass) {
        return HttpKit.post(url, WxPayKit.toXml(params), certFile, certPass);
    }

}
