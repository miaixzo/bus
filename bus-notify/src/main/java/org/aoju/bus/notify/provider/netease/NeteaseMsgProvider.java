package org.aoju.bus.notify.provider.netease;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.notify.magic.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * 云信消息
 *
 * @author wubenhui
 * @since 2020/3/19
 */
public class NeteaseMsgProvider extends AbstractNeteaseProvider {

    private static final String API = "https://api.netease.im/nimserver/msg/sendMsg.action";

    public NeteaseMsgProvider(NeteaseProperties properties) {
        super(properties);
    }

    @Override
    public Message send(NeteaseMsgTemplate template, Map<String, String> context) {
        //构造payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", template.getContent());
        Map<String, Object> aps = new HashMap<>();
        aps.put("mutable-content", "1");
        Map<String, Object> alert = new HashMap<>();
        alert.put("title", template.getTitle());
        alert.put("body", template.getMessage());
        aps.put("alert", alert);
        payload.put("apsField", aps);

        //构造请求参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("from", template.getSender());
        param.put("to", template.getReceive());
        param.put("ope", "0");
        param.put("type", "100");
        param.put("body", template.getContent());
        param.put("payload", JSONObject.toJSONString(payload));

        Map<String, Object> option = new HashMap<>();
        option.put("needPushNick", "false");
        option.put("sendersync", "false");
        param.put("option", JSON.toJSONString(option));
        return post(API, param);
    }


}
