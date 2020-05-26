/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.extra.mail;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dm.model.v20151123.SingleSendMailRequest;
import com.aliyuncs.dm.model.v20151123.SingleSendMailResponse;
import com.aliyuncs.profile.DefaultProfile;
import lombok.AllArgsConstructor;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;

/**
 * 阿里云邮件服务
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
@AllArgsConstructor
public class AliMailService {

    /**
     * 邮件地址连接","分割
     */
    static final String JOIN_SPLIT = Symbol.COMMA;
    /**
     * Access Key
     */
    private String accessKey;
    /**
     * Access Secret
     */
    private String accessSecret;
    /**
     * Acs客户端支持实例
     */
    private IAcsClient client;
    /**
     * 在管理控制台中配置的邮件地址
     */
    private String accountName;
    /**
     * 使用管理控制台中配置的应答地址(必须验证状态)
     */
    private boolean replyToAddress;
    /**
     * 随机帐户的范围从0到1:0，地址从1
     */
    private int addressType;
    /**
     * Mail Region
     */
    private AliMailRegion region;

    /**
     * 发送邮件逻辑处理
     *
     * @param entity 请求对象
     * @return 处理结果响应
     * @throws InstrumentException 异常信息
     */
    public AliMailResult send(AliMailEntity entity) throws InstrumentException {
        try {
            this.client = new DefaultAcsClient(
                    DefaultProfile.getProfile(
                            this.region.getValue(),
                            this.accessKey,
                            this.accessSecret
                    )
            );

            if (StringKit.isEmpty(entity.getContent())) {
                throw new InstrumentException("Email content cannot be empty");
            } else if (CollKit.isEmpty(entity.getToAddress())) {
                throw new InstrumentException("Email address cannot be empty");
            } else if (StringKit.isEmpty(entity.getSubject())) {
                throw new InstrumentException("Email subject cannot be empty");
            }

            SingleSendMailRequest request = new SingleSendMailRequest();
            request.setAccountName(accountName);
            request.setFromAlias(entity.getFormAlias());
            request.setAddressType(addressType);

            if (!StringKit.isEmpty(entity.getTagName())) {
                request.setTagName(entity.getTagName());
            }
            request.setReplyToAddress(replyToAddress);
            request.setToAddress(String.join(JOIN_SPLIT, entity.getToAddress()));
            request.setSubject(entity.getSubject());
            switchBody(request, entity.getContent(), entity.getContentType());

            SingleSendMailResponse response = client.getAcsResponse(request);

            return AliMailResult.builder().success(!StringKit.isEmpty(response.getRequestId())).build();
        } catch (Exception e) {
            throw new InstrumentException(e.getMessage(), e);
        }
    }

    /**
     * 如果contentType为空，默认使用文本
     *
     * @param request     邮件请求
     * @param contentType 文本类型
     */
    private void switchBody(SingleSendMailRequest request,
                            String content,
                            AliMailEntity.ContentType contentType) {
        switch (contentType) {
            case HTML:
                request.setHtmlBody(content);
                break;
            default:
                request.setTextBody(content);
                break;
        }
    }

}
