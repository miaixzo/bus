package org.aoju.bus.notify.provider.dingtalk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.notify.AbstractProvider;
import org.aoju.bus.notify.magic.Message;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 钉钉通知
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Setter
public class DingTalkCropMsgProvider extends AbstractProvider<DingTalkCropMsgTemplate, DingTalkProperties> {

    private static final String SUCCESS_RESULT = "200";
    private static final String TOKEN_API = "https://oapi.dingtalk.com/gettoken";
    private static final String NOTIFY_API = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";
    private AtomicReference<String> accessToken = new AtomicReference<>();
    private long refreshTokenTime;
    private long tokenTimeOut = Duration.ofSeconds(7000).toMillis();

    public DingTalkCropMsgProvider(DingTalkProperties properties) {
        super(properties);
    }

    @Override
    public Message send(DingTalkCropMsgTemplate template, Map<String, String> context) {
        String token = getToken();
        Map<String, Object> param = new HashMap<>();
        param.put("access_token", token);
        param.put("agent_id", template.getAgentId());
        param.put("msg", template.getMsg());
        if (StringUtils.isNotBlank(template.getUserIdList())) {
            param.put("userid_list", template.getUserIdList());
        }
        if (StringUtils.isNotBlank(template.getDeptIdList())) {
            param.put("dept_id_list", template.getDeptIdList());
        }
        param.put("to_all_user", template.isToAllUser());
        String response = Httpx.post(NOTIFY_API, param);
        JSONObject object = JSON.parseObject(response);
        return Message.builder()
                .result(SUCCESS_RESULT.equals(object.getString("errcode")))
                .desc(object.getString("errmsg"))
                .build();
    }

    private String getToken() {
        if (System.currentTimeMillis() - refreshTokenTime > tokenTimeOut || accessToken.get() == null) {
            return requestToken();
        }
        return accessToken.get();
    }


    /**
     * 获取钉钉token
     *
     * @return 结果
     */
    private String requestToken() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("corpid", properties.getAppKey());
        paramMap.put("corpsecret", properties.getAppSecret());
        String response = Httpx.get(TOKEN_API, paramMap);
        JSONObject object = JSON.parseObject(response);
        if (SUCCESS_RESULT.equals(object.getString("errcode"))) {
            String token = object.getString("access_token");
            refreshTokenTime = System.currentTimeMillis();
            accessToken.set(token);
            return token;
        }

        Logger.error("获取钉钉token失败：{}", object.getString("errmsg"));
        return null;


    }


}
