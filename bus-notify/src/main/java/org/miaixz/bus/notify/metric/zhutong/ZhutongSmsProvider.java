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
package org.miaixz.bus.notify.metric.zhutong;

import org.miaixz.bus.core.basics.entity.Message;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 助通短信发送
 * 1. 自定义短信发送 无需定义模板 https://doc.zthysms.com/web/#/1/14
 * 2. 模板短信发送  需定义模板   https://doc.zthysms.com/web/#/1/13
 * appKey           ：username  助通终端用户管理的用户名，非登录账号密码
 * appSecret        ：password  终端用户管理的密码
 * signature        ：短信签名可以为空，为空发送【自定义短信】无需要提前创建短信模板; 不为空发送:【模板短信】
 * templateId       ：模板id可以为空，为空发送【自定义短信】无需要提前创建短信模板; 不为空发送:【模板短信】
 * templateName     ：模板变量名称可以为空，为空发送【自定义短信】无需要提前创建短信模板; 不为空发送:【模板短信】
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZhutongSmsProvider extends AbstractProvider<ZhutongMaterial, Context> {

    /**
     * 构造器，用于构造短信实现模块
     */
    public ZhutongSmsProvider(Context context) {
        super(context);
    }

    @Override
    public Message send(ZhutongMaterial entity) {
        // 如果模板id为空 or 模板变量名称为空，使用无模板的自定义短信发送
        if (ArrayKit.hasBlank(entity.getSignature(), entity.getTemplate(), entity.getTemplateName())) {
            return sendForCustom(entity);
        }

        return sendForTemplate(entity);
    }

    /**
     * 发送 自定义短信：https://doc.zthysms.com/web/#/1/14
     */
    protected Message sendForCustom(ZhutongMaterial entity) {
        String requestUrl = this.getUrl(entity);
        String username = this.context.getAppKey();
        String password = this.context.getAppSecret();

        validator(requestUrl, username, password);
        if (StringKit.isEmpty(entity.getReceive())) {
            throw new ValidateException("助通短信：手机号不能为空！");
        }
        if (entity.getReceive().length() >= 20000) {
            throw new ValidateException("助通短信：手机号码最多支持2000个！");
        }
        if (StringKit.isBlank(entity.getContent())) {
            throw new ValidateException("助通短信：发送内容不能为空！");
        }
        if (entity.getContent().length() >= 1000) {
            throw new ValidateException("助通短信：发送内容不能超过1000个字符！");
        }
        if (!entity.getContent().contains("【")) {
            throw new ValidateException("助通短信：自定义短信发送内容必须包含签名信息，如：【助通科技】您的验证码是8888！");
        }


        String url = this.getUrl(entity) + "v2/sendSms";
        long tKey = System.currentTimeMillis() / 1000;
        Map<String, String> bodys = new HashMap<>(5);
        //账号
        bodys.put("username", username);
        //密码
        bodys.put("password", Builder.md5(Builder.md5(password) + tKey));
        //tKey
        bodys.put("tKey", tKey + "");
        //手机号
        bodys.put("mobile", entity.getReceive());
        //内容
        bodys.put("content", entity.getContent());

        Map<String, String> headers = MapKit.newHashMap(1, true);
        headers.put("Content-Type", MediaType.APPLICATION_JSON);

        String response = Httpx.post(url, bodys, headers);

        boolean succeed = Objects.equals(JsonKit.getValue(response, "code"), 0);
        String errcode = succeed ? ErrorCode.SUCCESS.getCode() : ErrorCode.FAILURE.getCode();
        String errmsg = succeed ? ErrorCode.SUCCESS.getDesc() : ErrorCode.FAILURE.getDesc();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

    /**
     * 发送 模板短信：https://doc.zthysms.com/web/#/1/13
     */
    protected Message sendForTemplate(ZhutongMaterial entity) {
        validator(this.getUrl(entity), this.context.getAppKey(), this.context.getAppSecret());
        if (StringKit.isBlank(entity.getSignature())) {
            throw new InternalException("助通短信：模板短信中已报备的签名signature不能为空！");
        }

        if (StringKit.isBlank(entity.getTemplate())) {
            throw new InternalException("助通短信：模板短信模板id不能为空！！");
        }

        //地址
        String url = this.getUrl(entity) + "v2/sendSmsTp";
        //请求入参
        Map<String, String> bodys = new HashMap<>();
        //账号
        bodys.put("username", this.context.getAppKey());
        //tKey
        long tKey = System.currentTimeMillis() / 1000;
        bodys.put("tKey", String.valueOf(tKey));
        //明文密码
        bodys.put("password", Builder.md5(Builder.md5(this.context.getAppSecret()) + tKey));
        //模板ID
        bodys.put("tpId", entity.getTemplate());
        //签名
        bodys.put("signature", entity.getSignature());
        //扩展号
        bodys.put("ext", "");
        //自定义参数
        bodys.put("extend", "");
        //发送记录集合
        Map records = new HashMap<>();

        for (String mobile : StringKit.split(entity.getReceive(), Symbol.COMMA)) {
            Map<String, String> record = new HashMap<>();
            //手机号
            record.put("mobile", mobile);
            record.put("tpContent", entity.getContent());
            records.putAll(record);
        }

        bodys.put("records", JsonKit.toJsonString(records));

        Map<String, String> headers = MapKit.newHashMap(1, true);
        headers.put("Content-Type", MediaType.APPLICATION_JSON);
        String response = Httpx.post(url, bodys, headers);

        boolean succeed = Objects.equals(JsonKit.getValue(response, "code"), 0);
        String errcode = succeed ? ErrorCode.SUCCESS.getCode() : ErrorCode.FAILURE.getCode();
        String errmsg = succeed ? ErrorCode.SUCCESS.getDesc() : ErrorCode.FAILURE.getDesc();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

    private void validator(String requestUrl, String username, String password) {
        if (StringKit.isBlank(requestUrl)) {
            throw new ValidateException("助通短信：requestUrl不能为空！");
        }
        if (!requestUrl.endsWith("/")) {
            throw new ValidateException("助通短信：requestUrl必须以'/'结尾!");
        }
        if (ArrayKit.hasBlank(username, password)) {
            throw new ValidateException("助通短信：账号username、密码password不能为空！");
        }
    }

}
