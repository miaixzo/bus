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
package org.miaixz.bus.pay.metric.wechat.entity.v3;

import lombok.*;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.metric.wechat.entity.Receiver;

import java.util.List;

/**
 * V3 分账
 * 支持: 请求单次分账、请求多次分账、添加分账接收方、删除分账接收方、完结分账
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitSharing extends Material {

    /**
     * 微信支付分配的服务商商户号,兼容V2接口
     */
    private String mch_id;
    /**
     * 微信支付分配的子商户号，即分账的出资商户号
     */
    private String sub_mchid;
    /**
     * 微信分配的子商户公众账号ID，分账接收方类型包含PERSONAL_SUB_OPENID时必填
     */
    private String sub_appid;
    /**
     * 微信支付订单号
     */
    private String transaction_id;
    /**
     * 服务商系统内部的分账单号，在服务商系统内部唯一，同一分账单号多次请求等同一次。只能是数字、大小写字母_-|*@
     */
    private String out_order_no;
    /**
     * 分账接收方列表，可以设置出资商户作为分账接受方，最多可有50个分账接收方
     */
    private List<Receiver> receivers;
    /**
     * 是否解冻剩余未分资金
     * 1、如果为true，该笔订单剩余未分账的金额会解冻回分账方商户；
     * 2、如果为false，该笔订单剩余未分账的金额不会解冻回分账方商户，可以对该笔订单再次进行分账。
     */
    private boolean unfreeze_unsplit;

}
