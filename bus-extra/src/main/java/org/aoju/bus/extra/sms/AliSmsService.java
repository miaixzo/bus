/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.extra.sms;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.AllArgsConstructor;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;

/**
 * 阿里云 短信服务
 * 通过该注入该类实现发送短信
 *
 * @author Kimi Liu
 * @version 5.8.1
 * @since JDK 1.8+
 */
@AllArgsConstructor
public class AliSmsService {

    /**
     * 阿里云国际短信产品名称
     */
    private static final String ALIYUN_PRODUCT = "Dysmsapi";
    /**
     * 阿里云国际短信产品域名
     */
    private static final String ALIYUN_PRODUCT_DOMAIN = "dysmsapi.aliyuncs.com";
    /**
     * 发送成功后返回code
     */
    private static final String SUCCESS_RESULT = "OK";
    /**
     * RAM账号的AccessKey ID
     */
    private String accessKey;
    /**
     * RAM账号Access Key Secret
     */
    private String accessSecret;
    /**
     * 短信签名
     */
    private String signName;
    /**
     * 短信环境
     */
    private String profile;
    /**
     * 短信发送连接超时时长
     */
    private Integer connectionTimeout;
    /**
     * 短信接收消息连接超时时长
     */
    private Integer readTimeout;

    /**
     * 发送短信逻辑处理
     *
     * @param entity 请求对象
     * @return 处理结果响应
     * @throws InstrumentException 异常信息
     */
    public AliSmsResult send(AliSmsEntity entity) throws InstrumentException {
        try {
            IClientProfile profile = DefaultProfile.getProfile(StringUtils.toString(this.profile, "default"), this.accessKey, this.accessSecret);
            DefaultProfile.addEndpoint(this.profile, ALIYUN_PRODUCT, ALIYUN_PRODUCT_DOMAIN);
            IAcsClient acsClient = new DefaultAcsClient(profile);

            SendSmsRequest sendSmsRequest = new SendSmsRequest();
            sendSmsRequest.setReadTimeout(this.readTimeout == null ? 10000 : this.readTimeout);
            sendSmsRequest.setConnectTimeout(this.connectionTimeout == null ? 10000 : this.connectionTimeout);
            sendSmsRequest.setPhoneNumbers(entity.getPhone());
            sendSmsRequest.setSignName(this.signName);
            sendSmsRequest.setTemplateCode(entity.getTemplateCode());
            sendSmsRequest.setTemplateParam(JSON.toJSONString(entity.getParams()));
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(sendSmsRequest);
            return AliSmsResult.builder().success(SUCCESS_RESULT.equals(sendSmsResponse.getCode())).build();
        } catch (Exception e) {
            throw new InstrumentException("短信验证码发送送异常" + e.getMessage());
        }
    }

}
